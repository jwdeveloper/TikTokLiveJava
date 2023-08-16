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
