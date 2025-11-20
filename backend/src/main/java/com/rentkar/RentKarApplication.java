package com.rentkar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RentKarApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentKarApplication.class, args);
        System.out.println("\n=================================");
        System.out.println("🚀 RentKar Backend is running!");
        System.out.println("📍 API: http://localhost:8080/api");
        System.out.println("📚 Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("=================================\n");
    }
}
