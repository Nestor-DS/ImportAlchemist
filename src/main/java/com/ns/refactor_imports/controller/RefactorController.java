package com.ns.refactor_imports.controller;

import com.ns.refactor_imports.dto.RefactorRequest;
import com.ns.refactor_imports.service.RefactorService;
import com.ns.refactor_imports.util.ResponseUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class RefactorController {

    @Autowired
    private RefactorService refactorService;

    @GetMapping("/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @PostMapping("/refactor")
    public ResponseEntity<byte[]> refactorImports(@Valid @ModelAttribute RefactorRequest request) throws IOException {
        byte[] zip = refactorService.refactorAndZip(request);
        return ResponseUtil.zipDownload(zip, "refactored_files.zip");
    }
}