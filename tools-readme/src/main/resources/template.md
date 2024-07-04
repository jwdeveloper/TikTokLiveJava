<div align="center" >
<a target="blank" >
<img src="https://raw.githubusercontent.com/jwdeveloper/TikTokLiveJava/develop-1_0_0/Tools-ReadmeGenerator/src/main/resources/logo.svg" width="15%" >
</img>
</a>
</div>
<div align="center" >
<h1>TikTok Live Java</h1>

❤️❤️🎁 *Connect to TikTok live in 3 lines* 🎁❤️❤️

<div align="center" >
<a href="https://jitpack.io/#jwdeveloper/TikTok-Live-Java" target="blank" >
<img src="https://jitpack.io/v/jwdeveloper/TikTok-Live-Java.svg" width="20%" >
</img>
</a>


<a href="https://discord.gg/e2XwPNTBBr" target="blank" >
<img src="https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white" >
</img>
</a>

<a target="blank" >
<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" >
</img>
</a>
</div>
</div>

# Introduction
A Java library inspired by [TikTokLive](https://github.com/isaackogan/TikTokLive) and [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp). Use it to receive live stream events such as comments and gifts in realtime from [TikTok LIVE](https://www.tiktok.com/live) by connecting to TikTok's internal WebCast push service. 
The library includes a wrapper that connects to the WebCast service using just the username (`uniqueId`). This allows you to connect to your own live chat as well as the live chat of other streamers. 
No credentials are required. Events such as [Members Joining](#member), [Gifts](#gift), [Subscriptions](#subscribe), [Viewers](#roomuser), [Follows](#social), [Shares](#social), [Questions](#questionnew), [Likes](#like) and [Battles](#linkmicbattle) can be tracked. 

<div align="center">
  <a href="https://www.youtube.com/watch?v=eerWGgUKc6c" align="right" target="blank"><img src="https://img.youtube.com/vi/eerWGgUKc6c/hqdefault.jpg" alt="IMAGE ALT TEXT" width="38%" align="right"></a>
</div>

Join the support [discord](https://discord.gg/e2XwPNTBBr) and visit the `#java-support` channel for questions, contributions and ideas. Feel free to make pull requests with missing/new features, fixes, etc

Do you prefer other programming languages?
- **Node** orginal: [TikTok-Live-Connector](https://github.com/isaackogan/TikTok-Live-Connector) by [@zerodytrash](https://github.com/zerodytrash)
- **Python** rewrite: [TikTokLive](https://github.com/isaackogan/TikTokLive) by [@isaackogan](https://github.com/isaackogan)
- **Go** rewrite: [GoTikTokLive](https://github.com/Davincible/gotiktoklive) by [@Davincible](https://github.com/Davincible)
- **C#** rewrite: [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp) by [@frankvHoof93](https://github.com/frankvHoof93)

**NOTE:** This is not an official API. It's a reverse engineering project.

#### Overview
- [Getting started](#getting-started)
- [Events](#events)
- [Listeners](#listeners)
- [Contributing](#contributing)

## Getting started

1. Install the package via Maven

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
            <version>{{version}}</version>
            <scope>compile</scope>
        </dependency>
   </dependencies>
```

2. Create your first chat connection

{{code-content}}

## Events


@{events-content}

{{for item of data}} 

{{if item is 2}}

{{else}}

{{end}}

{{end}}
<br>

## Listeners
```java
{{listener-content}}
```

## Contributing
Your improvements are welcome! Feel free to open an <a href="https://github.com/jwdeveloper/TikTok-Live-Java/issues">issue</a> or <a href="https://github.com/jwdeveloper/TikTok-Live-Java/pulls">pull request</a>.
