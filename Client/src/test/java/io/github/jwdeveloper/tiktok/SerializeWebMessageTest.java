package io.github.jwdeveloper.tiktok;

import com.google.protobuf.InvalidProtocolBufferException;

import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;
import org.junit.Test;
import java.io.IOException;

public class SerializeWebMessageTest
{
    @Test
    public void WebcastWebsocketMessage()
    {
        try (var str = getClass().getClassLoader().getResourceAsStream("WebcastWebsocketMessage.bin"))
        {
            var bytes = str.readAllBytes();
            var person = WebcastWebsocketMessage.parseFrom(bytes);
            System.out.println("id: " + person.getId());
            System.out.println("type: " + person.getType());
            System.out.println("binary: " + person.getBinary().size());
            // System.out.println("Email: " + person.getEmail());
        } catch (InvalidProtocolBufferException e) {
            System.out.println("Error parsing the protobuf message: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }
}
