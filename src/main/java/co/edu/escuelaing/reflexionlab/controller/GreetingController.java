package co.edu.escuelaing.reflexionlab.controller;

import co.edu.escuelaing.reflexionlab.annotation.GetMapping;
import co.edu.escuelaing.reflexionlab.annotation.RequestParam;
import co.edu.escuelaing.reflexionlab.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}
