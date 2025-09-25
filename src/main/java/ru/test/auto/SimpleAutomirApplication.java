package ru.test.auto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Этой аннотацией Spring Boot автоматически настраивает множество вещей
public class SimpleAutomirApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleAutomirApplication.class, args);
        System.out.println("Simple AutoMir Application started!");
    }
}