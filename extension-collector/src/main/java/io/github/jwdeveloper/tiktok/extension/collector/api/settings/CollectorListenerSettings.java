package io.github.jwdeveloper.tiktok.extension.collector.api.settings;

import io.github.jwdeveloper.tiktok.extension.collector.api.CollectorEvent;
import lombok.Data;
import org.bson.Document;

import java.util.Map;
import java.util.function.Function;

@Data
public class CollectorListenerSettings {
    private Map<String, Object> extraFields;
    private CollectorEvent filter;
}