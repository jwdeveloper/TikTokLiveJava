# Collaboration Guide

Are you willing to help or improve TikTokLiveJava? 




### Project setup

1. Clone project to your favorite IDE (IntelliJ recommended) https://github.com/jwdeveloper/TikTokLiveJava.git

2. After project is cloned you can encounter error that some classes are missing
   Don't worry this is normal! To fix that use `Maven compile` command for the
   root project



### How does library works?

  We can divide working of library to 4 important parts

   - Getting info about live from TikTok. Library is making 3 https requests
     - first for getting live `Room_ID`
     - second for getting more specific live metadata such as live title, host name...
     - third to `Sign API` that returns access token that is later use for connecting 
       to TikTok websocket
   - Connecting to TikTok websocket (PushServer)
     
        After successful connection to TikTok, `pushServer` starts to send `ProtocolBuffer`
        messages in binary format. This is very important to understand `ProtocolBuffer`. Don't worry it is not complicated :).
        All the proto files are included under `API/src/main/proto` After using `Maven compile` command on project, java classes are generated from 
        those files. so then we can easily map incoming bytes to classes, for examples
        `WebcastGiftMessage message = WebcastGiftMessage.parseFrom(incomingBytesArray)`
        
   - Mapping TikTok data to events
     
      At this point we have TikTok data inside protocol-buffer classes now we want
      to map it to events. Why? because `protocol-buffer classes` might be changed at any point,
      but we want to keep library code structure consistent across versions.
      so for example  `WebcastGiftMessage` is mapped to `TikTokGiftEvent`
       
   - trigger events
     
      When the events objects are done last step is to trigger them. And that's it!
      `tikTokEventObserver.publish(liveClient, tiktokGiftEvent)`
  


### Project structure 
   Project is made from few modules the most important one are

  #### API
        
   Contains interfaces and data classes. All code included in this 
   project is ment to be visible to people that are using library.
   

   - All the events can be found user `io.github.jwdeveloper.tiktok.data.events` 
   - All the class data that are used in events is under `io.github.jwdeveloper.tiktok.data.models`
   - All the protocol-buffer classes will be generated at namespack `io.github.jwdeveloper.tiktok.messages` they are at location `API\target\classes\io\github\jwdeveloper\tiktok\messages`

  #### Client

   Contains implementation of `API` modules interfaces and all the code
   important classes

   - `TikTokLiveClient` core class that is use to mangae connection disconnection
   - `TikTokLiveClientBuilder` preparing and creating `TikTokLiveClient` class
   - `TikTokApiService` use for Http requests to TikTok/Sign API 
   - `TikTokWebSocketClient` receiving all ProtocolBuffer messages from TikTok
   - `TikTokMessageHandler` **heart of library** it finds suitable mapper for incoming data and triggers its mapping handler as result list of events
      is created and published. **check out** `TikTokMessageHandler.handleSingleMessage`
   - `TikTokMessageHandlerRegistration` register all mappings `protol-buffer` classes -> `events`
   - `TikTokEventObserver` used to register and trigger TikTok events


  ### There are also few more modules made purely for testing and debbuging code

  #### Examples
   Project is made to show up new features and present basic 
   example of library. While developing you can use it this project as playground 
   
  #### Tools
   Project that contains code generators.
   The most useful one is class `GenerateGiftsEnum` that download gifts json from TikTok
   and generates code for `Gift` enum that is later added to `API` module at path `io.github.jwdeveloper.tiktok.data.models.gifts.Gift`
   
  #### Tools-EventsCollector
   Tool that can be used to store all `protocol-buffer` and `events` from live to `sqlLite` database or `Json` file
   It is very handy for later debuging `protocol-buffer` and `events` data
   
  #### Tools-EventsWebViewer
   Tools that runs website that collects and display pure data from TikTok
   very useful for debuging
   
  #### Tools-ReadmeGenerator
   Generates readme file from template




### How to add new Event?

First step is to create class that represends event. Remember, all the events classes must be located in the `io.github.jwdeveloper.tiktok.data.events` package

```java
package io.github.jwdeveloper.tiktok.data.events;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.data.User;
import lombok.Data;


@Data //lombok annotation
@EventMeta(eventType = EventType.Message) //this annotation is used by readme generater code
public class CustomEvent extends TikTokHeaderEvent
{
    private final User user;
    private final String title;

    public CustomEvent(User user,String title)
    {
         this.user = user;
         this.title = title;
    }
}
```
Now we can jump to the `io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration` class. It is used 
to define mappings from incoming protocolbuffer data to Events. 
Note that all classes that starts with `Webcast` represents protocolbuffer data that is coming from tiktok
Note all `Webcast` classes are generated from `proto` file that is defined in `API/src/main/proto/webcast.proto` I recommand to use `protocolbuffer` plugin for inteliji  


For this example we registered new mapping that is triggered every time `WebcastGiftMessage` is comming 
from TikTok. 

```java
 public void init() {

        registerMapping(WebcastGiftMessage.class, bytes ->
        {
            try {
                WebcastGiftMessage tiktokData = WebcastGiftMessage.parseFrom(bytes);

                io.github.jwdeveloper.tiktok.messages.data.User tiktokProtocolBufferUser = tiktokData.getUser();
                io.github.jwdeveloper.tiktok.data.models.users.User tiktokLiveJavaUser = User.map(tiktokProtocolBufferUser);

                return new CustomEvent(tiktokLiveJavaUser, "hello word");
            } catch (Exception e) {
                throw new TikTokLiveException("Unable to parse our custom event", e);
            }
        });
         
        //ConnectionEvents events
        registerMapping(WebcastControlMessage.class, this::handleWebcastControlMessage);

        //Room status events
        registerMapping(WebcastLiveIntroMessage.class, roomInfoHandler::handleIntro);
        registerMapping(WebcastRoomUserSeqMessage.class, roomInfoHandler::handleUserRanking);

        registerMapping(WebcastCaptionMessage.class, TikTokCaptionEvent.class);
        //... more mappings down there
}
```
![image](https://github.com/jwdeveloper/TikTokLiveJava/assets/79764581/b4e410c9-c363-43ed-a0c0-8220ed50a387)



Next step is to open `TikTokLiveClientBuilder` and add method for handling our new event

``` java

  public LiveClientBuilder onCustomEvent(EventConsumer<CustomEvent> event) {
        tikTokEventHandler.subscribe(CustomEvent.class, event);
        return this;
    }

```
![image](https://github.com/jwdeveloper/TikTokLiveJava/assets/79764581/b22d2044-d565-4b2d-944b-df6a6b75083a)



To make `onCustomEvent` method visible from `TikTokLive.newClient("asds").onCustomEvent()` we 
need to also include it to interface `EventsBuilder`

``` java
 T onCustomEvent(EventConsumer<CustomEvent> event);
```

![image](https://github.com/jwdeveloper/TikTokLiveJava/assets/79764581/547f5d16-83fa-48ab-909e-993bf9af1a8e)








Finally we are good to go, our event has been included!

![image](https://github.com/jwdeveloper/TikTokLiveJava/assets/79764581/36ad6f1f-b38c-4cf7-93bd-b4cc0638cba0)