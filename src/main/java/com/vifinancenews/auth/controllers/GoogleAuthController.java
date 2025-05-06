package com.vifinancenews.auth.controllers;

import com.vifinancenews.auth.services.AuthenticationService;
import com.vifinancenews.auth.services.AuthenticationService.LoginResult;
import com.vifinancenews.auth.services.GoogleAuthService;
import com.vifinancenews.common.daos.IdentifierDAO;
import com.vifinancenews.common.models.Identifier;
import com.vifinancenews.common.utilities.RedisSessionManager;
import io.javalin.http.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GoogleAuthController {

    private static final AuthenticationService authService = new AuthenticationService();

    public static Handler handleGoogleLogin = ctx -> {
        try {
            // Get the idToken from the request body
            GoogleLoginRequest request = ctx.bodyAsClass(GoogleLoginRequest.class);
            String idToken = request.idToken();
            System.out.println("Received ID token: " + idToken);
    
            // Step 1: Validate the ID token and extract user details
            GoogleAuthService.UserDetails userDetails = GoogleAuthService.getUserDetailsFromIdToken(idToken);
            if (userDetails == null) {
                System.out.println("Invalid Google token");
                ctx.status(401).json(Map.of("error", "Invalid Google token"));
                return;
            }
    
            String email = userDetails.getEmail();
            String name = userDetails.getName();
            System.out.println("User details: " + name + ", " + email);
    
            // Step 2: Look up the user by email
            Identifier existingUser = IdentifierDAO.getIdentifierByEmail(email);
    
            // Step 3: If the user doesn't exist, register them
            if (existingUser == null) {
                System.out.println("User not found, registering new user");
                boolean success = authService.createUserFromGoogle(
                        email, name, null, null
                );
                if (!success) {
                    System.out.println("Google login registration failed");
                    ctx.status(400).json(Map.of("error", "Google login failed"));
                    return;
                }
                existingUser = IdentifierDAO.getIdentifierByEmail(email);
            }
    
            // Step 4: Handle login for the existing or newly registered user
            LoginResult loginResult = authService.loginWithGoogle(email);
            if (loginResult == null) {
                System.out.println("Login failed for Google user");
                ctx.status(400).json(Map.of("error", "Google login failed"));
                return;
            }
    
            // Check if the account is soft-deleted (same as in local login)
            if (loginResult.softDeleted()) {
                ctx.status(200).json(Map.of(
                        "message", "Account is in reactivation period",
                        "actionRequired", "reactivate",
                        "userId", loginResult.userId()
                ));
                return;
            }
    
            // Step 5: Create a Redis-backed session for the user (tie session to user)
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("userId", loginResult.userId());
            String sessionId = RedisSessionManager.createSession(sessionData);
    
            // Step 6: Set the session cookie in the response
            ctx.cookie("SESSION_ID", sessionId, 3600); // 1 hour
    
            // Step 7: Respond with success message and user ID
            System.out.println("Successful login");
            ctx.status(200).json(Map.of("message", "Google login successful", "userId", loginResult.userId()));
            
        } catch (Exception e) {
            // Handle any exceptions and respond with a 500 error
            System.err.println("Error during Google login: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Google login error", "details", e.getMessage()));
        }
    };
    

    // DTO for parsing the Google login request
    public record GoogleLoginRequest(String idToken) {}
}
