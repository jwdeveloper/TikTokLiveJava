package docs;

/**
 * A message field.
 *
 * @param label  {@code "repeated"}, {@code "optional"}, {@code "map"}, {@code "oneof"} (a member of a {@code oneof} group)
 *               or an empty string for a plain singular field
 * @param type   the type as written ({@code int64}, {@code Image}, {@code Outer.Inner}), or {@code map<K, V>} for maps
 * @param name   the field name
 * @param number the field number
 */
record ProtoField(String label, String type, String name, int number) {}
