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
package io.github.jwdeveloper.tiktok.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import io.github.jwdeveloper.tiktok.data.dto.MessageMetaData;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

public class JsonUtil {
    public static String toJson(Object obj) {
        return new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes)
                    {

                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass)
                    {
                        if(aClass.equals(Image.class))
                        {
                            return true;
                        }
                        if(aClass.equals(MessageMetaData.class))
                        {
                            return true;
                        }

                        return false;
                    }
                })
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
                .toJson(obj);
    }

    public static String messageToJson(Object obj) {

        var ignoredFields = new ArrayList<String>();
        ignoredFields.add("memoizedIsInitialized");
        ignoredFields.add("memoizedSize");
        ignoredFields.add("memoizedHashCode");

        return new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes fieldAttributes) {

                        if (ignoredFields.contains(fieldAttributes.getName())) {
                            return true;
                        }

                        if (fieldAttributes.getName().equals("common_")) {
                        //    return true;
                        }
                        if (fieldAttributes.getName().equals("bytes")) {
                            return true;
                        }
                        if (fieldAttributes.getName().equals("unknownFields")) {
                            return true;
                        }

                        return false;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> aClass) {
                        return false;
                    }
                })
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create()
                .toJson(obj);
    }
}
