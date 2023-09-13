package io.github.jwdeveloper.tiktok.tools.collector.tables;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TikTokMessageModel
{
    private Integer id;

    private String hostName;

    private String eventName;

    private String type;

    private String eventContent;

    private String createdAt;
}
