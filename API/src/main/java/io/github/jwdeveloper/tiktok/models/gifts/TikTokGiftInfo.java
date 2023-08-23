package io.github.jwdeveloper.tiktok.models.gifts;

import lombok.Data;

import java.util.List;

@Data
public class TikTokGiftInfo
{
    private int action_type;
    private int app_id;
    private String business_text;
    private boolean can_put_in_gift_box;
    private List<Object> color_infos;
    private boolean combo;
    private String describe;
    private int diamond_count;
    private int duration;
    private String event_name;
    private boolean for_custom;
    private boolean for_linkmic;
    private GiftLabelIcon gift_label_icon;
    private GiftPanelBanner gift_panel_banner;
    private String gift_rank_recommend_info;
    private int gift_scene;
    private String gold_effect;
    private String gray_scheme_url;
    private String guide_url;
    private Icon icon;
    private int id;
    private Image image;
    private boolean is_box_gift;
    private boolean is_broadcast_gift;
    private boolean is_displayed_on_panel;
    private boolean is_effect_befview;
    private boolean is_gray;
    private boolean is_random_gift;
    private int item_type;
    private LockInfo lock_info;
    private String manual;
    private String name;
    private boolean notify;
    private int primary_effect_id;
    private String region;
    private String scheme_url;
    private SpecialEffects special_effects;
    private TrackerParams tracker_params;
    private List<Object> trigger_words;
    private int type;
}
