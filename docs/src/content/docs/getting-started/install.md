---
title: Install
description: Add TikTokLiveJava to a Maven or Gradle project from JitPack, pick a version, and check that the dependency resolves.
---

TikTokLiveJava is published on [JitPack](https://jitpack.io/#jwdeveloper/TikTok-Live-Java), which builds the library from the tags of its GitHub repository. Add the JitPack repository to your build, then depend on the `Client` artifact; it brings the `API` module, protobuf, Gson, SLF4J, and Java-WebSocket with it.

## Requirements

- Java 16 or newer. The library is compiled for Java 16.
- Maven or Gradle with access to `https://jitpack.io`.

## Maven

Add the repository and the dependency to `pom.xml`:

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
        <version>1.13.1-Release</version>
    </dependency>
</dependencies>
```

## Gradle

Add the repository and the dependency to `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url = 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jwdeveloper.TikTok-Live-Java:Client:1.13.1-Release'
}
```

If the project declares its repositories in `settings.gradle` under `dependencyResolutionManagement`, add `maven { url 'https://jitpack.io' }` there instead, as the repository's README does.

## Group names

JitPack resolves two group names to the same artifact: `com.github.jwdeveloper.TikTok-Live-Java`, used in the README and above, and `com.github.jwdeveloper.TikTokLiveJava`, the repository's current name. Either works with the same `Client` artifact and version.

## Versions

Releases are tagged `<major>.<minor>.<patch>-Release`. The current release is `1.13.1-Release`, the version in the repository's `pom.xml`; the [GitHub releases](https://github.com/jwdeveloper/TikTokLiveJava/releases) page is the changelog. These docs are generated from the `master` sources, so a method shown here can be newer than the release you depend on; the [API reference](/api/) index names the version it was generated from.

## Check the installation

Build a client without connecting. `build()` validates the settings and wires the client; it makes no network request, so this runs offline and prints `isaackogz`.

```java
var client = TikTokLive.newClient("isaackogz").build();
System.out.println(client.getRoomInfo().getHostName());
```

`TikTokLive` is `io.github.jwdeveloper.tiktok.client.TikTokLive`; the event classes live under `io.github.jwdeveloper.tiktok.api.data.events`. Next, run the example on [Getting Started](/#listen-to-a-livestream), then read the [connection lifecycle](/getting-started/connection/).
