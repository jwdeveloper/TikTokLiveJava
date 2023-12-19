package io.github.jwdeveloper.tiktok.mappers;

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;
import io.github.jwdeveloper.tiktok.utils.ProtoBufferObject;
import io.github.jwdeveloper.tiktok.utils.ProtocolUtils;

public class TikTokLiveMapperUtils implements TikTokMapperHelper {
    private final TikTokGenericEventMapper genericMapper;

    public TikTokLiveMapperUtils(TikTokGenericEventMapper genericMapper) {
        this.genericMapper = genericMapper;
    }

    @Override
    public <T extends GeneratedMessageV3> T bytesToWebcastObject(byte[] bytes, Class<T> messageClass) {
        try {
            var parsingMethod = genericMapper.getParsingMethod(messageClass);
            var sourceObject = parsingMethod.invoke(null, bytes);
            return (T) sourceObject;
        } catch (Exception e) {
            throw new TikTokMessageMappingException(messageClass, "can't find parsing method", e);
        }
    }

    @Override
    public Object bytesToWebcastObject(byte[] bytes, String messageName) {
        try {
            var packageName = "io.github.jwdeveloper.tiktok.messages.webcast." + messageName;
            var clazz = Class.forName(packageName);
            return bytesToWebcastObject(bytes, (Class<? extends GeneratedMessageV3>) clazz);
        } catch (Exception e) {
            throw new TikTokMessageMappingException(messageName, e);
        }
    }

    @Override
    public boolean isMessageHasProtoClass(String messageName) {
        try {
            var packageName = "io.github.jwdeveloper.tiktok.messages.webcast." + messageName;
            Class.forName(packageName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ProtoBufferObject bytesToProtoBufferStructure(byte[] bytes) {
        return ProtocolUtils.getProtocolBufferStructure(bytes);
    }

    @Override
    public String toJson(Object obj) {
        return JsonUtil.toJson(obj);
    }


}
