package io.github.jwdeveloper.tiktok.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class ProtoBufferJsonGenerator {
    public static JsonObject genratejson(ProtoBufferObject protoBuffObj) {

        JsonObject jsonObject = new JsonObject();
        for (var entry : protoBuffObj.getFields().entrySet()) {
            var fieldName = entry.getKey() + "_" + entry.getValue().type;
            if (entry.getValue().value instanceof ProtoBufferObject protoObj)
            {
                JsonObject childJson = genratejson(protoObj);
                jsonObject.add(fieldName, childJson);
                continue;
            }

            var value = entry.getValue().value.toString();
            jsonObject.addProperty(fieldName, value);
        }

        return jsonObject;
    }

    public static String generate(ProtoBufferObject protoBufferObject) {
        var json = genratejson(protoBufferObject);
        var gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }
}
