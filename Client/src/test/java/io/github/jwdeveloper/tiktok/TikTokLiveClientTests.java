package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.live.GiftsManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.live.LiveMessagesHandler;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import io.github.jwdeveloper.tiktok.websocket.LiveSocketClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.logging.Logger;

public class TikTokLiveClientTests {

    private LiveClient sut;
    LiveMessagesHandler messageHandler;
    GiftsManager giftsManager;
    TikTokRoomInfo tikTokLiveMeta;
    LiveHttpClient tiktokHttpClient;
    LiveSocketClient webSocketClient;
    LiveEventsHandler tikTokEventHandler;
    LiveClientSettings clientSettings;
    ListenersManager listenersManager;
    Logger logger;

    @Before
    public void onBefore() {
        messageHandler = Mockito.mock(LiveMessagesHandler.class);
        giftsManager = Mockito.mock(GiftsManager.class);
        tikTokLiveMeta = Mockito.mock(TikTokRoomInfo.class);
        tiktokHttpClient = Mockito.mock(LiveHttpClient.class);
        webSocketClient = Mockito.mock(LiveSocketClient.class);
        tikTokEventHandler = Mockito.mock(LiveEventsHandler.class);
        clientSettings = Mockito.mock(LiveClientSettings.class);
        listenersManager = Mockito.mock(ListenersManager.class);
        logger = Mockito.mock(Logger.class);

        sut = new TikTokLiveClient(messageHandler,
                giftsManager,
                tikTokLiveMeta,
                tiktokHttpClient,
                webSocketClient,
                tikTokEventHandler,
                clientSettings,
                listenersManager,
                logger);
    }


    @Test
    public void shouldThrownWhenAlreadyConnected() {
        tikTokLiveMeta.setConnectionState(ConnectionState.CONNECTED);
        Assert.assertThrows(TikTokLiveException.class, () ->
        {
            sut.connect();
        });
    }

    @Test
    public void shouldThrowWhenUserIsOffline() {

        var request = new LiveData.Request("X");
        var response = new LiveData.Response();
        response.setLiveStatus(LiveData.LiveStatus.HostOffline);
        Mockito.when(tiktokHttpClient.fetchLiveData(request)).thenReturn(response);
        Assert.assertThrows(TikTokLiveException.class, () ->
        {
            sut.connect();
        });
    }

    @Test
    public void shouldThrowWhenUserNotFound()
    {
        var request = new LiveData.Request("X");
        var response = new LiveData.Response();
        response.setLiveStatus(LiveData.LiveStatus.HostNotFound);
        Mockito.when(tiktokHttpClient.fetchLiveData(request)).thenReturn(response);
        Assert.assertThrows(TikTokLiveException.class, () ->
        {
            sut.connect();
        });
    }

    @Test
    public void shouldThrowWhenAgeRestricted()
    {
        Mockito.when(tiktokHttpClient.fetchLiveData(new LiveData.Request("X")))
                .thenReturn(new LiveData.Response());
        Assert.assertThrows(TikTokLiveException.class, () ->
        {
            sut.connect();
        });
    }


    @Test
    public void shouldConnect() {
      //  sut.connect();
    }

}
