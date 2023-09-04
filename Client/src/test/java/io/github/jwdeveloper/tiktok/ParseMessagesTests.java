package io.github.jwdeveloper.tiktok;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.common.TikTokBaseTest;
import io.github.jwdeveloper.tiktok.messages.*;
import org.junit.Test;

public class ParseMessagesTests extends TikTokBaseTest
{


    @Test
    public void ShouldParseMessageWebcastLikeMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("LikeMessage.bin");
        var message = WebcastLikeMessage.parseFrom(bytes);
    }

    @Test
    public void ShouldParseMessageWebcastGiftMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastGiftMessage.bin");
        var message = WebcastGiftMessage.parseFrom(bytes);
    }
    @Test
    public void ShouldParseMessageWebcastChatMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastChatMessage.bin");
        var message = WebcastChatMessage.parseFrom(bytes);
    }

    @Test
    public void ShouldParseMessageWebcastImDeleteMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastImDeleteMessage.bin");
        var message = WebcastImDeleteMessage.parseFrom(bytes);
    }


    @Test
    public void ShouldParseMessageWebcastSocialMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastSocialMessage.bin");
        var message = WebcastSocialMessage.parseFrom(bytes);
    }


    @Test
    public void ShouldParseMessageWebcastMemberMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("WebcastMemberMessage.bin");
        var message = WebcastMemberMessage.parseFrom(bytes);
    }

}
