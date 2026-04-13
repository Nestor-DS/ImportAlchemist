package com.ns.refactor_imports.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    public String processFile(String content, Set<String> packagesToRefactor, String newPackage, Map<String, Set<String>> classPackages) {
        Set<String> knownClasses = classPackages.keySet();

        String updatedContent = updatePackageDeclaration(content, packagesToRefactor, newPackage);

        updatedContent = updateImports(updatedContent, knownClasses, newPackage);

        updatedContent = updateCodeReferencesFixed(updatedContent, knownClasses, newPackage, packagesToRefactor);

        return updatedContent;

    }

    private String updatePackageDeclaration(String content, Set<String> packagesToRefactor, String newPackage) {
        Pattern packagePattern = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

        Matcher matcher = packagePattern.matcher(content);
        if (matcher.find()) {
            String currentPackage = matcher.group(1);

            if (packagesToRefactor.contains(currentPackage)) {
                return matcher.replaceFirst("package " + newPackage + ";");
            }
        }
        return content;
    }

    private String updateImports(String content, Set<String> knownClasses, String newPackage) {

        Pattern importPattern = Pattern.compile("^(import\\s+)([a-zA-Z0-9_.]+)(\\s*;)$", Pattern.MULTILINE);

        Matcher matcher = importPattern.matcher(content);
        StringBuilder resultsImports = new StringBuilder();

        while (matcher.find()) {
            String fullImport = matcher.group(2);
            String[] parts = fullImport.split("\\.");
            String className = parts[parts.length - 1];

            if (knownClasses.contains(className)) {
                String newImport = newPackage + "." + className;
                matcher.appendReplacement(resultsImports, "$1" + Matcher.quoteReplacement(newImport) + "$3");
            } else {
                matcher.appendReplacement(resultsImports, matcher.group(0));
            }
        }
        matcher.appendTail(resultsImports);

        return resultsImports.toString();
    }

    private String updateCodeReferencesFixed(String content, Set<String> knownClasses, String newPackage, Set<String> packagesToRefactor) {

        String result = content;

        for (String className : knownClasses) {
            String escapedClassName = Pattern.quote(className);

            Pattern pattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\." + escapedClassName + "\\b");

            Matcher matcher = pattern.matcher(result);
            StringBuilder resultsReferences = new StringBuilder();

            while (matcher.find()) {
                String fullMatch = matcher.group(0);
                String packagePart = matcher.group(1);

                if (packagePart.startsWith("java.") ||
                        packagePart.startsWith("javax.") ||
                        packagePart.startsWith("jakarta.") ||
                        packagePart.equals(newPackage) ||
                        packagePart.equals("new")) {
                    matcher.appendReplacement(resultsReferences, fullMatch);
                    continue;
                }

                String replacement = newPackage + "." + className;
                matcher.appendReplacement(resultsReferences, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(resultsReferences);
            result = resultsReferences.toString();
        }

        for (String oldPackage : packagesToRefactor) {
            String escapedPackage = Pattern.quote(oldPackage);


            Pattern pattern = Pattern.compile("\\b" + escapedPackage + "\\.([A-Za-z_][A-Za-z0-9_]*)\\b");

            Matcher matcher = pattern.matcher(result);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                String className = matcher.group(1);
                String replacement = newPackage + "." + className;

                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    public String extractDeclaredPackage(String content) {
        Matcher matcher = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE)
                .matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    public String extractDeclaredClassName(String content) {
        Matcher matcher = Pattern.compile("(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)")
                .matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}