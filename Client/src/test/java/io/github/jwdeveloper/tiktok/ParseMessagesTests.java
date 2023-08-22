package io.github.jwdeveloper.tiktok;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.messages.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastLikeMessage;
import org.junit.Test;

public class ParseMessagesTests extends TikTokBaseTest
{


    @Test
    public void ShouldParseLikeMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("LikeMessage.bin");
        var message = WebcastLikeMessage.parseFrom(bytes);
    }

    @Test
    public void ShouldParseGiftMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastGiftMessage.bin");
        var message = WebcastGiftMessage.parseFrom(bytes);
    }
    @Test
    public void ShouldParseMessageWebcastChatMessage() throws InvalidProtocolBufferException {
        var bytes = getFileBytesUtf("MessageWebcastChatMessage.bin");
        var message = WebcastChatMessage.parseFrom(bytes);
    }


}
