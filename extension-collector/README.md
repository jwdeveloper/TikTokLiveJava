Collects live data to mongodb database


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
            <artifactId>extension-collector</artifactId>
            <version>1.1.0-Release</version>
        </dependency>
    </dependencies>
```


Usage

```java
    public static void main(String[] args) throws IOException {

        var collector = TikTokLiveCollector.use(settings ->
        {
        settings.setConnectionUrl("mongodb+srv://" + mongoUser + ":" + mongoPassword + "@" + mongoDatabase + "/?retryWrites=true&w=majority");
        settings.setDatabaseName("tiktok");
        });
        collector.connectDatabase();

        var users = List.of("tehila_723", "dino123597", "domaxyzx", "dash4214", "obserwacje_live");
        var sessionTag = "Tag1";
        for (var user : users) {
        TikTokLive.newClient(user)
        .configure(liveClientSettings ->
        {
        liveClientSettings.setPrintToConsole(true);
        })
        .onError((liveClient, event) ->
        {
        event.getException().printStackTrace();
        })
        .addListener(collector.newListener(Map.of("sessionTag", sessionTag), document ->
        {
        if (document.get("dataType") == "message") {
        return false;
        }
        return true;
        }))
        .buildAndConnectAsync();
        }

        System.in.read();
        collector.disconnectDatabase();
        }
```