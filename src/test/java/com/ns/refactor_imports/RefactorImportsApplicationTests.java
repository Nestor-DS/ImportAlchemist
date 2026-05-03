package com.ns.refactor_imports;

import com.ns.refactor_imports.service.PackageDetectorService;
import com.ns.refactor_imports.util.FileProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RefactorImportsApplicationTests {

    @Autowired
    private PackageDetectorService packageDetectorService;

    @Autowired
    private FileProcessor fileProcessor;

    @Test
    void detectPackages() throws Exception {
        var student = file("Student.java", """
                package com.newpackage;

                public class Student {
                    private String name;
                }""");

        var course = file("Course.java", """
                import java.util.ArrayList;
                import com.newpackage.Student;

                public class Course {
                    private ArrayList<oldpackage_abc.Student> students;
                }""");
        Collection<String> files = new ArrayList<>(Arrays.asList(student.toString(), course.toString()));
        var result = packageDetectorService.detect(files);

        assertNotNull(result);
        assertEquals(Set.of("com.newpackage", "oldpackage_abc"), result);
    }

    @Test
    void processGenerics() {
        var content = """
                import java.util.ArrayList;
                import com.newpackage.Student;

                public class Course {
                    private ArrayList<oldpackage_abc.Student> students;

                    public void test() {
                        com.otherpackage.OtherClass o = new com.otherpackage.OtherClass();
                    }
                }""";

        var result = process(content,
                Set.of("com.newpackage", "oldpackage_abc", "com.otherpackage"), "new.target",
                Map.of("Student", Set.of("com.newpackage"), "OtherClass", Set.of("com.otherpackage"))
        );

        assertAll(
                () -> assertTrue(result.contains("ArrayList<new.target.Student>")),
                () -> assertTrue(result.contains("import new.target.Student")),
                () -> assertTrue(result.contains("new.target.OtherClass")),
                () -> assertFalse(result.contains("oldpackage_abc.Student")),
                () -> assertFalse(result.contains("com.newpackage.Student")),
                () -> assertFalse(result.contains("com.otherpackage.OtherClass"))
        );
    }

    @Test
    void processPackageDeclaration() {
        var content = """
                package com.newpackage;

                public class Student {}""";

        var result = process(content,
                Set.of("com.newpackage"),
                "new.target",
                Map.of("Student", Set.of("com.newpackage"))
        );

        assertTrue(result.contains("package new.target;"));
        assertFalse(result.contains("package com.newpackage;"));
    }

    @Test
    void processMultipleReferences() {
        var content = """
                import com.newpackage.Student;
                import java.util.List;

                public class Main {
                    public static void main(String[] args) {
                        Student s = new Student();
                        List<oldpackage_abc.Student> list = null;
                        com.otherpackage.Teacher t = new com.otherpackage.Teacher();
                    }
                }""";

        var result = process(content,
                Set.of("com.newpackage", "oldpackage_abc", "com.otherpackage"),
                "new.target.vo",
                Map.of(
                        "Student", Set.of("com.newpackage"),
                        "Teacher", Set.of("com.otherpackage")
                )
        );

        assertAll(
                () -> assertTrue(result.contains("import new.target.vo.Student")),
                () -> assertTrue(result.contains("List<new.target.vo.Student>")),
                () -> assertTrue(result.contains("new.target.vo.Teacher")),
                () -> assertFalse(result.contains("com.newpackage")),
                () -> assertFalse(result.contains("oldpackage_abc")),
                () -> assertFalse(result.contains("com.otherpackage"))
        );
    }

    @Test
    void noChanges() {
        var content = """
                import java.util.List;

                public class Test {
                    private List<String> items;
                }""";

        var result = process(content,
                Set.of("com.newpackage"),
                "new.target",
                Map.of("Student", Set.of("com.newpackage"))
        );

        assertEquals(content.trim(), result.trim());
    }

    private MockMultipartFile file(String name, String content) {
        return new MockMultipartFile(
                "files",
                name,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String process(String content, Set<String> packages, String target, Map<String, Set<String>> classMap) {
        return fileProcessor.processFile(content, packages, target, classMap);
    }
}