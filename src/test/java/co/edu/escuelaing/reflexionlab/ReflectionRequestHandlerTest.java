package co.edu.escuelaing.reflexionlab;

import co.edu.escuelaing.reflexionlab.controller.GreetingController;
import co.edu.escuelaing.reflexionlab.controller.HelloController;
import co.edu.escuelaing.reflexionlab.controller.FirstWebService;
import co.edu.escuelaing.reflexionlab.ioc.ReflectionRequestHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionRequestHandlerTest {

    private ReflectionRequestHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReflectionRequestHandler();
    }

    @Test
    void shouldRegisterAndInvokeGetMapping() throws Exception {
        handler.registerController(HelloController.class);
        String result = handler.handle("/", Map.of());
        assertEquals("Greetings from Spring Boot!", result);
    }

    @Test
    void shouldReturnNullForUnmappedPath() throws Exception {
        handler.registerController(HelloController.class);
        assertNull(handler.handle("/unknown", Map.of()));
    }

    @Test
    void shouldSupportRequestParamWithDefault() throws Exception {
        handler.registerController(GreetingController.class);
        assertEquals("Hola World", handler.handle("/greeting", Map.of()));
        assertEquals("Hola Juan", handler.handle("/greeting", Map.of("name", "Juan")));
    }

    @Test
    void shouldLoadMultipleControllers() throws Exception {
        handler.registerController(HelloController.class);
        handler.registerController(GreetingController.class);
        assertEquals("Greetings from Spring Boot!", handler.handle("/", Map.of()));
        assertEquals("Hola World", handler.handle("/greeting", Map.of()));
    }

    @Test
    void shouldInvokeFirstWebService() throws Exception {
        handler.registerController(FirstWebService.class);
        assertEquals("Hello from FirstWebService!", handler.handle("/hello", Map.of()));
    }
}
