/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.tools.collector.client;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.TikTokLiveClientBuilder;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.collector.tables.ExceptionInfoModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokResponseModel;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TikTokClientFactory {
    private final MessageCollector messageCollector;
    private final TikTokDatabase tikTokDatabase;

    public TikTokClientFactory(MessageCollector messageCollector, TikTokDatabase tikTokDatabase) {
        this.messageCollector = messageCollector;
        this.tikTokDatabase = tikTokDatabase;
    }

    public CompletableFuture<LiveClient> runClientAsync(String tiktokUser, List<Class<?>> filters, Consumer<LiveClientBuilder> onBuilder) {
        var builder = TikTokLive.newClient(tiktokUser);
        var msgFilter = filters.stream().map(Class::getSimpleName).toList();
        onBuilder.accept(builder);
        return builder.onConnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("CONNECTED TO " + liveClient.getRoomInfo().getHostName());
                })
                .onWebsocketResponse((liveClient, event) ->
                {
                    var response = Base64.getEncoder().encodeToString(event.getResponse().toByteArray());

                    var responseModel = new TikTokResponseModel();
                    responseModel.setResponse(response);
                    responseModel.setHostName(liveClient.getRoomInfo().getHostName());
                    tikTokDatabase.insertResponse(responseModel);
                    liveClient.getLogger().info("Response");
                    for (var message : event.getResponse().getMessagesList())
                    {
                        if(msgFilter.size() > 0 && !msgFilter.contains(message.getMethod()))
                        {
                            continue;
                        }
                        messageCollector.addMessage(liveClient.getLogger(), liveClient.getRoomInfo().getHostName(), message);
                    }
                })
                .onWebsocketMessage((liveClient, event) ->
                {
                    var eventName = event.getEvent().getClass().getSimpleName();

                    /*
                    if (msgFilter.size() != 0 && !msgFilter.contains(event.getEvent().getClass())) {
                        return;
                    }*/

                    var messageBinary = Base64.getEncoder().encodeToString(event.getMessage().toByteArray());
                    var model = new TikTokMessageModel();
                    model.setType("messsage");
                    model.setHostName(tiktokUser);
                    model.setEventName(eventName);
                    model.setMessage(messageBinary);

                    //   tikTokDatabase.insertMessage(model);
                   // liveClient.getLogger().info("EVENT: [" + tiktokUser + "] " + eventName);
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                    var exception = event.getException();
                    var exceptionContent = ExceptionInfoModel.getStackTraceAsString(exception);
                    var errorModel = new TikTokErrorModel();
                    if (exception instanceof TikTokLiveMessageException ex) {
                        errorModel.setHostName(tiktokUser);
                        errorModel.setErrorName(ex.messageMethod());
                        errorModel.setErrorType("error-message");
                        errorModel.setExceptionContent(exceptionContent);
                        errorModel.setMessage(ex.messageToBase64());
                        errorModel.setResponse(ex.webcastResponseToBase64());
                    } else {
                        errorModel.setHostName(tiktokUser);
                        errorModel.setErrorName(exception.getClass().getSimpleName());
                        errorModel.setErrorType("error-system");
                        errorModel.setExceptionContent(exceptionContent);
                        errorModel.setMessage("");
                        errorModel.setResponse("");
                    }


                    tikTokDatabase.insertError(errorModel);
                    liveClient.getLogger().info("ERROR: " + errorModel.getErrorName());
                    exception.printStackTrace();

                })
                .buildAndConnectAsync();
    }
}
