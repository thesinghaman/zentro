package com.zentro;

import com.zentro.config.EnvironmentConfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for Zentro E-commerce Backend Application
 * 
 * @author Aman Singh
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class ZentroApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ZentroApplication.class);
        app.addInitializers(new EnvironmentConfig());
        app.run(args);
    }
}
