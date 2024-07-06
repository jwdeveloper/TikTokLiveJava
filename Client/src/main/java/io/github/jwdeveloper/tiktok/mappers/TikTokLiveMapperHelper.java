/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.mappers;

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;
import io.github.jwdeveloper.tiktok.utils.ProtoBufferObject;
import io.github.jwdeveloper.tiktok.utils.ProtocolUtils;

public class TikTokLiveMapperHelper implements LiveMapperHelper {

    private static final String PACKAGE_PREFIX = "io.github.jwdeveloper.tiktok.messages.webcast.";

    private final TikTokGenericEventMapper genericMapper;

    public TikTokLiveMapperHelper(TikTokGenericEventMapper genericMapper) {
        this.genericMapper = genericMapper;
    }

    @Override
    public <T extends GeneratedMessageV3> T bytesToWebcastObject(byte[] bytes, Class<T> messageClass) {
        try {
            var parsingMethod = genericMapper.getParsingMethod(messageClass);
            //NULL is passed, since Parsing method is Static
            var sourceObject = parsingMethod.invoke(null, bytes);
            return (T) sourceObject;
        } catch (Exception e) {
            throw new TikTokMessageMappingException(messageClass, "can't find parsing method", e);
        }
    }

    @Override
    public Object bytesToWebcastObject(byte[] bytes, String messageName) {
        try {
            var packageName = PACKAGE_PREFIX + messageName;
            var clazz = Class.forName(packageName);
            return bytesToWebcastObject(bytes, (Class<? extends GeneratedMessageV3>) clazz);
        } catch (Exception e) {
            throw new TikTokMessageMappingException(messageName, e);
        }
    }

    @Override
    public boolean isMessageHasProtoClass(String messageName) {
        try {
            var packageName = PACKAGE_PREFIX + messageName;
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
