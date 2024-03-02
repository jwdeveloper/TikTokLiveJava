package io.github.jwdeveloper.tiktok.extension.collector.api;

import io.github.jwdeveloper.tiktok.live.LiveClient;
import org.bson.Document;

public interface CollectorEvent {
    boolean execute(LiveClient client, Document document);
}
