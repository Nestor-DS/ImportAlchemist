package com.ns.refactor_imports.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PackageDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(PackageDetectorService.class);

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

    private static final Pattern CLASS_NAME_PATTERN =
            Pattern.compile("(?:public\\s+)?(?:abstract\\s+)?(?:final\\s+)?class\\s+(\\w+)");

    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("import\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

    public Set<String> detectPackagesToRefactor(List<MultipartFile> files) throws IOException {
        Map<String, Set<String>> classToRealPackages = new HashMap<>();
        Set<String> knownClasses = new HashSet<>();

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String declaredPackage = extractDeclaredPackage(content);
            String declaredClassName = extractDeclaredClassName(content);

            if (declaredClassName != null) {
                knownClasses.add(declaredClassName);
                if (declaredPackage != null) {
                    classToRealPackages.computeIfAbsent(declaredClassName, k -> new HashSet<>()).add(declaredPackage);
                }
            }
        }

        Set<String> allPackagesToRefactor = new HashSet<>();

        for (Set<String> packages : classToRealPackages.values()) {
            allPackagesToRefactor.addAll(packages);
        }

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            Matcher importMatcher = IMPORT_PATTERN.matcher(content);
            while (importMatcher.find()) {
                String fullImport = importMatcher.group(1);
                String[] parts = fullImport.split("\\.");
                String className = parts[parts.length - 1];

                if (knownClasses.contains(className)) {
                    String importedPackage = fullImport.substring(0, fullImport.length() - className.length() - 1);
                    allPackagesToRefactor.add(importedPackage);
                }
            }

            Pattern codeReferencePattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*(?:\\.[a-zA-Z_][a-zA-Z0-9_]*)*)\\.([A-Z][a-zA-Z0-9_]*)\\b");

            Matcher codeMatcher = codeReferencePattern.matcher(content);
            while (codeMatcher.find()) {
                String packagePart = codeMatcher.group(1);
                String className = codeMatcher.group(2);

                if (packagePart.startsWith("java.") || packagePart.startsWith("javax.") || packagePart.startsWith("jakarta.")) {
                    continue;
                }

                if (knownClasses.contains(className)) {
                    allPackagesToRefactor.add(packagePart);

                }
            }
        }
        return allPackagesToRefactor;
    }

    private String extractDeclaredPackage(String content) {
        Matcher matcher = PACKAGE_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractDeclaredClassName(String content) {
        Matcher matcher = CLASS_NAME_PATTERN.matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}