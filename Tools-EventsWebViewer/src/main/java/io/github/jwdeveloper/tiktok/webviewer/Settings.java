package io.github.jwdeveloper.tiktok.webviewer;


import lombok.Data;

@Data
public class Settings
{

    private int port;
    private String dbName;
    private String userName;
    private String sessionTag;
}
