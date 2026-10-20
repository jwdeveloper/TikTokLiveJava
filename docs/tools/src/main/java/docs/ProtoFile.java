package docs;

import java.util.List;

/**
 * One parsed {@code .proto} file.
 *
 * @param pkg         the {@code package} statement, or an empty string
 * @param javaPackage the {@code java_package} option, or an empty string
 * @param messages    top-level messages in declaration order
 * @param enums       top-level enums in declaration order
 */
record ProtoFile(String pkg, String javaPackage, List<ProtoMessage> messages, List<ProtoEnum> enums) {}
