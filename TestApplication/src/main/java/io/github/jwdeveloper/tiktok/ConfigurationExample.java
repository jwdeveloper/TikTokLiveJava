package io.github.jwdeveloper.tiktok;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

public class ConfigurationExample {
    public static void main(String[] args) throws IOException {

        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .configure(clientSettings ->
                {
                    clientSettings.setHostName(Main.TEST_TIKTOK_USER); // TikTok user name
                    clientSettings.setClientLanguage("en"); // Language
                    clientSettings.setTimeout(Duration.ofSeconds(2)); // Connection timeout
                    clientSettings.setLogLevel(Level.ALL); // Log level
                    clientSettings.setDownloadGiftInfo(true); // Downloading meta information about gifts. You can access it by client.getGiftManager().getGiftsInfo();
                    clientSettings.setPrintMessageData(true); // Printing TikTok Protocol buffer messages in Base64 format
                    clientSettings.setPrintToConsole(true); // Printing all logs to console even if log level is Level.OFF
                    clientSettings.setHandleExistingMessagesOnConnect(true); // Invokes all TikTok events that had occurred before connection
                    clientSettings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
                    clientSettings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Timeout before next reconnection

                    //Optional: Sometimes not every message from chat are send to TikTokLiveJava to fix this issue you can set sessionId
                    // documentation how to obtain sessionId https://github.com/isaackogan/TikTok-Live-Connector#send-chat-messages
                    clientSettings.setSessionId("86c3c8bf4b17ebb2d74bb7fa66fd0000");

                    //Optional:
                    clientSettings.setRoomId("XXXXXXXXXXXXXXXXX");
                })
                .buildAndRun();
        System.in.read();
    }
}
