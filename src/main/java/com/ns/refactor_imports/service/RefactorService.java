package com.ns.refactor_imports.service;

import com.ns.refactor_imports.dto.RefactorRequest;
import java.io.IOException;

public interface RefactorService {
    byte[] refactorAndZip(RefactorRequest request) throws IOException;
}