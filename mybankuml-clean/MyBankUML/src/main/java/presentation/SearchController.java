package presentation;

import java.util.List;
import java.util.Map;

import application.AuthenticationManager;
import application.RoleManager;
import application.SearchManager;
import io.javalin.http.Context;
import model.User;

public class SearchController {
    private SearchManager searchManager;
    private AuthenticationManager authManager;
    private RoleManager roleManager;

    public SearchController(SearchManager searchManager, AuthenticationManager authManager, RoleManager roleManager) {
        this.searchManager = searchManager;
        this.authManager = authManager;
        this.roleManager = roleManager;
    }

    public void searchUsers(Context ctx) {
        try {
            // 1. Verify Authentication
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
            
            User currentUser = authManager.getUserByToken(token);
            
            if (currentUser == null) {
                ctx.status(401).json(Map.of("error", "Invalid session"));
                return;
            }

            // 2. Verify Role (RBAC) - Only Teller or Admin can search
            if (!roleManager.canAccess(currentUser, RoleManager.Feature.SEARCH_CUSTOMERS)) {
                ctx.status(403).json(Map.of("error", "Access Denied: Insufficient Permissions"));
                return;
            }

            // 3. Get Query Parameter - Support both 'q' and 'query'
            String query = ctx.queryParam("q");
            if (query == null) {
                query = ctx.queryParam("query");
            }
            
            if (query == null || query.trim().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Search query is required"));
                return;
            }
            
            // 4. Execute Search (Logic Layer)
            List<User> results = searchManager.searchUsers(query);

            // 5. Return Results
            ctx.json(results);

        } catch (Exception e) {
            // Return 403 for permission errors, 400 for bad requests
            int status = e.getMessage().contains("Access Denied") ? 403 : 400;
            ctx.status(status).json(Map.of("error", e.getMessage()));
        }
    }
}