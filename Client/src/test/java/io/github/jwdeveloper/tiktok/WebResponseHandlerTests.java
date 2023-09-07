package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.common.TikTokBaseTest;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import org.junit.Before;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class WebResponseHandlerTests extends TikTokBaseTest {
    public static TikTokMessageHandlerRegistration sut;

    @Before
    public void before() {
        var mockEventHandler = mock(TikTokEventObserver.class);
        var mockGiftManager = mock(TikTokGiftManager.class);
        var mockRoomInfo = mock(TikTokRoomInfo.class);
        var mockClientSettings = mock(ClientSettings.class);
        var mockLogger = mock(Logger.class);
        sut = new TikTokMessageHandlerRegistration(mockEventHandler,mockClientSettings,mockLogger, mockGiftManager, mockRoomInfo);
    }


}
