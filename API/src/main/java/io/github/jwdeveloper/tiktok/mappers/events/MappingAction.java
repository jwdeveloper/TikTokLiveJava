package io.github.jwdeveloper.tiktok.mappers.events;

import io.github.jwdeveloper.tiktok.mappers.TikTokMapperHelper;

@FunctionalInterface
public interface MappingAction<T> {

    /**
     * @param inputBytes   incoming bytes from TikTok server. The represents protocol buffer message that was send to client
     * @param messageName  name of protocol buffer message
     * @param mapperHelper utils and helper methods that can be use to debbug/display/deserialize protocol buffer data
     * @return
     */
    T onMapping(byte[] inputBytes, String messageName, TikTokMapperHelper mapperHelper);

}
