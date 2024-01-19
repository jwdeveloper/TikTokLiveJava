
Records stream to flv file


```xml
   <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
            <artifactId>Client</artifactId>
            <version>1.1.0-Release</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
            <artifactId>extension-recorder</artifactId>
            <version>1.1.0-Release</version>
        </dependency>
    </dependencies>
```


Usage

```java

public static void main(String[] args) {

        TikTokLive.newClient("bangbetmenygy")
        .configure(liveClientSettings ->
        {
        liveClientSettings.setPrintToConsole(true);
        })
        .onError((liveClient, event) ->
        {
        event.getException().printStackTrace();
        })
        .addListener(TikTokLiveRecorder.use(recorderSettings ->
        {
        recorderSettings.setFfmpegPath("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\extension-recorder\\libs\\ffmpeg.exe");
        recorderSettings.setOutputPath("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\extension-recorder\\out");
        recorderSettings.setOutputFileName("test.flv");
        }))
        .onEvent(TikTokLiveRecorderStartedEvent.class, (liveClient, event) ->
        {
        System.out.println(event.getDownloadData().getFullUrl());
        })
        .buildAndConnect();

        }
```