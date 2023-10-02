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
package io.github.jwdeveloper.tiktok.intefacee;

import com.squareup.javapoet.*;
import io.github.jwdeveloper.tiktok.TikTokLiveClientBuilder;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events_generator.EventGeneratorSettings;
import org.reflections.Reflections;

import javax.lang.model.element.Modifier;
import java.util.Set;

public class EventsInterfaceGenerator {
    public void compile(EventGeneratorSettings settings) {
        Reflections reflections = new Reflections("io.github.jwdeveloper.tiktok.events.messages");

        // Get all types (i.e., classes) in the specified package
        var classes = reflections.getSubTypesOf(TikTokEvent.class);
        classes.add(TikTokEvent.class);

    // var result = generateInterface("io.github.jwdeveloper.tiktok.events", classes);System.out.println(result);
     var result =  getBuilderImplementation("x",classes); System.out.println(result);

    }

    public String generateInterface(String packageName, Set<Class<? extends TikTokEvent>> eventsClasses) {

        TypeSpec.Builder classBuilder = TypeSpec.interfaceBuilder("TikTokEventBuilder");
        classBuilder.addModifiers(Modifier.PUBLIC);
        classBuilder.addTypeVariable(TypeVariableName.get("T"));

        // Generate constructors
        for (var clazz : eventsClasses) {
            var clazzName = clazz.getSimpleName();

            var methodName = clazzName;
            methodName = clazzName.replace("TikTok", "");
            if(!clazz.equals(TikTokEvent.class))
            {
                methodName = methodName.replace("Event", "");
            }
            MethodSpec.Builder constructorBuilder = MethodSpec.methodBuilder("on" + methodName);


            var name = "TikTokEventConsumer<" + clazzName + ">";
            constructorBuilder.addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC);
            constructorBuilder.addParameter(ClassName.bestGuess(name), "event");
            constructorBuilder.returns(TypeVariableName.get("T"));
            classBuilder.addMethod(constructorBuilder.build());

        }



        // Generate Java class
        TypeSpec javaClass = classBuilder.build();
        var result = JavaFile.builder(packageName, javaClass).build();
        return result.toString();
    }



    public String getBuilderImplementation(String packageName, Set<Class<? extends TikTokEvent>> eventsClasses) {

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder("TikTokEvents");
        classBuilder.addModifiers(Modifier.PUBLIC);

        /*
  public TikTokClientBuilder onLinkMicFanTicket(Consumer<TikTokLinkMicFanTicketEvent> event) {
        tikTokEventHandler.subscribe(TikTokEventHandler.class, event);
        return this;
    }


         */

        // Generate constructors
        for (var clazz : eventsClasses) {
            var clazzName = clazz.getSimpleName();
            var methodName = clazzName;
            methodName = clazzName.replace("TikTok", "");
            if(!clazz.equals(TikTokEvent.class))
            {
                methodName = methodName.replace("Event", "");
            }
            methodName ="on" + methodName;
            MethodSpec.Builder constructorBuilder = MethodSpec.methodBuilder( methodName);


            var name = "TikTokEventConsumer<" + clazzName + ">";
            constructorBuilder.addModifiers( Modifier.PUBLIC);
            constructorBuilder.addParameter(ClassName.bestGuess(name), "event");
            constructorBuilder.addStatement("tikTokEventHandler.subscribe("+clazzName+".class,event)");
            constructorBuilder.addStatement("return this");
            constructorBuilder.returns(TikTokLiveClientBuilder.class);
            classBuilder.addMethod(constructorBuilder.build());

        }

        // Generate Java class
        TypeSpec javaClass = classBuilder.build();
        var result = JavaFile.builder(packageName, javaClass).build();
        return result.toString();
    }
}
