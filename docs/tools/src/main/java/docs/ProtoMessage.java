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
