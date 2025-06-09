package com.example.springjwt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringJwtApplication {

    public static void main(String[] args) {
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "src/main/resources/gcp-key.json");
        SpringApplication.run(SpringJwtApplication.class, args);
    }

}
