package co.edu.escuelaing.reflexionlab;

import co.edu.escuelaing.reflexionlab.annotation.RestController;
import co.edu.escuelaing.reflexionlab.controller.GreetingController;
import co.edu.escuelaing.reflexionlab.controller.HelloController;
import co.edu.escuelaing.reflexionlab.controller.FirstWebService;
import co.edu.escuelaing.reflexionlab.ioc.ClassPathScanner;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ClassPathScannerTest {

    private static final String BASE_PACKAGE = "co.edu.escuelaing.reflexionlab";

    @Test
    void shouldFindRestControllerClasses() {
        Set<Class<?>> classes = ClassPathScanner.findAnnotatedClasses(RestController.class, BASE_PACKAGE);
        assertTrue(classes.contains(HelloController.class));
        assertTrue(classes.contains(GreetingController.class));
        assertTrue(classes.contains(FirstWebService.class));
    }
}
