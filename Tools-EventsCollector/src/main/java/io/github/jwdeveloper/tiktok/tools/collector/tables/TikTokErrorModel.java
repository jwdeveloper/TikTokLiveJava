package io.github.jwdeveloper.tiktok.tools.collector.tables;

import lombok.Data;

@Data
public class TikTokErrorModel
{
    private Integer id;

    private String hostName;

    private String errorName;

    private String errorType;

    private String exceptionContent;

    private String message;

    private String response;

    private String createdAt;
}
