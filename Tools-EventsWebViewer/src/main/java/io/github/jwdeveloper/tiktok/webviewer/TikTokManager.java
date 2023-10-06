package io.github.jwdeveloper.tiktok.webviewer;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.tools.collector.client.MessageCollector;
import io.github.jwdeveloper.tiktok.tools.collector.client.TikTokMessageCollectorClient;
import io.github.jwdeveloper.tiktok.tools.collector.client.TikTokMessagessCollectorBuilder;
import io.github.jwdeveloper.tiktok.tools.util.MessageUtil;
import lombok.Value;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class TikTokManager {
    TikTokMessagessCollectorBuilder client;
    MessageCollector msgCollector;

    public TikTokManager() {
        msgCollector = new MessageCollector("web");
    }

    public void connect(String name) throws SQLException {
        disconnect();
        client = TikTokMessageCollectorClient.create(msgCollector, "web").addUser(name);
        client.buildAndRun();
    }

    public List<String> getEventsNames() {
        return msgCollector.getMessages().keySet().stream().toList();
    }

    public List<String> getEventMessages(String eventName) {
        return msgCollector.getMessages().get(eventName).stream().map(MessageCollector.MessageData::getEventData).toList();
    }


    public MessageDto getMessage(String event) throws InvalidProtocolBufferException {
        var eventData = msgCollector.getMessages().get(event);
        var messages = eventData.stream().toList();
        var random = new Random();
        var index = random.nextInt(messages.size()-1);
        var msg = messages.get(index);


        var bytes = Base64.getDecoder().decode(msg.getEventData());
        var content = MessageUtil.getContent(event,bytes);
        return new MessageDto(content, msg.getEventData(), event);
    }

    @Value
    public class MessageDto {
        String content;
        String base64;
        String eventName;
    }

    public void disconnect() {
        if (client == null) {
            return;
        }
        client.stop();
    }

}
