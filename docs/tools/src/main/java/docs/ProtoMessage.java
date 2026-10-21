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

import java.util.List;

/**
 * A protobuf message.
 *
 * @param name        the simple name
 * @param parent      the qualified name of the enclosing message ({@code Outer} or {@code Outer.Inner}), or null at top level
 * @param fields      fields in declaration order, {@code oneof} members included
 * @param nested      messages declared inside this one
 * @param nestedEnums enums declared inside this one
 */
record ProtoMessage(String name, String parent, List<ProtoField> fields, List<ProtoMessage> nested, List<ProtoEnum> nestedEnums) {

    /** {@code Outer.Inner} for a nested message, the simple name at top level. */
    String qualifiedName() {
        return parent == null ? name : parent + "." + name;
    }
}
