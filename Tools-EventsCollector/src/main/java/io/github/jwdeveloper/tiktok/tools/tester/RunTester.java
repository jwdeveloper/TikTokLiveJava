package io.github.jwdeveloper.tiktok.tools.tester;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;

import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

public class RunTester {

    public static void main(String[] args) throws SQLException, InvalidProtocolBufferException {

            var db = new TikTokDatabase("test");
            db.init();
            var errors = db.selectErrors();


            var handler =  getMessageHandler();
            for (var error : errors) {

                var bytes = Base64.getDecoder().decode(error.getResponse());
                var response = WebcastResponse.parseFrom(bytes);
                handler.handle(null,response);
            }


    }

    public  static TikTokMessageHandlerRegistration getMessageHandler()
    {
        var observer = new TikTokEventObserver();
        observer.<TikTokErrorEvent>subscribe(TikTokErrorEvent.class,(liveClient, event) ->
        {
             event.getException().printStackTrace();
        });
        var settings = new ClientSettings();
        //settings.setPrintMessageData(true);
        var logger = Logger.getGlobal();
        var roomInfo = new TikTokRoomInfo();
        var manager = new TikTokGiftManager();
        return new TikTokMessageHandlerRegistration(observer,settings,logger,manager,roomInfo);
    }
}
