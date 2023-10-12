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
package io.github.jwdeveloper.tiktok.gifts;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.gifts.downloader.GiftDto;
import lombok.Getter;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class GenerateGiftsEnum {

    public static void main(String args[]) throws IOException {


        var downloader = new GiftsDownloader();
        var gifts = downloader.getGiftsFromFile();
        for(var link : gifts)
        {
            System.out.println(link.getImage());
        }
        var groupedByName = gifts.stream().collect(Collectors.groupingBy(GiftDto::getName));
        System.out.println("Total gifts" + gifts.size());
        var result = generate(groupedByName);
        result.writeTo(new File("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java"));
        System.out.println("DONE");
    }


    public static JavaFile generate(Map<String, List<GiftDto>> giftInfoMap) {
        var enumBuilder = TypeSpec.enumBuilder("Gift")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Getter.class)
                .addField(int.class, "id", Modifier.PRIVATE, Modifier.FINAL)
                .addField(String.class, "name", Modifier.PRIVATE, Modifier.FINAL)
                .addField(int.class, "diamondCost", Modifier.PRIVATE, Modifier.FINAL)
                .addField(Picture.class, "picture", Modifier.PRIVATE, Modifier.FINAL);

        var constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(int.class, "id")
                .addParameter(String.class, "name")
                .addParameter(int.class, "diamondCost")
                .addParameter(String.class, "pictureLink")
                .addStatement("this.id = id")
                .addStatement("this.name = name")
                .addStatement("this.diamondCost = diamondCost")
                .addStatement("this.picture = new Picture(pictureLink)")
                .build();

        var inRangeMethod = MethodSpec.methodBuilder("hasDiamondCostRange")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "minimalCost")
                .addParameter(int.class, "maximalCost")
                .addStatement(" return diamondCost >= minimalCost && diamondCost <= maximalCost")
                .returns(boolean.class);

        var hasCostMethod = MethodSpec.methodBuilder("hasDiamondCost")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(int.class, "cost")
                .addStatement(" return diamondCost == cost")
                .returns(boolean.class);

        enumBuilder.addMethod(inRangeMethod.build());
        enumBuilder.addMethod(hasCostMethod.build());
        enumBuilder.addMethod(constructor);


        enumBuilder.addEnumConstant("UNDEFINED", addGift(-1, "undefined", -1, ""));
        for (var giftInfo : giftInfoMap.entrySet()) {


            var name = giftInfo.getKey().replace(" ", "_")
                    .replace("’", "_")
                    .replace("+", "_")
                    .replace("'", "_")
                    .replace(".", "_")
                    .replace("-", "_")
                    .replace("!","_")
                    .toUpperCase();

            if (isNumeric(name)) {
                name = "_" + name;
            }

            if (name.equalsIgnoreCase("")) {
                continue;
            }
            var contier = 1;
            for (var value : giftInfo.getValue()) {
                var enumName = name;
                if (contier > 1) {
                    enumName += "_" + value.getId();
                }
                enumBuilder.addEnumConstant(enumName, addGift(value.getId(), value.getName(), value.getDiamondCost(), value.getImage()));
                contier++;
            }


        }
        var output = JavaFile.builder("io.github.jwdeveloper.tiktok.data.models.gifts", enumBuilder.build());
        output.addFileComment("This enum is generated");
        return output.build();
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static TypeSpec addGift(int id, String name, int diamond, String picture)
    {
        return TypeSpec.anonymousClassBuilder(
                        "$L, $S, $L, $S",
                        id,
                        name,
                        diamond,
                        picture)
                .build();
    }

}
