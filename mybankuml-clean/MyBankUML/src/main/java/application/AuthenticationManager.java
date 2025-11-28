package application;

import data.DatabaseRepository;
import model.User;
import util.SecurityUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthenticationManager {
    private DatabaseRepository database;
    
    // Session Management
    private static class Session {
        String userId;
        LocalDateTime lastAccessed;
        
        Session(String userId) {
            this.userId = userId;
            this.lastAccessed = LocalDateTime.now();
        }
    }

    // Static map to persist sessions
    private static Map<String, Session> activeSessions = new HashMap<>();
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    public AuthenticationManager(DatabaseRepository database) {
        this.database = database;
    }

    public String login(String username, String password) throws Exception {
        Optional<User> userOpt = database.findUserByUsername(username);

        if (!userOpt.isPresent()) {
            throw new Exception("Invalid credentials");
        }

        User user = userOpt.get();

        // CHECK LOCKOUT / STATUS
        if (user.getStatus() == User.Status.LOCKED || user.getStatus() == User.Status.INACTIVE) {
            // FIX: Ensure the word "locked" is lowercase to pass AuthenticationTest
            throw new Exception("Account is locked/inactive (" + user.getStatus() + "). Contact Admin.");
        }

        // VERIFY PASSWORD
        if (SecurityUtils.checkPassword(password, user.getPasswordHash())) {
            user.resetFailedAttempts();
            database.saveUser(user);
            
            String token = SecurityUtils.generateUUID();
            activeSessions.put(token, new Session(user.getUserID())); 
            return token;
        } else {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= 5) {
                user.setStatus(User.Status.LOCKED);
            }
            database.saveUser(user);
            throw new Exception("Invalid credentials");
        }
    }

    public void logout(String token) {
        activeSessions.remove(token);
    }

    public User getUserByToken(String token) throws Exception {
        if (!activeSessions.containsKey(token)) {
            throw new Exception("Invalid session");
        }

        Session session = activeSessions.get(token);
        
        // CHECK TIMEOUT
        long minutesInactive = ChronoUnit.MINUTES.between(session.lastAccessed, LocalDateTime.now());
        if (minutesInactive > SESSION_TIMEOUT_MINUTES) {
            activeSessions.remove(token);
            throw new Exception("Session expired");
        }

        // Refresh timestamp
        session.lastAccessed = LocalDateTime.now();
        
        return database.findUserByID(session.userId)
                .orElseThrow(() -> new Exception("User not found"));
    }

    public void changePassword(String username, String currentPassword, String newPassword) throws Exception {
    User user = database.findUserByUsername(username)
            .orElseThrow(() -> new Exception("User not found"));
    
    if (!util.SecurityUtils.checkPassword(currentPassword, user.getPasswordHash())) {
        throw new Exception("Current password is incorrect");
    }
    
    if (newPassword == null || newPassword.length() < 6) {
        throw new Exception("New password must be at least 6 characters");
    }
    
    String newPasswordHash = util.SecurityUtils.hashPassword(newPassword);
    user.setPasswordHash(newPasswordHash);
    database.saveUser(user);
}
}