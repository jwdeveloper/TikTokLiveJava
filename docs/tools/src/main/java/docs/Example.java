package docs;

/** One {@code ```java} fence from an authored page: {@code label} is "{@code <page>, example <n>}", {@code code} the dedented body. */
record Example(String label, String code) {}
