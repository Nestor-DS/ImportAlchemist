package com.ns.refactor_imports.util;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FileProcessor {

    public String processFile(String content,
                              Set<String> oldPackages,
                              String newPackage,
                              Map<String, Set<String>> classPackages) {

        String result = content;

        for (String oldPackage : oldPackages) {
            String searchPattern = oldPackage + ".";

            int index = 0;
            while ((index = result.indexOf(searchPattern, index)) != -1) {

                int start = index + searchPattern.length();
                int end = start;

                while (end < result.length() &&
                        (Character.isLetterOrDigit(result.charAt(end)) ||
                                result.charAt(end) == '_' ||
                                result.charAt(end) == '$')) {
                    end++;
                }

                if (end > start) {
                    String className = result.substring(start, end);
                    if (classPackages.containsKey(className)) {
                        Set<String> actualPackages = classPackages.get(className);
                        if (!actualPackages.contains(oldPackage)) {
                            String oldText = oldPackage + "." + className;
                            String newText = newPackage + "." + className;

                            result = result.replace(oldText, newText);
                            index = index + newText.length();
                        } else {
                            index = end;
                        }
                    } else {
                        index = end;
                    }
                } else {
                    index = end + 1;
                }
            }
        }
        return result;
    }

    public String extractDeclaredPackage(String content) {
        Matcher matcher = Pattern.compile("^package\\s+([a-zA-Z0-9_.]+)\\s*;", Pattern.MULTILINE).matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }

    public String extractDeclaredClassName(String content) {
        Matcher matcher = Pattern.compile("class\\s+(\\w+)").matcher(content);
        return matcher.find() ? matcher.group(1) : null;
    }
}