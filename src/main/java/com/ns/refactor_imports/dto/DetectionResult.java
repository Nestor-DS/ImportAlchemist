package com.ns.refactor_imports.dto;

import java.util.Map;
import java.util.Set;

public record DetectionResult(
        Set<String> packagesToRefactor,
        Map<String, Set<String>> classPackages
) {}