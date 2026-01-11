package org.clokey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ClokeyApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClokeyApiApplication.class, args);
    }
}
