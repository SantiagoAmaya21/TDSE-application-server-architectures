package co.edu.escuelaing.reflexionlab;

import co.edu.escuelaing.reflexionlab.annotation.RestController;
import co.edu.escuelaing.reflexionlab.ioc.ClassPathScanner;
import co.edu.escuelaing.reflexionlab.ioc.ReflectionRequestHandler;
import co.edu.escuelaing.reflexionlab.server.HttpServer;

import java.util.Set;

/**
 * Entry point for the IoC web server. Supports two modes:
 * 1) Command-line: pass fully qualified class names of @RestController classes.
 *    Example: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.controller.FirstWebService
 * 2) Classpath scan: if no args, scans the classpath for classes annotated with @RestController and loads them.
 */
public class MicroSpringBoot {

    private static final String BASE_PACKAGE = "co.edu.escuelaing.reflexionlab";

    public static void main(String[] args) throws Exception {
        ReflectionRequestHandler handler = new ReflectionRequestHandler();

        if (args != null && args.length > 0) {
            for (String className : args) {
                Class<?> clazz = Class.forName(className.trim());
                handler.registerController(clazz);
            }
        } else {
            Set<Class<?>> controllers = ClassPathScanner.findAnnotatedClasses(RestController.class, BASE_PACKAGE);
            for (Class<?> clazz : controllers) {
                handler.registerController(clazz);
            }
        }

        int port = HttpServer.getDefaultPort();
        HttpServer server = new HttpServer(port, handler);
        server.start();
    }
}
