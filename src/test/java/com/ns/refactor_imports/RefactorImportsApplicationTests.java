package com.ns.refactor_imports;

import com.ns.refactor_imports.service.PackageDetectorService;
import com.ns.refactor_imports.service.RefactorService;
import com.ns.refactor_imports.util.FileProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefactorImportsApplicationTests {

    @Autowired
    private PackageDetectorService packageDetectorService;

    @Autowired
    private FileProcessor fileProcessor;

    @Autowired
    private RefactorService refactorService;

    @Test
    void testDetectPackagesToRefactor() throws IOException {
        // Given: Archivos de prueba
        String estudianteContent = "package com.paquetevo;\n\npublic class Estudiante {\n    private String nombre;\n}";
        String cursoContent = "import java.util.ArrayList;\nimport com.paquetevo.Estudiante;\n\npublic class Curso {\n\n    private String nombreCurso;\n    private ArrayList<paqueteviejo_pkh.Estudiante> estudiantes;\n\n    public Curso(String nombreCurso) {\n        this.nombreCurso = nombreCurso;\n        this.estudiantes = new ArrayList<>();\n    }\n}";

        MockMultipartFile estudianteFile = new MockMultipartFile(
                "files", "Estudiante.java", "text/plain", estudianteContent.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile cursoFile = new MockMultipartFile(
                "files", "Curso.java", "text/plain", cursoContent.getBytes(StandardCharsets.UTF_8));

        List<MultipartFile> files = Arrays.asList(estudianteFile, cursoFile);

        // When: Detectamos paquetes a refactorizar
        Set<String> packagesToRefactor = packageDetectorService.detectPackagesToRefactor(files);

        // Then: Verificamos que detectó el paquete correcto
        System.out.println("Paquetes detectados: " + packagesToRefactor);
        assertNotNull(packagesToRefactor);
        assertTrue(packagesToRefactor.contains("paqueteviejo_pkh"),
                "Debería detectar paqueteviejo_pkh. Detectados: " + packagesToRefactor);
        assertEquals(1, packagesToRefactor.size());
    }
    @Test
    void testProcessFileWithGenericType() throws IOException {
        // Given
        String cursoContent = "import java.util.ArrayList;\nimport com.paquetevo.Estudiante;\n\npublic class Curso {\n\n    private String nombreCurso;\n    private ArrayList<paqueteviejo_pkh.Estudiante> estudiantes;\n\n    public Curso(String nombreCurso) {\n        this.nombreCurso = nombreCurso;\n        this.estudiantes = new ArrayList<>();\n    }\n}";

        Set<String> oldPackages = new HashSet<>(Arrays.asList("paqueteviejo_pkh"));
        String newPackage = "com.nuevopaquete";

        Map<String, Set<String>> classPackages = new HashMap<>();
        classPackages.put("Estudiante", new HashSet<>(Arrays.asList("com.paquetevo")));

        // When
        String processedContent = fileProcessor.processFile(
                cursoContent,
                oldPackages,
                newPackage,
                classPackages
        );

        // Then
        assertTrue(processedContent.contains("ArrayList<com.nuevopaquete.Estudiante>"),
                "Debería reemplazar el tipo genérico");
        assertFalse(processedContent.contains("paqueteviejo_pkh.Estudiante"),
                "No debería quedar el paquete antiguo");
        assertTrue(processedContent.contains("import com.paquetevo.Estudiante"),
                "No debería cambiar el import correcto");
    }
}