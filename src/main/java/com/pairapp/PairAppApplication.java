package com.pairapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PairAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(PairAppApplication.class, args);
    }
}
