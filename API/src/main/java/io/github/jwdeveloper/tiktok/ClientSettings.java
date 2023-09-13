package io.github.jwdeveloper.tiktok;

import lombok.Data;

import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;

@Data
public class ClientSettings {
    /**
     * Timeout for Connections
     */
    private Duration timeout;

    // public RotatingProxy Proxy;
    /**
     * ISO-Language for Client
     */

    private String clientLanguage;

    /**
     * Whether to Retry if Connection Fails
     */
    private boolean retryOnConnectionFailure;


    /**
     * Wait to connect again for selected amount of time
     */
    private Duration retryConnectionTimeout;

    /**
     * Whether to handle Messages received from Room when Connecting
     */
    private boolean handleExistingMessagesOnConnect;
    /**
     * Whether to download List of Gifts for Room when Connecting
     */
    private boolean downloadGiftInfo;

    /**
     * Whether to print Logs to Console
     */

    private boolean printToConsole;
    /**
     * LoggingLevel for Logs
     */
    private Level logLevel;

    /**
     * Whether to print Base64-Data for Messages to Console
     */
    private boolean printMessageData;

    /**
     * Tiktok user name
     */
    private String hostName;


    /**
     * Parameters used in requests to Tiktok api
     */
    private Map<String, Object> clientParameters;


    /*
     * Optional: Sometimes not every messages from chat are send to TikTokLiveJava to fix this issue you can set sessionId
     *  documentation how to obtain sessionId https://github.com/isaackogan/TikTok-Live-Connector#send-chat-messages
     */
    private String sessionId;

}

