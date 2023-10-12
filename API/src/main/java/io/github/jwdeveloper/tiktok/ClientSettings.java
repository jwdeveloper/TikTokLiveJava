/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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

    /**
     * ISO-Language for Client
     */

    private String clientLanguage;

    /**
     * Whether to Retry if Connection Fails
     */
    private boolean retryOnConnectionFailure;


    /**
     * Before retrying connect, wait for select amount of time
     */
    private Duration retryConnectionTimeout;

    /**
     * Whether to handle Events received from Room when Connecting
     */
    private boolean handleExistingEvents;

    /**
     * Whether to print Logs to Console
     */

    private boolean printToConsole;
    /**
     * LoggingLevel for Logs
     */
    private Level logLevel;


    /**
     *  Optional: Use it if you need to change TikTok live hostname in builder
     */
    private String hostName;


    /**
     * Parameters used in requests to TikTok api
     */
    private Map<String, Object> clientParameters;


    /*
     *  Optional: Sometimes not every messages from chat are send to TikTokLiveJava to fix this issue you can set sessionId
     *  documentation how to obtain sessionId https://github.com/isaackogan/TikTok-Live-Connector#send-chat-messages
     */
    private String sessionId;

    /*
     * Optional: By default roomID is fetched before connect to live, but you can set it manually
     *
     */
    private String roomId;

}

