package com.ns.refactor_imports.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PackageDetectorService {

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

    private static final Pattern CLASS_NAME_PATTERN =
            Pattern.compile("(?:public\\s+)?class\\s+(\\w+)");

    private static final Pattern IMPORT_PATTERN =
            Pattern.compile("import\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE);

    private static final Pattern PACKAGE_REFERENCE_PATTERN =
            Pattern.compile("([a-zA-Z][a-zA-Z0-9_]*(?:\\.[a-zA-Z][a-zA-Z0-9_]*)+)");

    public Set<String> detectPackagesToRefactor(List<MultipartFile> files) throws IOException {
        Map<String, Set<String>> classPackageMap = buildClassPackageMap(files);

        Set<String> validImports = extractValidImports(files);

        Set<String> packagesToRefactor = new HashSet<>();

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename();

            Matcher matcher = PACKAGE_REFERENCE_PATTERN.matcher(content);

            while (matcher.find()) {
                String reference = matcher.group(1);

                if (validImports.contains(reference) || reference.startsWith("java.") || reference.startsWith("javax.")) {
                    continue;
                }

                String[] parts = reference.split("\\.");
                String className = parts[parts.length - 1];

                String referencedPackage = String.join(".", Arrays.copyOf(parts, parts.length - 1));
                if (classPackageMap.containsKey(className)) {
                    Set<String> actualPackages = classPackageMap.get(className);

                    if (!actualPackages.contains(referencedPackage)) {

                        if (actualPackages.contains("") && !referencedPackage.isEmpty()) {
                            packagesToRefactor.add(referencedPackage);
                        }

                        else if (!actualPackages.contains(referencedPackage) && !actualPackages.contains("")) {
                            packagesToRefactor.add(referencedPackage);
                        }
                    }
                }
            }
        }

        return packagesToRefactor;
    }

    private Map<String, Set<String>> buildClassPackageMap(List<MultipartFile> files) throws IOException {
        Map<String, Set<String>> classPackageMap = new HashMap<>();

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

            String declaredPackage = extractDeclaredPackage(content);
            String declaredClassName = extractDeclaredClassName(content);

            if (declaredClassName != null) {
                if (declaredPackage != null) {
                    classPackageMap.computeIfAbsent(declaredClassName, k -> new HashSet<>()).add(declaredPackage);
                } else {
                    classPackageMap.computeIfAbsent(declaredClassName, k -> new HashSet<>()).add("");
                }
            }
        }

        return classPackageMap;
    }

    private Set<String> extractValidImports(List<MultipartFile> files) throws IOException {
        Set<String> validImports = new HashSet<>();

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            Matcher matcher = IMPORT_PATTERN.matcher(content);

            while (matcher.find()) {
                validImports.add(matcher.group(1));
            }
        }

        return validImports;
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