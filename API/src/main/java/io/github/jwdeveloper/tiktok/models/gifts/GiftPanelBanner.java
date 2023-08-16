package io.github.jwdeveloper.tiktok.models.gifts;

import lombok.Data;

import java.util.List;


@Data
public class GiftPanelBanner
{
    private List<Object> bg_color_values ;
    private DisplayText display_text ;
    private LeftIcon left_icon ;
    private String schema_url ;
}