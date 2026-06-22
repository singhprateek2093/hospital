package com.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 *
 * {@code @SpringBootApplication} bundles three things:
 *  - @Configuration      : this class can define beans
 *  - @EnableAutoConfiguration : Spring Boot wires up Tomcat, JPA, Security, etc.
 *  - @ComponentScan      : finds all our @Component/@Service/@RestController classes
 *                          under the com.hospital package.
 */
@SpringBootApplication
public class HospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}
