package co.edu.escuelaing.reflexionlab.controller;

import co.edu.escuelaing.reflexionlab.annotation.GetMapping;
import co.edu.escuelaing.reflexionlab.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
