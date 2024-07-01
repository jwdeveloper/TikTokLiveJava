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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.dependance.Dependance;
import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.mappers.MessagesMapperFactory;
import io.github.jwdeveloper.tiktok.common.LoggerFactory;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.*;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.link.*;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.room.*;
import io.github.jwdeveloper.tiktok.data.events.social.*;
import io.github.jwdeveloper.tiktok.data.events.websocket.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftsManager;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.listener.*;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.live.builder.*;
import io.github.jwdeveloper.tiktok.mappers.*;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokCommonEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokRoomInfoEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokSocialMediaEventHandler;
import io.github.jwdeveloper.tiktok.websocket.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements LiveClientBuilder {

    protected final LiveClientSettings clientSettings;
    protected final TikTokLiveEventHandler eventHandler;
    protected final List<TikTokEventListener> listeners;
    protected final List<Consumer<TikTokMapper>> onCustomMappings;

    public TikTokLiveClientBuilder(String userName) {
        this.clientSettings = LiveClientSettings.createDefault();
        this.clientSettings.setHostName(userName);
        this.eventHandler = new TikTokLiveEventHandler();
        this.listeners = new ArrayList<>();
        this.onCustomMappings = new ArrayList<>();
    }

    public LiveClientBuilder onMapping(Consumer<TikTokMapper> consumer) {
        this.onCustomMappings.add(consumer);
        return this;
    }

    public TikTokLiveClientBuilder configure(Consumer<LiveClientSettings> onConfigure) {
        onConfigure.accept(clientSettings);
        return this;
    }

    public TikTokLiveClientBuilder addListener(TikTokEventListener listener) {
        if (listener != null)
            listeners.add(listener);
        return this;
    }

    protected void validate() {
        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().isEmpty())
            clientSettings.setClientLanguage("en");

        if (clientSettings.getHostName() == null || clientSettings.getHostName().isEmpty())
            throw new TikTokLiveException("HostName can not be null");

        if (clientSettings.getHostName().startsWith("@"))
            clientSettings.setHostName(clientSettings.getHostName().substring(1));

        if (clientSettings.getPingInterval() < 250)
            throw new TikTokLiveException("Minimum allowed ping interval is 250 millseconds");

        var httpSettings = clientSettings.getHttpSettings();
        httpSettings.getParams().put("app_language", clientSettings.getClientLanguage());
        httpSettings.getParams().put("webcast_language", clientSettings.getClientLanguage());
    }

    //TODO each class registered to container should implement own interface,
    public LiveClient build() {
        validate();

        //Docs: https://github.com/jwdeveloper/DepenDance?tab=readme-ov-file#01-basic
        var dependance = Dependance.newContainer();

        //config
        dependance.registerSingleton(LiveClientSettings.class, clientSettings);
        dependance.registerSingleton(Logger.class, LoggerFactory.create(clientSettings.getHostName(), clientSettings));
        dependance.registerSingleton(TikTokRoomInfo.class, container ->
        {
            var roomInfo = new TikTokRoomInfo();
            roomInfo.setHostName(clientSettings.getHostName());
            return roomInfo;
        });

        //messages
        dependance.registerSingleton(TikTokLiveEventHandler.class, eventHandler);
        dependance.registerSingleton(TikTokLiveMessageHandler.class);

        //listeners
        dependance.registerSingletonList(TikTokEventListener.class, (e) -> listeners);
        dependance.registerSingleton(ListenersManager.class, TikTokListenersManager.class);

        //networking
        dependance.registerSingleton(HttpClientFactory.class);
        if (clientSettings.isOffline()) {
            dependance.registerSingleton(SocketClient.class, TikTokWebSocketOfflineClient.class);
            dependance.registerSingleton(LiveHttpClient.class, TikTokLiveHttpOfflineClient.class);
        } else {
            dependance.registerSingleton(SocketClient.class, TikTokWebSocketClient.class);
            dependance.registerSingleton(LiveHttpClient.class, TikTokLiveHttpClient.class);
        }

        /* TODO in future, custom proxy implementation that can be provided via builder
         * if(customProxy != null)
         *    dependance.registerSingleton(TikTokProxyProvider.class,customProxy);
         * else
         *    dependance.registerSingleton(TikTokProxyProvider.class,DefaultProxyProvider.class);
         */

        //gifts
        if (clientSettings.isFetchGifts()) {
            dependance.registerSingleton(GiftsManager.class, TikTokLive.gifts());
        } else {
            dependance.registerSingleton(GiftsManager.class, new TikTokGiftsManager(List.of()));
        }

        //mapper
        dependance.registerSingleton(TikTokGenericEventMapper.class);
        dependance.registerSingleton(TikTokMapperHelper.class, TikTokLiveMapperHelper.class);
        dependance.registerSingleton(TikTokMapper.class, (container) ->
        {
            var dependace = (DependanceContainer) container.find(DependanceContainer.class);
            var mapper = MessagesMapperFactory.create(dependace);
            onCustomMappings.forEach(action -> action.accept(mapper));
            return mapper;
        });

        //mapper handlers
        dependance.registerSingleton(TikTokCommonEventHandler.class);
        dependance.registerSingleton(TikTokGiftEventHandler.class);
        dependance.registerSingleton(TikTokRoomInfoEventHandler.class);
        dependance.registerSingleton(TikTokSocialMediaEventHandler.class);

        //client
        dependance.registerSingleton(LiveClient.class, TikTokLiveClient.class);

        var container = dependance.build();
        return container.find(LiveClient.class);
    }

    public LiveClient buildAndConnect() {
        var client = build();
        client.connect();
        return client;
    }

    public CompletableFuture<LiveClient> buildAndConnectAsync() {
        return build().connectAsync();
    }

    @Override
    public <E extends TikTokEvent> LiveClientBuilder onEvent(Class<E> eventClass, EventConsumer<E> action) {
        eventHandler.subscribe(eventClass, action);
        return this;
    }
}