package io.github.jwdeveloper.tiktok;

import lombok.Data;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Data
public class ClientSettings {
    /// <summary>
    /// Timeout for Connections
    /// </summary>

    private Duration timeout;
    /// <summary>
    /// Polling-Interval for Socket-Connection
    /// </summary

    private Duration pollingInterval;
    /// <summary>
    /// Proxy for Connection
    /// </summary>

    // public RotatingProxy Proxy;
    /// <summary>
    /// ISO-Language for Client
    /// </summary>

    private String clientLanguage;

    /// <summary>
    /// Whether to Retry if Connection Fails
    /// </summary>
    private boolean retryOnConnectionFailure;

    /// <summary>
    /// Whether to handle Messages received from Room when Connecting
    /// </summary>
    private boolean handleExistingMessagesOnConnect;
    /// <summary>
    /// Whether to download List of Gifts for Room when Connecting
    /// </summary>
    private boolean downloadGiftInfo;

    /// <summary>
    /// Whether to print Logs to Console
    /// </summary>

    private boolean printToConsole;
    /// <summary>
    /// LoggingLevel for Logs
    /// </summary>
    private Level logLevel;

    /// <summary>
    /// Whether to print Base64-Data for Messages to Console
    /// </summary>
    private boolean printMessageData;

    /// <summary>
    /// Whether to check Messages for Unparsed Data
    /// </summary>
    private boolean checkForUnparsedData;


    /// <summary>
    /// Tiktok user name
    /// </summary>
    private String hostName;


    /// <summary>
    /// Parameters used in requests to tiktok api
    /// </summary>
    private Map<String, Object> clientParameters;
}

