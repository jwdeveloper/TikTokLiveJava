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
                })
                .buildAndRun();
        System.in.read();
    }
}
