package com.ns.refactor_imports.util;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    public String processFile(String content, Set<String> packagesToRefactor,
                              String newPackage, @NonNull Map<String, Set<String>> classPackages) {

        Set<String> knownClasses = classPackages.keySet();

        String result = updatePackageDeclaration(content, packagesToRefactor, newPackage);
        result = updateImports(result, knownClasses, newPackage);
        result = updateCodeReferences(result, knownClasses, newPackage, packagesToRefactor);

        return result;
    }

    private String updatePackageDeclaration(String content, Set<String> packagesToRefactor, String newPackage) {
        String current = JavaSourceParser.extractPackage(content);
        if (current == null || !packagesToRefactor.contains(current)) return content;
        Pattern p = Pattern.compile("^package\\s+" + Pattern.quote(current) + "\\s*;", Pattern.MULTILINE);
        String updated = p.matcher(content).replaceFirst("package " + newPackage + ";");
        logger.info("Package declaration: '{}' -> '{}'", current, newPackage);
        return updated;
    }

    private @NonNull String updateImports(String content, Set<String> knownClasses, String newPackage) {
        Matcher matcher = Constants.IMPORT_PATTERN.matcher(content);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String fullPath   = matcher.group(1);
            String simpleName = matcher.group(2);

            if (knownClasses.contains(simpleName)) {
                String replacement = String.join("", "import ",newPackage,".",simpleName,";");
                logger.info("Import: '{}' -> '{}'", fullPath, replacement);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String updateCodeReferences(String content, Set<String> knownClasses, String newPackage,
                                        @NonNull Set<String> packagesToRefactor) {
        String result = content;

        for (String oldPackage : packagesToRefactor) {
            Pattern p = Pattern.compile("\\b" + Pattern.quote(oldPackage) + "\\.([A-Za-z_][A-Za-z0-9_]*)\\b");
            Matcher m = p.matcher(result);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String className    = m.group(1);
                String replacement  = newPackage + "." + className;
                logger.debug("Code ref (pkg match): '{}.{}' -> '{}'", oldPackage, className, replacement);
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            result = sb.toString();
        }

        for (String className : knownClasses) {
            Pattern p = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\."+ Pattern.quote(className) + "\\b");
            Matcher m = p.matcher(result);
            StringBuilder sb = new StringBuilder();

            while (m.find()) {
                String qualifier = m.group(1);
                if (qualifier.equals(newPackage)) {
                    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                    continue;
                }

                if (new JavaSourceParser.CodeReference(qualifier, className).isJdkPackage()) {
                    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                    continue;
                }

                char firstChar = qualifier.charAt(0);
                if (Character.isLowerCase(firstChar) && !qualifier.contains(".")) {
                    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(0)));
                    continue;
                }

                String replacement = newPackage + "." + className;
                logger.debug("Code ref (class match): '{}' -> '{}'", m.group(0), replacement);
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }
}
