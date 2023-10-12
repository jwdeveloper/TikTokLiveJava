package io.github.jwdeveloper.tiktok.mappers;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *  Goal of this class is to map ProtocolBuffer objects to TikTok Event in generic way
 *
 *  First parameter is ProtocolBuffer class type
 *  Second parameters is TikTokEvent class type
 *  Third parameters is bytes payload
 *
 *    mapToEvent(WebcastGiftMessage.class, TikTokGiftEvent.class, payload)
 *
 *    How does it work?
 *    1. Finds method `parseFrom(byte[] bytes)` inside ProtocolBuffer class
 *    2. put payload to the method methods and create new instance of ProtcolBuffer object
 *    3. Finds in TikTokEvent constructor that takes ProtocolBuffer type as parameter
 *    4. create new Instance in TikTokEvents using object from step 2 and constructor from step 3
 *
 *    methodCache and constructorCache are used to boost performance
 */
public class TikTokGenericEventMapper {

    private record TypePair(Class<?> a, Class<?> b) {
    }

    private final Map<Class<?>, Method> methodCache;
    private final Map<TypePair, Constructor<?>> constructorCache;

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

    private Method getParsingMethod(Class<?> input) throws NoSuchMethodException {
        if (methodCache.containsKey(input)) {
            return methodCache.get(input);
        }
        var method = input.getDeclaredMethod("parseFrom", byte[].class);
        methodCache.put(input, method);
        return method;
    }

    private Constructor<?> getParsingConstructor(Class<?> input, Class<?> output) {
        var pair = new TypePair(input, output);
        if (constructorCache.containsKey(pair)) {
            return constructorCache.get(pair);
        }

        var optional = Arrays.stream(output.getConstructors())
                .filter(ea -> Arrays.stream(ea.getParameterTypes())
                        .toList()
                        .contains(input))
                .findFirst();

        if (optional.isEmpty()) {
            throw new TikTokMessageMappingException(input, output, "Unable to find constructor with input class type");
        }

        constructorCache.put(pair, optional.get());
        return optional.get();
    }


}
