package com.ns.refactor_imports.service;

import com.ns.refactor_imports.dto.DetectionResult;
import com.ns.refactor_imports.dto.RefactorRequest;
import com.ns.refactor_imports.util.FileProcessor;
import com.ns.refactor_imports.util.ZipUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RefactorServiceImpl implements RefactorService {

    private static final Logger logger = LoggerFactory.getLogger(RefactorServiceImpl.class);

    @Autowired
    private PackageDetectorService packageDetectorService;

    @Autowired
    private FileProcessor fileProcessor;

    @Override
    public byte[] refactorAndZip(RefactorRequest request) throws IOException {
        Map<String, String> fileContents = new LinkedHashMap<>();
        for (MultipartFile file : request.getFiles()) {
            String name = file.getOriginalFilename();
            if (StringUtils.isNotBlank(name) && name.endsWith(".java")) {
                fileContents.put(name, new String(file.getBytes(), StandardCharsets.UTF_8));
            }
        }

        if (fileContents.isEmpty()) throw new IllegalArgumentException("No .java files found in the request.");

        logger.info("Processing {} Java files, newPackage='{}'", fileContents.size(), request.getNewPackage());

        DetectionResult detection = packageDetectorService.detect(fileContents.values());

        Map<String, byte[]> processedFiles = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            String processed = fileProcessor.processFile(entry.getValue(), detection.packagesToRefactor(),
                    request.getNewPackage(), detection.classPackages());
            processedFiles.put(entry.getKey(), processed.getBytes(StandardCharsets.UTF_8));
        }
        return ZipUtil.createZip(processedFiles);
    }
}