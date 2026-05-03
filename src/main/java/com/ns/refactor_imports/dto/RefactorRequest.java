package com.ns.refactor_imports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class RefactorRequest {

    @NotEmpty(message = "At least one .java file is required")
    private List<MultipartFile> files;

    @NotBlank(message = "newPackage must not be blank")
    @Pattern(regexp = "^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$", message = "newPackage must be a valid Java package name (e.g. com.example.myapp).")
    private String newPackage;
}