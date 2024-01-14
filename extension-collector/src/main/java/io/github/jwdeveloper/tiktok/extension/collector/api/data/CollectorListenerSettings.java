package io.github.jwdeveloper.tiktok.extension.collector.api.data;

import lombok.Data;
import org.bson.Document;

import java.util.Map;
import java.util.function.Function;

@Data
public class CollectorListenerSettings {
    private Map<String, Object> extraFields;
    private Function<Document, Boolean> filter;
}
