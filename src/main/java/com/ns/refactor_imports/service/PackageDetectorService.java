package com.ns.refactor_imports.service;

import com.ns.refactor_imports.dto.DetectionResult;
import com.ns.refactor_imports.util.Constants;
import com.ns.refactor_imports.util.JavaSourceParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PackageDetectorService {

    private static final Logger logger = LoggerFactory.getLogger(PackageDetectorService.class);

    public DetectionResult detect(Collection<String> fileContents) {

        Set<String> knownClasses   = new HashSet<>();
        Set<String> candidates     = new HashSet<>();
        Set<String> namespaceRoots = new HashSet<>();
        Map<String, Set<String>> classPackages = new HashMap<>();

        int skipped = 0;

        for (String content : fileContents) {
            String pkg = JavaSourceParser.extractPackage(content);
            String cls = JavaSourceParser.extractClassName(content);

            if (StringUtils.isBlank(cls)) {
                skipped++;
                continue;
            }

            knownClasses.add(cls);
            classPackages.computeIfAbsent(cls, k -> new HashSet<>()).add(StringUtils.defaultString(pkg));

            if (StringUtils.isNotBlank(pkg)) {
                candidates.add(pkg);
                namespaceRoots.add(topLevelSegment(pkg));
            }
        }

        for (String content : fileContents) {
            for (JavaSourceParser.ImportEntry entry : JavaSourceParser.extractImports(content)) {
                String fullPath   = entry.fullPath();
                String simpleName = entry.simpleName();

                if (isJdkImport(fullPath)) continue;

                String importedPackage = fullPath.substring(0, fullPath.length() - simpleName.length() - 1);

                if (knownClasses.contains(simpleName)) {
                    candidates.add(importedPackage);
                    continue;
                }
                if (namespaceRoots.contains(topLevelSegment(importedPackage))) candidates.add(importedPackage);
            }
        }

        logger.info("Detection completed: classes={}, candidates={}, skippedFiles={}", knownClasses.size(), candidates.size(), skipped);

        return new DetectionResult(candidates, classPackages);
    }

    private String topLevelSegment(String pkg) {
        int dot = pkg.indexOf('.');
        return dot == -1 ? pkg : pkg.substring(0, dot);
    }

    private boolean isJdkImport(String fullPath) {
        for (String prefix : Constants.JDK_PREFIXES) {
            if (fullPath.startsWith(prefix)) return true;
        }
        return false;
    }
}