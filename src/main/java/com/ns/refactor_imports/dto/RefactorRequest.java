package com.ns.refactor_imports.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RefactorRequest {
    private List<MultipartFile> files;
    private String newPackage;
}