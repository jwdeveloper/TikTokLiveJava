package io.github.jwdeveloper.tiktok.utils;

import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;

public class CancelationToken
{
    private boolean isCanceled =false;

    public void cancel()
    {
        isCanceled =true;
    }

    public boolean isCancel()
    {

        return isCanceled;
    }

    public void throwIfCancel()
    {
        if(!isCanceled)
        {
            return;
        }
        throw new TikTokLiveException("Token requested cancelation");
    }

    public boolean isNotCancel()
    {
        return !isCancel();
    }

    public static CancelationToken create()
    {
        return new CancelationToken();
    }
}
