package com.pico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@Modulith
@SpringBootApplication
public class PicoApplication {

    public static void main(String[] args) {
        // Tune virtual thread scheduler parallelism by CPU cores; can be overridden via JVM args
        System.setProperty("jdk.virtualThreadScheduler.parallelism",
                String.valueOf(Runtime.getRuntime().availableProcessors()));

        SpringApplication.run(PicoApplication.class, args);
    }
}
