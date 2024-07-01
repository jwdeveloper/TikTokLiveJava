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
import io.github.jwdeveloper.tiktok.utils.ProtoBufferObject;

public interface LiveMapperHelper {

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
