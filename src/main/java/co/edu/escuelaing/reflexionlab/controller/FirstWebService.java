package co.edu.escuelaing.reflexionlab.controller;

import co.edu.escuelaing.reflexionlab.annotation.GetMapping;
import co.edu.escuelaing.reflexionlab.annotation.RestController;

/**
 * Example REST controller for command-line loading.
 * Invoke: java -cp target/classes co.edu.escuelaing.reflexionlab.MicroSpringBoot co.edu.escuelaing.reflexionlab.controller.FirstWebService
 */
@RestController
public class FirstWebService {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from FirstWebService!";
    }
}
