package com.family.spend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FamilySpendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FamilySpendApplication.class, args);
    }
}
