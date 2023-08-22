package io.github.jwdeveloper.tiktok;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.common.TikTokBaseTest;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandler;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.mockito.Mockito.mock;

public class WebResponseHandlerTests extends TikTokBaseTest
{
    public static WebResponseHandler sut;

    @Before
    public void before()
    {
        var mockEventHandler = mock(TikTokEventHandler.class);
        var mockGiftManager = mock(TikTokGiftManager.class);
        sut = new WebResponseHandler(mockEventHandler, mockGiftManager);
    }


}
