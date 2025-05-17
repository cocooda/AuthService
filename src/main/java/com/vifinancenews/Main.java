package com.vifinancenews;

import com.vifinancenews.auth.controllers.AuthController;

import com.vifinancenews.auth.controllers.GoogleAuthController;
import com.vifinancenews.auth.controllers.GuestController;


import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class Main {
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            // CORS and Routing Optimizations
            config.router.contextPath = "/";
            config.router.treatMultipleSlashesAsSingleSlash = true;

            // Enable Brotli & Gzip Compression
            config.http.brotliAndGzipCompression();

            // Static Files (relative to project root)
            config.staticFiles.add(staticFileConfig -> {
                staticFileConfig.hostedPath = "/"; // URL path for static files
                staticFileConfig.directory = "static"; // Ensure `/static` is accessible
                staticFileConfig.location = Location.CLASSPATH;
            });
            
        
            // Request Settings
            config.http.asyncTimeout = 30000; // 30 sec timeout
            config.http.maxRequestSize = 10_000_000L; // 10MB max request size
        }).start("0.0.0.0", 6999);


        // Handle preflight OPTIONS requests
        app.options("/*", ctx -> {
            String origin = ctx.header("Origin");
            if (origin != null) {
                ctx.header("Access-Control-Allow-Origin", origin);
                ctx.header("Vary", "Origin");
            }
            ctx.header("Access-Control-Allow-Credentials", "true");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization, Cookie");
            ctx.status(204);
        });

        app.before(ctx -> {
            String origin = ctx.header("Origin");
            if (origin != null) {
                ctx.header("Access-Control-Allow-Origin", origin);
                ctx.header("Vary", "Origin");
            }
            ctx.header("Access-Control-Allow-Credentials", "true");
        });
        
        // **Auth Routes**
        app.post("/api/register", AuthController.register);
        app.post("/api/verify", AuthController.verifyCredentials); 
        app.post("/api/login", AuthController.login); 
        app.post("/api/logout", AuthController.logout);
        app.get("/api/auth-status", AuthController.checkAuth);
        app.post("/api/reactivate-account", AuthController.reactivateAccount);
        app.post("/api/forgot-password", AuthController.requestPasswordReset);
        app.post("/api/reset-password", AuthController.resetPassword);


        // **Guest Routes**
        GuestController.registerRoutes(app);
        app.post("/api/google-login", GoogleAuthController.handleGoogleLogin);

        System.out.println("Server running on http://localhost:6999/index.html");
    }
}
