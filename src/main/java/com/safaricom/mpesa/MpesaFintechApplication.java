package com.safaricom.mpesa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MpesaFintechApplication
 *
 * <p>Main entry point for the Safaricom M-PESA & Core Banking Integration Gateway.</p>
 * <p>Features orchestrated:</p>
 * <ul>
 *     <li>Auto-configuration of Spring MVC, Spring Data JPA, and Spring Security</li>
 *     <li>Component scanning for controllers, services, repositories, and filters</li>
 *     <li>Embedded Tomcat server startup on designated port (default: 8080)</li>
 * </ul>
 *
 * @author Bililign Yibgeta
 * @version 1.0.0
 */
@SpringBootApplication
public class MpesaFintechApplication {

    /**
     * Application entry point.
     *
     * @param args command-line arguments passed during startup
     */
    public static void main(String[] args) {
        SpringApplication.run(MpesaFintechApplication.class, args);
    }
}
