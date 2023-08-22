package io.github.jwdeveloper.tiktok;



public class TikTokLive
{
    public static TikTokLiveClientBuilder newClient(String userName)
    {
        return new TikTokLiveClientBuilder(userName);
    }
}
