package io.github.jwdeveloper.tiktok.http;

import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils
{
    public static String parseParameters(String url, Map<String,Object> parameters)
    {
        var parameterString = "";
        if (!parameters.isEmpty()) {
            var builder = new StringBuilder();
            builder.append("?");
            var first = false;
            for (var param : parameters.entrySet()) {

                if (first) {
                    builder.append("&");
                }
                builder.append(param.getKey()).append("=").append(param.getValue());
                first = true;
            }
            parameterString = builder.toString();
        }

        return url+parameterString;
    }

    @SneakyThrows
    public static String parseParametersEncode(String url, Map<String,Object> parameters)
    {

        var parameterString = "";
        if (!parameters.isEmpty()) {
            var builder = new StringBuilder();
            builder.append("?");
            var first = false;
            for (var param : parameters.entrySet()) {

                if (first) {
                    builder.append("&");
                }

                final String encodedKey = URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8);
                final String encodedValue = URLEncoder.encode(param.getValue().toString(), StandardCharsets.UTF_8);
                builder.append(encodedKey).append("=").append(encodedValue);
                first = true;
            }
            parameterString = builder.toString();
        }

        return url+parameterString;
    }
}
