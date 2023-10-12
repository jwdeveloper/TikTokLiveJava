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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class Constants {

    /**
     * Web-URL for TikTok
     */
    public static final String TIKTOK_URL_WEB = "https://www.tiktok.com/";
    /**
     * WebCast-BaseURL for TikTok
     */
    public static final String TIKTOK_URL_WEBCAST = "https://webcast.tiktok.com/webcast/";
    /**
     * Signing API by Isaac Kogan
     * https://github-wiki-see.page/m/isaackogan/TikTokLive/wiki/All-About-Signatures
     */
    public static final String TIKTOK_SIGN_API = "https://tiktok.eulerstream.com/webcast/sign_url";

    /**
     * Default TimeOut for Connections
     */
    public static final int DEFAULT_TIMEOUT = 20;


    /**
     * Default Settings for Client
     */
    public static ClientSettings DefaultClientSettings() {
        var clientSettings = new ClientSettings();
        clientSettings.setTimeout(Duration.ofSeconds(DEFAULT_TIMEOUT));
        clientSettings.setClientLanguage("en-US");
        clientSettings.setHandleExistingEvents(true);
        clientSettings.setRetryOnConnectionFailure(false);
        clientSettings.setRetryConnectionTimeout(Duration.ofSeconds(1));
        clientSettings.setPrintToConsole(false);
        clientSettings.setLogLevel(Level.ALL);
        clientSettings.setClientParameters(Constants.DefaultClientParams());
        return clientSettings;
    }


    /**
     * Default Parameters for HTTP-Request
     */
    public static Map<String, Object> DefaultClientParams() {
        var clientParams = new TreeMap<String, Object>();
        clientParams.put("aid", 1988);
        clientParams.put("app_language", "en-US");
        clientParams.put("app_name", "tiktok_web");
        clientParams.put("browser_language", "en");
        clientParams.put("browser_name", "Mozilla");
        clientParams.put("browser_online", true);
        clientParams.put("browser_platform", "Win32");
        clientParams.put("browser_version", "5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Safari/537.36");
        clientParams.put("cookie_enabled", true);
        clientParams.put("cursor", "");
        clientParams.put("internal_ext", "");
        clientParams.put("device_platform", "web");
        clientParams.put("focus_state", true);
        clientParams.put("from_page", "user");
        clientParams.put("history_len", 4);
        clientParams.put("is_fullscreen", false);
        clientParams.put("is_page_visible", true);
        clientParams.put("did_rule", 3);
        clientParams.put("fetch_rule", 1);
        clientParams.put("identity", "audience");
        clientParams.put("last_rtt", 0);
        clientParams.put("live_id", 12);
        clientParams.put("resp_content_type", "protobuf");
        clientParams.put("screen_height", 1152);
        clientParams.put("screen_width", 2048);
        clientParams.put("tz_name", "Europe/Berlin");
        clientParams.put("referer", "https, //www.tiktok.com/");
        clientParams.put("root_referer", "https, //www.tiktok.com/");
        clientParams.put("msToken", "");
        clientParams.put("version_code", 180800);
        clientParams.put("webcast_sdk_version", "1.3.0");
        clientParams.put("update_version_code", "1.3.0");


        return clientParams;
    }


    /**
     * Default Headers for HTTP-Request
     */
    public static Map<String, String> DefaultRequestHeaders() {
        var headers = new HashMap<String, String>();

        headers.put("authority","www.tiktok.com");
        headers.put("Connection", "keep-alive");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Accept", "text/html,application/json,application/protobuf");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Safari/537.36");
        headers.put("Referer", "https://www.tiktok.com/");
        headers.put("Origin", "https://www.tiktok.com");
        headers.put("Accept-Language", "en-US,en; q=0.9");
        headers.put("Accept-Encoding", "gzip, deflate");
        return headers;
    }
}
