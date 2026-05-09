package com.example.techtitans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TechTitansApplication {

    public static void main(String[] args) {
        SpringApplication.run(TechTitansApplication.class, args);
    }

}
