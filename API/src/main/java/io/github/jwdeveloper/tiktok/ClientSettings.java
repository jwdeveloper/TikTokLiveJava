package io.github.jwdeveloper.tiktok;

import lombok.Data;

import java.time.Duration;
import java.util.logging.Level;

@Data
public class ClientSettings {
    /// <summary>
    /// Timeout for Connections
    /// </summary>

    private Duration Timeout;
    /// <summary>
    /// Polling-Interval for Socket-Connection
    /// </summary

    private Duration PollingInterval;
    /// <summary>
    /// Proxy for Connection
    /// </summary>

    // public RotatingProxy Proxy;
    /// <summary>
    /// ISO-Language for Client
    /// </summary>

    private String ClientLanguage;
    /// <summary>
    /// Size for Buffer for Socket-Connection
    /// </summary>

    private int SocketBufferSize;

    /// <summary>
    /// Whether to Retry if Connection Fails
    /// </summary>
    private boolean RetryOnConnectionFailure;


    /// <summary>
    /// Whether to handle Messages received from Room when Connecting
    /// </summary>
    private boolean HandleExistingMessagesOnConnect;
    /// <summary>
    /// Whether to download List of Gifts for Room when Connecting
    /// </summary>
    private boolean DownloadGiftInfo;

    /// <summary>
    /// Whether to print Logs to Console
    /// </summary>

    private boolean PrintToConsole;
    /// <summary>
    /// LoggingLevel for Logs
    /// </summary>
    private Level LogLevel;

    /// <summary>
    /// Whether to print Base64-Data for Messages to Console
    /// </summary>
    private boolean PrintMessageData;

    /// <summary>
    /// Whether to check Messages for Unparsed Data
    /// </summary>
    private boolean CheckForUnparsedData;
}

