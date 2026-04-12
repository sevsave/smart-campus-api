package com.smartcampus.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class AppConfig extends Application {
    // This stays empty. Its job is to activate JAX-RS.
}