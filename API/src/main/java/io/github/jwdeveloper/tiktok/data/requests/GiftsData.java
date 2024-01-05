package io.github.jwdeveloper.tiktok.data.requests;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class GiftsData
{
    @Getter
    public final class Request
    {

    }

    @Getter
    @AllArgsConstructor
    public static final class Response
    {
        private String json;
        private List<GiftModel> gifts;
    }

    @Data
    public static class GiftModel
    {
        private int id;
        private String name;
        private int diamondCost;
        private String image;
    }

}
