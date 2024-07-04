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

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 *  Goal of this class is to map ProtocolBuffer objects to TikTok Event in generic way
 *  <ul>
 *      <li>First parameter is ProtocolBuffer class type</li>
 *      <li>Second parameters is TikTokEvent class type</li>
 *      <li>Third parameters is bytes payload</li>
 *  </ul>
 *  <p>mapToEvent(WebcastGiftMessage.class, TikTokGiftEvent.class, payload)</p>
 *  <p>How does it work?</p>
 *  <ol>
 *      <li>Finds method `parseFrom(byte[] bytes)` inside ProtocolBuffer class</li>
 *      <li>Put payload to the method methods and create new instance of ProtcolBuffer object</li>
 *      <li>Finds in TikTokEvent constructor that takes ProtocolBuffer type as parameter</li>
 *      <li>Create new Instance in TikTokEvents using object from step 2 and constructor from step 3</li>
 *  </ol>
 *   methodCache and constructorCache are used to boost performance
 */
public class TikTokGenericEventMapper {

    private record TypePair(Class<?> a, Class<?> b) {
    }

    private final Map<Class<?>, Method> methodCache;
    private final Map<TypePair, Constructor<?>> constructorCache;
    private static final String PARSE_FIELD = "parseFrom";
    public TikTokGenericEventMapper() {
        this.methodCache = new HashMap<>();
        this.constructorCache = new HashMap<>();
    }

    public TikTokEvent mapToEvent(Class<?> inputClazz, Class<?> outputClass, byte[] payload) {
        try {
            var method = getParsingMethod(inputClazz);
            var deserializedMessage = method.invoke(null, payload);
            var constructor = getParsingConstructor(inputClazz, outputClass);

            var tiktokEvent = constructor.newInstance(deserializedMessage);
            return (TikTokEvent) tiktokEvent;
        } catch (Exception ex) {
            throw new TikTokMessageMappingException(inputClazz, outputClass, ex);
        }
    }

    public Method getParsingMethod(Class<?> input) throws RuntimeException {
        return methodCache.computeIfAbsent(input, aClass -> {
			try {
				return aClass.getDeclaredMethod(PARSE_FIELD, byte[].class);
			} catch (NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		});
    }

    private Constructor<?> getParsingConstructor(Class<?> input, Class<?> output) {
        return constructorCache.computeIfAbsent(new TypePair(input, output), pair -> {
            var optional = Arrays.stream(output.getConstructors())
                .filter(ea -> Arrays.stream(ea.getParameterTypes())
                    .toList()
                    .contains(input))
                .findFirst();

            if (optional.isEmpty())
				throw new TikTokMessageMappingException(input, output, "Unable to find constructor with input class type");
            return optional.get();
        });
    }
}