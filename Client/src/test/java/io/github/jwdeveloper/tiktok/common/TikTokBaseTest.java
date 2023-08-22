package io.github.jwdeveloper.tiktok.common;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;

import java.io.IOException;
import java.util.Base64;

public class TikTokBaseTest
{
    public byte[] getFileBytes(String path)
    {
        try {
            var stream = getClass().getClassLoader().getResourceAsStream(path);
            var bytes=  stream.readAllBytes();
            stream.close();
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getFileBytesUtf(String path)
    {
        try {
            var stream = getClass().getClassLoader().getResourceAsStream(path);
            var bytes=  stream.readAllBytes();
            stream.close();
            return Base64.getDecoder().decode(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
