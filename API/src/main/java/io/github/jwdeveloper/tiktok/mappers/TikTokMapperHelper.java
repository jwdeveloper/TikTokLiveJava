package io.github.jwdeveloper.tiktok.mappers;

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.utils.ProtoBufferObject;

public interface TikTokMapperHelper {

    /**
     * @param bytes        protocol buffer data bytes
     * @param messageClass class that we want to serialize bytes to
     * @param <T>          @messageClass must be class that is made by protocol buffer
     * @return object of type @messageClass
     */
    <T extends GeneratedMessageV3> T bytesToWebcastObject(byte[] bytes, Class<T> messageClass);

    /**
     * @param bytes       protocol buffer data bytes
     * @param messageName class that we want to serialize bytes to
     * @return protocol buffer objects if class for @messageName has been found
     * @throws TikTokMessageMappingException if there is no suitable class for messageName
     */
    Object bytesToWebcastObject(byte[] bytes, String messageName);


    /**
     * @param messageName checks wheaten TikTokLiveJava has class representation for certain protocol-buffer message name
     * @return false if class is not found
     */
    boolean isMessageHasProtoClass(String messageName);

    /**
     * @param bytes protocol buffer data bytes
     * @return tree structure of protocol buffer object
     * @see ProtoBufferObject
     */
    ProtoBufferObject bytesToProtoBufferStructure(byte[] bytes);

    /**
     * Converts object to json
     *
     * @param obj any object
     * @return pretty formatted json
     */
    String toJson(Object obj);
}
