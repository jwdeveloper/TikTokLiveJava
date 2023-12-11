package io.github.jwdeveloper.tiktok.utils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.util.Base64;

public class ProtocolUtils {
    public static String toBase64(byte[] bytes) {

        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String toBase64(WebcastResponse.Message bytes) {
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }


    public static byte[] fromBase64(String base64) {

        return Base64.getDecoder().decode(base64);
    }

    public static ProtoBufferObject getProtocolBufferStructure(byte[] bytes) {

        try {
            var files = UnknownFieldSet.parseFrom(bytes);
            var protoBufferObject = new ProtoBufferObject();
            createStructure(files, protoBufferObject);
            return protoBufferObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void createStructure(UnknownFieldSet unknownFieldSet, ProtoBufferObject root) throws InvalidProtocolBufferException {
        for (var entry : unknownFieldSet.asMap().entrySet()) {
            var objectValue = entry.getValue();
            var type = "undefind";
            Object value = null;
            var index = entry.getKey();
            if (!objectValue.getLengthDelimitedList().isEmpty()) {
                var nestedObject = new ProtoBufferObject();
                for (var str : objectValue.getLengthDelimitedList()) {
                    try {
                        var nestedFieldsSet = UnknownFieldSet.parseFrom(str);
                        createStructure(nestedFieldsSet, nestedObject);
                    } catch (Exception e)
                    {
                        type = "string";
                        value = str.toStringUtf8();
                    }
                }
                if (value != null) {
                    root.addField(index, "string", value);
                } else {
                    root.addField(index, "object", nestedObject);
                }
                continue;
            }

            if (!objectValue.getFixed32List().isEmpty()) {
                type = "Fixed32List";
                value = objectValue.getFixed32List();
            }

            if (!objectValue.getFixed64List().isEmpty()) {
                type = "Fixed64List";
                value = objectValue.getFixed64List();
            }

            if (!objectValue.getGroupList().isEmpty()) {
                type = "getGroupList";
                value = objectValue.getGroupList();
            }

            if (!objectValue.getVarintList().isEmpty()) {
                type = "int";
                value = objectValue.getVarintList().get(0);
            }

            root.addField(index, type, value);
        }
    }

    public static WebcastResponse.Message fromBase64ToMessage(String base64) throws InvalidProtocolBufferException {
        var bytes = fromBase64(base64);
        return WebcastResponse.Message.parseFrom(bytes);
    }


}
