# Collaboration Guide

Are you willing to help or improve TikTokLiveJava? 




### Project setup

1. Clone project to your favorite IDE (IntelliJ recommended) https://github.com/jwdeveloper/TikTokLiveJava.git

2. After project is cloned you can encounter error that some classes are missing
   Don't worry this is normal! To fix that use `Maven compile` command for the
   root project



### How does library works?

  We can divide working of library to 4 important parts

   - Getting info about live from TikTok
     Library is making 3 https
     - first for getting live `Room_ID`
     - second for getting more specific live metadata such as live title, host name...
     - third to `Sign API` that returns access token that is later use for connecting 
       to TikTok websocket
   - Connecting to TikTok websocket (PushServer)
        After successful connection TikTok starts to send `ProtocolBuffer`
        messages in binary format. This is very important to understand `ProtocolBuffer`
        it is not complicated :). All the proto files are included under `API/src/main/proto`
        After using `Maven compile` command on project, java classes are generated from 
        those files. so then we can easily map incoming bytes to class, for examples
        `WebcastGiftMessage message = WebcastGiftMessage.parseFrom(incomingBytesArray)`
        
   - Mapping TikTok data to events
      at this point we have TikTok data inside protocol-buffer classes now we want
      to map it to TikTokLiveJava events. Why? because `protocol-buffer classes` might
      be changed at any point, but we want to keep library code structure keep consistent
      so for example  `WebcastGiftMessage` is mapped manually to `TikTokGiftEvent`
       
   - trigger events 
      when the events objects are done last step is to trigger then and that's it
      `tikTokEventObserver.publish(liveClient, tiktokGiftEvent)`
  


### Project structure 
   project is made from few modules the most important one are

  #### API
        
   Contains interfaces and data classes, all code that is ment 
   to be visible and use for the Library user should be included
   in this project

   - All the events can be found user `io.github.jwdeveloper.tiktok.data.events` 
   - All the class data that are used in events is under `io.github.jwdeveloper.tiktok.data.models`
 

  #### Client

   Contains implementation of `API` modules interfaces and all the code
   important classes

   - `TikTokLiveClient` core class that is use to connect/disconnect from TikTok
   - `TikTokLiveClientBuilder` preparing `TikTokLiveClient` class
   - `TikTokApiService` use for Http requests to TikTok/Sign API 
   - `TikTokWebSocketClient` receiving all ProtocolBuffer messages from TikTok
   - `TikTokMessageHandlerRegistration` register all mappings TikTok data -> TikTokLiveJava events
   - `TikTokEventObserver` used to register and trigger TikTok events

  There are also few more modules made purely for testing and debbuging code

  #### Examples
   Project made to show up new features and present basic 
   example for library. While developing new features you 
   can use it as playground 
   
  #### Tools
   Project that contains code generators for automation teadios boilder plate
   It contains very useful class `GenerateGiftsEnum` that download gifts json from TikTok
   and generates code for `Gift` enum that is later added to `API` module at path `io.github.jwdeveloper.tiktok.data.models.gifts.Gift`
   
  #### Tools-EventsCollector
   Tool that can be used to store all events from live to sqlLite database or Json file
   It is very handy for later debuging events data
   
  #### Tools-EventsWebViewer
   Tools that runs website that collects and display pure data from TikTok incoming events
   very useful for debuging
   
  #### Tools-ReadmeGenerator
   Generates readme file from template