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
package io.github.jwdeveloper.tiktok.tools.collector;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.events.messages.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.collector.tables.ExceptionInfoModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RunCollector {

    //https://protobuf-decoder.netlify.app/
    //https://streamdps.com/tiktok-widgets/gifts/


    public static List<String> ignoredEvents;

    public static List<Class<?>> filter;

    public static void main(String[] args) throws SQLException {
        ignoredEvents = new ArrayList<>();
        //ignoredEvents = List.of("TikTokJoinEvent","TikTokLikeEvent");

        filter = new ArrayList<>();
        filter.add(TikTokUnhandledSocialEvent.class);
        filter.add(TikTokFollowEvent.class);
        filter.add(TikTokLikeEvent.class);
        filter.add(TikTokShareEvent.class);
        filter.add(TikTokJoinEvent.class);


        var db = new TikTokDatabase("social_db");
        db.init();

        var errors = db.selectErrors();

        var users = new ArrayList<String>();
        users.add("mia_tattoo");
        users.add("mr_wavecheck");
        users.add("bangbetmenygy");
        users.add("larasworld0202");
        for (var user : users)
        {
            try {
                runTikTokLiveInstance(user, db);
            }
            catch (Exception e)
            {

            }

        }
    }

    private static void runTikTokLiveInstance(String tiktokUser, TikTokDatabase tikTokDatabase) {

        TikTokLive.newClient(tiktokUser)
                .onConnected((liveClient, event) ->
                {
                    System.out.println("CONNECTED TO "+liveClient.getRoomInfo().getUserName());
                })
                .onWebsocketMessage((liveClient, event) ->
                {
                    var eventName = event.getEvent().getClass().getSimpleName();

                    if(filter.size() != 0 &&  !filter.contains(event.getEvent().getClass()))
                    {
                        return;
                    }

                    var binary = Base64.getEncoder().encodeToString(event.getMessage().toByteArray());
                    var model = new TikTokMessageModel();
                    model.setType("messsage");
                    model.setHostName(tiktokUser);
                    model.setEventName(eventName);
                    model.setEventContent(binary);

                    tikTokDatabase.insertMessage(model);
                    System.out.println("EVENT: [" + tiktokUser + "] " + eventName);
                })
                .onError((liveClient, event) ->
                {
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
                    System.out.println("ERROR: " + errorModel.getErrorName());
                    exception.printStackTrace();

                })
                .buildAndRunAsync();
    }


}
