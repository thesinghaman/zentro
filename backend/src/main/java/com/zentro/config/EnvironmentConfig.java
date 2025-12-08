package com.zentro.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Environment Configuration Loader
 * Loads environment variables from .env.local file in enterprise-grade way
 * This runs before Spring Boot's property resolution
 */
public class EnvironmentConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final Logger log = LoggerFactory.getLogger(EnvironmentConfig.class);
    
    private static final String ENV_FILE = ".env.local";
    private static final String PROPERTY_SOURCE_NAME = "envFileProperties";
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Try to find .env.local in multiple locations
        Path envFilePath = findEnvFile();
        
        if (envFilePath != null && Files.exists(envFilePath)) {
            try {
                Map<String, Object> properties = loadEnvFile(envFilePath);
                environment.getPropertySources().addFirst(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, properties)
                );
                log.info("Loaded {} environment variables from {}", properties.size(), envFilePath);
            } catch (IOException e) {
                log.error("Failed to load environment file: {}", envFilePath, e);
                throw new RuntimeException("Failed to load environment configuration", e);
            }
        } else {
            log.warn("Environment file not found. Looking for: {}", ENV_FILE);
            log.warn("Searched in: project root, backend directory, and user directory");
            // In production, environment variables should be set by the deployment platform
            if (!"prod".equals(environment.getProperty("spring.profiles.active"))) {
                log.error("No .env.local file found for development environment");
            }
        }
    }
    
    /**
     * Find .env.local file in multiple locations
     * Priority: 1) Backend directory, 2) Project root, 3) User directory
     */
    private Path findEnvFile() {
        // Get the backend directory (where the JAR is running)
        String userDir = System.getProperty("user.dir");
        
        // Try backend directory first
        Path backendPath = Paths.get(userDir, ENV_FILE);
        if (Files.exists(backendPath)) {
            return backendPath;
        }
        
        // Try parent directory (project root)
        Path projectRootPath = Paths.get(userDir).getParent();
        if (projectRootPath != null) {
            Path rootEnvPath = projectRootPath.resolve(ENV_FILE);
            if (Files.exists(rootEnvPath)) {
                return rootEnvPath;
            }
        }
        
        // Try current directory
        Path currentPath = Paths.get(ENV_FILE);
        if (Files.exists(currentPath)) {
            return currentPath;
        }
        
        return null;
    }
    
    /**
     * Load and parse .env.local file
     * Supports: KEY=VALUE format, comments (#), empty lines, quoted values
     */
    private Map<String, Object> loadEnvFile(Path path) throws IOException {
        Map<String, Object> properties = new HashMap<>();
        
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                // Skip empty lines and comments
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return;
                }
                
                // Parse KEY=VALUE
                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex > 0) {
                    String key = trimmedLine.substring(0, separatorIndex).trim();
                    String value = trimmedLine.substring(separatorIndex + 1).trim();
                    
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    properties.put(key, value);
                }
            });
        }
        
        return properties;
    }
}
