package io.github.jwdeveloper.tiktok;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

public class ConfigurationExample
{
    public static void main(String[] args) throws IOException {

        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .configure(clientSettings ->
                {
                    clientSettings.setHostName(Main.TEST_TIKTOK_USER); //tiktok user
                    clientSettings.setClientLanguage("en"); //language
                    clientSettings.setTimeout(Duration.ofSeconds(2)); //connection timeout
                    clientSettings.setLogLevel(Level.ALL); //log level
                    clientSettings.setDownloadGiftInfo(true); //TODO
                    clientSettings.setCheckForUnparsedData(true); //TODO
                    clientSettings.setPollingInterval(Duration.ofSeconds(1)); //TODO
                    clientSettings.setPrintMessageData(true); //TODO
                    clientSettings.setPrintToConsole(true); //TODO
                    clientSettings.setHandleExistingMessagesOnConnect(true); //TODO
                    clientSettings.setRetryOnConnectionFailure(true); //TODO
                })
                .buildAndRun();
        System.in.read();
    }
}
