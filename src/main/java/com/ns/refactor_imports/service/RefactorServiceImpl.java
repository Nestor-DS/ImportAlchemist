package com.ns.refactor_imports.service;

import com.ns.refactor_imports.dto.RefactorRequest;
import com.ns.refactor_imports.util.FileProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class RefactorServiceImpl implements RefactorService {

    @Autowired
    private PackageDetectorService packageDetectorService;

    @Autowired
    private FileProcessor fileProcessor;

    @Override
    public byte[] refactorAndZip(RefactorRequest request) throws IOException {
        Set<String> packagesToRefactor = packageDetectorService.detectPackagesToRefactor(
                request.getFiles()
        );

        Map<String, Set<String>> classPackages = buildClassPackageMap(request.getFiles());

        Map<String, byte[]> processedFiles = new HashMap<>();

        for (MultipartFile file : request.getFiles()) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String originalFileName = file.getOriginalFilename();

            String processedContent = fileProcessor.processFile(
                    content,
                    packagesToRefactor,
                    request.getNewPackage(),
                    classPackages
            );

            processedFiles.put(originalFileName,
                    processedContent.getBytes(StandardCharsets.UTF_8));
        }

        return createZip(processedFiles);
    }

    private Map<String, Set<String>> buildClassPackageMap(List<MultipartFile> files)
            throws IOException {
        Map<String, Set<String>> classPackageMap = new HashMap<>();

        for (MultipartFile file : files) {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String declaredClassName = fileProcessor.extractDeclaredClassName(content);
            String declaredPackage = fileProcessor.extractDeclaredPackage(content);

            if (declaredClassName != null && declaredPackage != null) {
                classPackageMap.computeIfAbsent(declaredClassName, k -> new HashSet<>()).add(declaredPackage);
            }
        }

        return classPackageMap;
    }

    private byte[] createZip(Map<String, byte[]> files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        }
    }
}