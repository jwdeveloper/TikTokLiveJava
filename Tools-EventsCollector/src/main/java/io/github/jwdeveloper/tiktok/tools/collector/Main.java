package io.github.jwdeveloper.tiktok.tools.collector;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.collector.tables.ExceptionInfoModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;

import java.sql.SQLException;
import java.util.Base64;

public class Main {

    public static void main(String[] args) throws SQLException {
        var tiktokUser = "mr_cios";
        var db = new TikTokDatabase("test");
        db.init();
        TikTokLive.newClient(tiktokUser)
                .onSuccessResponseMapping((liveClient, event) ->
                {
                    var eventName = event.getEvent().getClass().getSimpleName();
                    var binary = Base64.getEncoder().encodeToString(event.getMessage().getBinary().toByteArray());
                    var model = TikTokMessageModel.builder()
                            .type("message")
                            .hostName(tiktokUser)
                            .eventName(eventName)
                            .eventContent(binary)
                            .build();

                    db.insertMessage(model);
                    System.out.println("EVENT: " + eventName);
                })
                .onError((liveClient, event) ->
                {
                    var exception = event.getException();
                    var exceptionContent = ExceptionInfoModel.getStackTraceAsString(exception);
                    var builder = TikTokErrorModel.builder();
                    if (exception instanceof TikTokLiveMessageException ex) {
                        builder.hostName(tiktokUser)
                                .errorName(ex.messageName())
                                .errorType("error-message")
                                .exceptionContent(exceptionContent)
                                .message(ex.messageToBase64())
                                .response(ex.webcastResponseToBase64());
                    } else {
                        builder.hostName(tiktokUser)
                                .errorName(exception.getClass().getSimpleName())
                                .errorType("error-system")
                                .exceptionContent(exceptionContent)
                                .message("")
                                .response("");
                    }

                    var error = builder.build();
                    db.insertError(error);
                    System.out.println("ERROR: "+error.getErrorName());
                    exception.printStackTrace();

                })
                .buildAndRun();
    }


}
