package com.ns.refactor_imports.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public final class JavaSourceParser {

    public static @Nullable String extractPackage(String content) {
        Matcher matcherPackage = Constants.PACKAGE_PATTERN.matcher(content);
        return matcherPackage.find() ? matcherPackage.group(1) : null;
    }

    public static @Nullable String extractClassName(String content) {
        Matcher matcherClassName = Constants.CLASS_NAME_PATTERN.matcher(content);
        return matcherClassName.find() ? matcherClassName.group(1) : null;
    }

    public static @NonNull List<ImportEntry> extractImports(String content) {
        List<ImportEntry> result = new ArrayList<>();
        Matcher matcherImport = Constants.IMPORT_PATTERN.matcher(content);
        while (matcherImport.find()) {
            result.add(new ImportEntry(matcherImport.group(1), matcherImport.group(2)));
        }
        return result;
    }

    public record ImportEntry(String fullPath, String simpleName) {}

    public record CodeReference(String qualifier, String className) {
        public boolean isJdkPackage() {
            return qualifier.startsWith(Constants.JAVA_PREFIX) || qualifier.startsWith(Constants.JAVAX_PREFIX) || qualifier.startsWith(Constants.JAKARTA_PREFIX);
        }
    }
}
