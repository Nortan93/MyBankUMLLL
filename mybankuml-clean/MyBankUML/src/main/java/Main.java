import application.AccountManager;
import application.AdminManager;
import application.AuthenticationManager;
import application.RoleManager;
import application.SearchManager;
import application.TransactionManager;
import data.JsonFileService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import presentation.AccountController;
import presentation.AdminController;
import presentation.AuthController;
import presentation.SearchController;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting MyBankUML Backend...");

        // 1. Initialize Database Layer (Loads JSON files)
        JsonFileService database = new JsonFileService();

        // 2. Initialize Application Logic Layer (Managers)
        AuthenticationManager authMgr = new AuthenticationManager(database);
        RoleManager roleMgr = new RoleManager();
        AccountManager accountMgr = new AccountManager(database);
        TransactionManager txMgr = new TransactionManager(database);
        SearchManager searchMgr = new SearchManager(database);
        AdminManager adminMgr = new AdminManager(database);

        // 3. Initialize Presentation Layer (Controllers)
        AuthController authController = new AuthController(authMgr);
        AccountController accountController = new AccountController(accountMgr, txMgr, authMgr, roleMgr);
        AdminController adminController = new AdminController(adminMgr, authMgr, roleMgr);
        SearchController searchController = new SearchController(searchMgr, authMgr, roleMgr);

        // 4. Configure and Start Web Server
        Javalin app = Javalin.create(config -> {
            // Enables Cross-Origin Resource Sharing (useful for local testing)
            config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
            // Serve the frontend files from src/main/resources/public
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);

        // 5. Register API Routes
        
        // --- Authentication ---
        app.post("/api/login", authController::login);
        app.post("/api/logout", authController::logout);
        app.post("/api/change-password", authController::changePassword);

        // --- Account & Transactions ---
        app.get("/api/accounts", accountController::getAccounts);
        app.get("/api/accounts/user/{userId}", accountController::getAccountsByUser);
        app.post("/api/transaction", accountController::handleTransaction);
        
        
        // --- Admin Functions ---
        app.post("/api/admin/create-user", adminController::createUser);
        app.patch("/api/admin/users/{id}", adminController::updateUser);
        app.get("/api/admin/audit-logs", adminController::getAuditLogs);
        
        // --- Search ---
        app.get("/api/search", searchController::searchUsers);

        System.out.println("Backend running on http://localhost:8080");
    }
}