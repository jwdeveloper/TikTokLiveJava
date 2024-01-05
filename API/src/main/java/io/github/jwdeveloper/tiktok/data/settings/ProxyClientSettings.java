package io.github.jwdeveloper.tiktok.data.settings;

import lombok.Getter;
import lombok.Setter;

//TODO proxy implementation
@Getter
public class ProxyClientSettings
{
    @Setter
    private boolean useProxy;


    public ProxyClientSettings clone()
    {
        return new ProxyClientSettings();
    }
}
