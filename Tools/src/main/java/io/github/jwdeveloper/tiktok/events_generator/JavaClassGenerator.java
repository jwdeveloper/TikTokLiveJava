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
package io.github.jwdeveloper.tiktok.events_generator;

import com.squareup.javapoet.*;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import lombok.Getter;

import javax.lang.model.element.Modifier;


public class JavaClassGenerator {
    public String generate(CSharpClassInfo cSharpClassInfo, String packageName, EventGeneratorSettings settings) {

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(cSharpClassInfo.getClassName())
                .addAnnotation(Getter.class);
        if (settings.isTikTokEvent()) {
            classBuilder.superclass(TikTokEvent.class);
        }
        classBuilder.addModifiers(Modifier.PUBLIC);

        // Generate fields
        for (var field : cSharpClassInfo.getFields()) {

            FieldSpec fieldSpec = FieldSpec.builder(ClassName.bestGuess(field.type()), field.name(), Modifier.PRIVATE).build();
            classBuilder.addField(fieldSpec);
        }

        // Generate constructors
        for (var constructor : cSharpClassInfo.getConstructors()) {
            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();

            if(settings.isTikTokEvent())
            {
                constructorBuilder.addStatement("super(msg.getHeader());") ;
            }

            constructorBuilder.addModifiers(Modifier.PUBLIC);
            for (var arg : constructor.arguemtns()) {
                constructorBuilder.addParameter(ClassName.bestGuess(arg.type()), arg.name());
            }
            classBuilder.addMethod(constructorBuilder.build());
        }

        // Generate Java class
        TypeSpec javaClass = classBuilder.build();
        var result = JavaFile.builder(packageName, javaClass).build();
        return result.toString();
    }
}
