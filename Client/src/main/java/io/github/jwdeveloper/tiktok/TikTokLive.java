package io.github.jwdeveloper.tiktok;



public class TikTokLive
{
    public static TikTokClientBuilder newClient(String userName)
    {
        return new TikTokClientBuilder(userName);
    }
}
