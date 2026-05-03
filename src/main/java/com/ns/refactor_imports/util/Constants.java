package com.ns.refactor_imports.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {
    private Constants() {}
    public static String JAVA_PREFIX = "java.";
    public static String JAVAX_PREFIX = "javax.";
    public static String JAKARTA_PREFIX = "jakarta.";
    public static String SUN_PREFIX = "sun.";
    public static String PACKAGE_SUN_PREFIX = "com.sun.";

    public static final Pattern PACKAGE_PATTERN = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);
    public static final Pattern CLASS_NAME_PATTERN = Pattern.compile("\\b(?:class|interface|enum|record|@interface)\\s+([a-zA-Z0-9_]+)", Pattern.CASE_INSENSITIVE);
    public static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([a-zA-Z0-9_.]+\\.([a-zA-Z0-9_]+))\\s*;", Pattern.MULTILINE);
    public static final Pattern CODE_REFERENCE_PATTERN = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\.([A-Z][a-zA-Z0-9_]*)\\b");
    public static Set<String> JDK_PREFIXES = Set.of(JAVA_PREFIX, JAVAX_PREFIX, JAKARTA_PREFIX, SUN_PREFIX, PACKAGE_SUN_PREFIX);
}
