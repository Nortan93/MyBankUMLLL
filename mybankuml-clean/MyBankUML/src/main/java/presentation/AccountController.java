package presentation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import application.AccountManager;
import application.AuthenticationManager;
import application.RoleManager;
import application.TransactionManager;
import io.javalin.http.Context;
import model.Account;
import model.Transaction;
import model.User;

public class AccountController {
    private AccountManager accountManager;
    private TransactionManager transactionManager;
    private AuthenticationManager authManager;
    private RoleManager roleManager;
    private ObjectMapper mapper = new ObjectMapper();

    public AccountController(AccountManager am, TransactionManager tm, AuthenticationManager auth, RoleManager role) {
        this.accountManager = am;
        this.transactionManager = tm;
        this.authManager = auth;
        this.roleManager = role;
    }

    /**
     * NEW: Get accounts for the logged-in user
     * Endpoint: GET /api/accounts
     */
    public void getAccounts(Context ctx) {
        try {
            // 1. Verify Session
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
            
            User user = authManager.getUserByToken(token);
            if (user == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // 2. Get accounts for this user
            List<Account> accounts = accountManager.getAccountsByUserId(user.getUserID());
            
            // 3. Return accounts as JSON
            ctx.status(200).json(accounts);

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch accounts"));
        }
    }

    /**
     * NEW: Get accounts for a specific user (Teller/Admin only)
     * Endpoint: GET /api/accounts/user/{userId}
     */
    public void getAccountsByUser(Context ctx) {
        try {
            // 1. Verify Session
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User currentUser = authManager.getUserByToken(token);
            if (currentUser == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // 2. Check Permissions (only Teller/Admin can view other users' accounts)
            if (!roleManager.canAccess(currentUser, RoleManager.Feature.SEARCH_CUSTOMERS)) {
                ctx.status(403).json(Map.of("error", "Access Denied"));
                return;
            }

            // 3. Get target user ID from path parameter
            String targetUserId = ctx.pathParam("userId");
            
            // 4. Get accounts for that user
            List<Account> accounts = accountManager.getAccountsByUserId(targetUserId);
            
            // 5. Return accounts as JSON
            ctx.status(200).json(accounts);

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch accounts"));
        }
    }

    /**
     * NEW: Get transaction history for a specific account
     * Endpoint: GET /api/transactions/{accountNumber}
     */
    public void getTransactionHistory(Context ctx) {
        try {
            // 1. Verify Session
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            User user = authManager.getUserByToken(token);
            if (user == null) {
                ctx.status(401).json(Map.of("error", "Unauthorized"));
                return;
            }

            // 2. Get account number from path parameter
            String accountNumber = ctx.pathParam("accountNumber");
            
            // 3. Verify the account belongs to the user (or user is Teller/Admin)
            List<Account> userAccounts = accountManager.getAccountsByUserId(user.getUserID());
            boolean isOwnAccount = userAccounts.stream()
                .anyMatch(acc -> acc.getAccountNumber().equals(accountNumber));
            
            boolean isTellerOrAdmin = roleManager.canAccess(user, RoleManager.Feature.SEARCH_CUSTOMERS);
            
            if (!isOwnAccount && !isTellerOrAdmin) {
                ctx.status(403).json(Map.of("error", "Access Denied"));
                return;
            }

            // 4. Get transaction history using TransactionManager
            List<Transaction> transactions = transactionManager.getTransactionsByAccount(accountNumber);
            
            // 5. Return transactions as JSON
            ctx.status(200).json(transactions);

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to fetch transactions"));
        }
    }

    public void handleTransaction(Context ctx) {
        try {
            // 1. Verify Session
            String token = ctx.header("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7); // Remove "Bearer " prefix
            }
            
            User user = authManager.getUserByToken(token);

            // 2. Check Permissions (RBAC)
            if (!roleManager.canAccess(user, RoleManager.Feature.PROCESS_TRANSACTION)) {
                throw new Exception("Access Denied");
            }

            // 3. Parse Request (Using Jackson)
            @SuppressWarnings("unchecked")
            Map<String, Object> req = mapper.readValue(ctx.body(), Map.class);
            
            String type = (String) req.get("type");
            String accNum = (String) req.get("accountNumber");
            
            // Handle numeric conversion safely for BigDecimal
            Object amountObj = req.get("amount");
            if (amountObj == null) throw new Exception("Amount is required");
            BigDecimal amount = new BigDecimal(String.valueOf(amountObj));

            // 4. Execute Logic
            if (type == null) throw new Exception("Transaction type is required");

            switch (type.toUpperCase()) {
                case "DEPOSIT":
                    accountManager.deposit(accNum, amount);
                    break;
                case "WITHDRAWAL":
                    accountManager.withdraw(accNum, amount);
                    break;
                case "TRANSFER":
                    String target = (String) req.get("targetAccount");
                    if (target == null) throw new Exception("Target account required for transfer");
                    transactionManager.transfer(accNum, target, amount);
                    break;
                default:
                    throw new Exception("Invalid transaction type");
            }

            ctx.status(200).json(Map.of("message", "Transaction Successful"));

        } catch (Exception e) {
            ctx.status(400).json(Map.of("error", e.getMessage() != null ? e.getMessage() : "Transaction Failed"));
        }
    }
}