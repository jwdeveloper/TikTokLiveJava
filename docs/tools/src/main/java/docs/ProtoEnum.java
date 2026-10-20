package docs;

import java.util.List;

/** A protobuf enum with its values in declaration order. */
record ProtoEnum(String name, List<ProtoEnumValue> values) {}
