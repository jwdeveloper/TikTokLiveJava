package io.github.jwdeveloper.tiktok.tools.collector;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.collector.tables.ExceptionInfoModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokMessageModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class RunCollector {

    //https://protobuf-decoder.netlify.app/
    //https://streamdps.com/tiktok-widgets/gifts/
    /*
       mia_tattoo
       moniczkka
       besin1276
     */

    public static List<String> ignoredEvents;

    public static void main(String[] args) throws SQLException {
        ignoredEvents = new ArrayList<>();
        //ignoredEvents = List.of("TikTokJoinEvent","TikTokLikeEvent");


        var db = new TikTokDatabase("test");
        db.init();

        var errors = db.selectErrors();

        var users = new ArrayList<String>();
        //   users.add("mia_tattoo");
        //  users.add("moniczkka");
        //  users.add("besin1276");
        users.add("evequinte96");
        for (var user : users) {
            runTikTokLiveInstance(user, db);
        }
    }

    private static void runTikTokLiveInstance(String tiktokUser, TikTokDatabase tikTokDatabase) {

        TikTokLive.newClient(tiktokUser)
                .onWebsocketMessage((liveClient, event) ->
                {
                    var eventName = event.getEvent().getClass().getSimpleName();

                    if (ignoredEvents.contains(eventName)) {
                        return;
                    }

                    var binary = Base64.getEncoder().encodeToString(event.getMessage().getBinary().toByteArray());
                    var model = TikTokMessageModel.builder()
                            .type("message")
                            .hostName(tiktokUser)
                            .eventName(eventName)
                            .eventContent(binary)
                            .build();

                    tikTokDatabase.insertMessage(model);
                    System.out.println("EVENT: [" + tiktokUser + "] " + eventName);
                })
                .onError((liveClient, event) ->
                {
                    var exception = event.getException();
                    var exceptionContent = ExceptionInfoModel.getStackTraceAsString(exception);
                    var errorModel = new TikTokErrorModel();
                    if (exception instanceof TikTokLiveMessageException ex) {
                        errorModel.setHostName(tiktokUser);
                        errorModel.setErrorName(ex.messageName());
                        errorModel.setErrorType("error-message");
                        errorModel.setExceptionContent(exceptionContent);
                        errorModel.setMessage(ex.messageToBase64());
                        errorModel.setResponse(ex.webcastResponseToBase64());
                    } else {
                        errorModel.setHostName(tiktokUser);
                        errorModel.setErrorName(exception.getClass().getSimpleName());
                        errorModel.setErrorType("error-system");
                        errorModel.setExceptionContent(exceptionContent);
                        errorModel.setMessage("");
                        errorModel.setResponse("");
                    }


                    tikTokDatabase.insertError(errorModel);
                    System.out.println("ERROR: " + errorModel.getErrorName());
                    exception.printStackTrace();

                })
                .buildAndRun();
    }


}
