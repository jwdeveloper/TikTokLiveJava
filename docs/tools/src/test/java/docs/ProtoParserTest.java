/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package docs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProtoParserTest {

    static final Path REPO = Path.of(System.getProperty("repo.root")).toAbsolutePath().normalize();

    @Test
    void parsesMessagesEnumsAndNested() {
        var f = ProtoParser.parse(REPO.resolve("API/src/main/proto/webcast.proto"));
        assertEquals("webcast", f.name());
        assertEquals("TikTok", f.pkg());
        assertEquals("io.github.jwdeveloper.tiktok.api.messages.webcast", f.javaPackage());
        assertTrue(f.messages().stream().anyMatch(m -> m.name().equals("WebcastChatMessage")));
        assertTrue(f.messages().stream().anyMatch(m -> !m.nested().isEmpty()), "a message with nested messages");
        assertTrue(f.messages().stream().anyMatch(m -> !m.nestedEnums().isEmpty()), "a message with nested enums");
        assertTrue(f.messages().stream().allMatch(m -> m.parent() == null), "top-level messages have no parent");
        var pin = f.messages().stream().filter(m -> m.name().equals("WebcastRoomPinMessage")).findFirst().orElseThrow();
        assertTrue(pin.fields().stream().anyMatch(x -> x.name().equals("chat_message") && x.label().equals("oneof")), "oneof member");
        assertTrue(f.messages().stream().noneMatch(m -> m.name().equals("RoomMessage")), "block-commented messages are ignored");
        var enums = ProtoParser.parse(REPO.resolve("API/src/main/proto/enums.proto"));
        assertTrue(enums.messages().isEmpty());
        assertTrue(enums.enums().size() > 90, "enums: " + enums.enums().size());
    }

    @Test
    void parsesRepeatedMapAndOneof(@TempDir Path dir) throws IOException {
        var source = """
                syntax = "proto3";
                package Fixture; // trailing comment
                option java_package = "fixture.gen";
                /* a block comment
                   message Ghost { int32 x = 1; } */
                import "other.proto";

                // A message.
                message Outer {
                  option deprecated = true;
                  reserved 9, 10 to 12;
                  reserved "old";
                  string name = 1;
                  repeated Inner items = 2;
                  map<string, int32> counts = 3 [deprecated = true];
                  optional bool flag = 4;;
                  oneof choice
                  {
                    Inner first = 5;
                    int64 second = 6;
                  }
                  message Inner {
                    Kind kind = 1;
                    enum Kind { UNKNOWN = 0; ODD = -1; }
                  }
                }
                enum Color { RED = 0; GREEN = 1; }
                """;
        var file = dir.resolve("fixture.proto");
        Files.writeString(file, source);
        var f = ProtoParser.parse(file);
        assertEquals("fixture", f.name());
        assertEquals("Fixture", f.pkg());
        assertEquals("notes", ProtoParser.parse("syntax = \"proto3\";", "dir/sub/Notes.proto").name(), "stem from a path");
        assertEquals("inline", ProtoParser.parse("syntax = \"proto3\";", "inline").name(), "a bare label is its own stem");
        assertEquals("fixture.gen", f.javaPackage());
        assertEquals(1, f.messages().size());
        assertEquals(1, f.enums().size());
        assertEquals("Color", f.enums().get(0).name());
        assertEquals(2, f.enums().get(0).values().size());
        assertEquals(1, f.enums().get(0).values().get(1).number());

        var outer = f.messages().get(0);
        assertEquals("Outer", outer.name());
        assertNull(outer.parent());
        var fields = outer.fields();
        assertEquals(6, fields.size(), "fields: " + fields);
        assertEquals(new ProtoField("", "string", "name", 1), fields.get(0));
        assertEquals(new ProtoField("repeated", "Inner", "items", 2), fields.get(1));
        assertEquals(new ProtoField("map", "map<string, int32>", "counts", 3), fields.get(2));
        assertEquals(new ProtoField("optional", "bool", "flag", 4), fields.get(3));
        assertEquals(new ProtoField("oneof", "Inner", "first", 5), fields.get(4));
        assertEquals(new ProtoField("oneof", "int64", "second", 6), fields.get(5));

        assertEquals(1, outer.nested().size());
        var inner = outer.nested().get(0);
        assertEquals("Inner", inner.name());
        assertEquals("Outer", inner.parent());
        assertEquals(1, inner.nestedEnums().size());
        var kind = inner.nestedEnums().get(0);
        assertEquals("Kind", kind.name());
        assertEquals(new ProtoEnumValue("ODD", -1), kind.values().get(1));
    }
}
