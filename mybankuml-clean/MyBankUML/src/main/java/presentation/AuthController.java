package presentation;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import application.AuthenticationManager;
import io.javalin.http.Context;
import model.User;

public class AuthController {

    private AuthenticationManager authManager;
    private ObjectMapper mapper = new ObjectMapper();

    public AuthController(AuthenticationManager authManager) {
        this.authManager = authManager;
    }

    public void login(Context ctx) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> creds = mapper.readValue(ctx.body(), Map.class);
            
            String token = authManager.login(creds.get("username"), creds.get("password"));

            User user = authManager.getUserByToken(token);
            
            ctx.json(Map.of(
                "token", token,
                "role", authManager.getUserByToken(token).getRole(), 
                "fullName", user.getFullName(),   
                "username", user.getUsername(),
                "message", "Login successful"
            ));
        } catch (Exception e) {
            ctx.status(401).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Authentication failed"));
        }
    }

    public void logout(Context ctx) {
        String token = ctx.header("Authorization");
        if (token != null) {
            authManager.logout(token);
        }
        ctx.status(200).json(Map.of("message", "Logged out"));
    }

    public void changePassword(Context ctx) {
        try {
            // Get token
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User user = authManager.getUserByToken(token);
            
            if (user == null) {
                ctx.status(401).json(Map.of("error", "Invalid session"));
                return;
            }

            // Parse request
            @SuppressWarnings("unchecked")
            Map<String, String> req = mapper.readValue(ctx.body(), Map.class);
            
            String currentPassword = req.get("currentPassword");
            String newPassword = req.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                ctx.status(400).json(Map.of("error", "Current password and new password are required"));
                return;
            }

            // Change password
            authManager.changePassword(user.getUsername(), currentPassword, newPassword);
            
            ctx.json(Map.of("message", "Password changed successfully"));

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to change password"));
        }
    }
}