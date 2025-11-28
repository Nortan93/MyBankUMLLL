package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonFileService implements DatabaseRepository {

    private static final String DATA_DIR = "data/";
    private static final String USERS_FILE = DATA_DIR + "users.json";
    private static final String ACCOUNTS_FILE = DATA_DIR + "accounts.json";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "transactions.json";
    private static final String AUDIT_FILE = DATA_DIR + "audit_logs.json";

    private List<User> users;
    private List<Account> accounts;
    private List<Transaction> transactions;
    private List<AuditLog> auditLogs;
    private ObjectMapper mapper;

    public JsonFileService() {
        // Initialize Jackson Mapper
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
        this.mapper.registerModule(new JavaTimeModule());       // Handle Java 8 Dates

        // Ensure data directory exists
        new File(DATA_DIR).mkdirs();

        // Load Data
        this.users = loadData(USERS_FILE, new TypeReference<List<User>>(){});
        this.accounts = loadData(ACCOUNTS_FILE, new TypeReference<List<Account>>(){});
        this.transactions = loadData(TRANSACTIONS_FILE, new TypeReference<List<Transaction>>(){});
        this.auditLogs = loadData(AUDIT_FILE, new TypeReference<List<AuditLog>>(){});
    }

    // --- GENERIC FILE IO ---
    private <T> List<T> loadData(String filePath, TypeReference<List<T>> typeRef) {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, typeRef);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveData(String filePath, Object data) {
        try {
            mapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- USER OPERATIONS ---
    @Override
    public Optional<User> findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public Optional<User> findUserByID(String id) {
        return users.stream().filter(u -> u.getUserID().equals(id)).findFirst();
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users);
    }

    @Override
    public void saveUser(User user) {
        users.removeIf(u -> u.getUserID().equals(user.getUserID()));
        users.add(user);
        saveData(USERS_FILE, users);
    }

    // --- ACCOUNT OPERATIONS ---
    @Override
    public List<Account> findAccountsByUserID(String userID) {
        return accounts.stream().filter(a -> a.getOwnerUserID().equals(userID)).collect(Collectors.toList());
    }

    @Override
    public Optional<Account> findAccountByNumber(String accountNumber) {
        return accounts.stream().filter(a -> a.getAccountNumber().equals(accountNumber)).findFirst();
    }

    @Override
    public void saveAccount(Account account) {
        // FIXED: Update in place to maintain order and avoid reference issues
        boolean found = false;
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getAccountNumber().equals(account.getAccountNumber())) {
                accounts.set(i, account);  // Replace at same position
                found = true;
                break;
            }
        }
        
        // If not found, add new account
        if (!found) {
            accounts.add(account);
        }
        
        saveData(ACCOUNTS_FILE, accounts);
    }

    // --- TRANSACTION OPERATIONS ---
    @Override
    public void logTransaction(Transaction transaction) {
        transactions.add(transaction);
        saveData(TRANSACTIONS_FILE, transactions);
    }

    @Override
    public List<Transaction> findTransactionsByAccount(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getSourceAccountNumber().equals(accountNumber) ||
                             (t.getTargetAccountNumber() != null && t.getTargetAccountNumber().equals(accountNumber)))
                .collect(Collectors.toList());
    }

    // --- AUDIT OPERATIONS ---
    @Override
    public void logAudit(AuditLog log) {
        auditLogs.add(log);
        saveData(AUDIT_FILE, auditLogs);
    }

    @Override
    public List<AuditLog> findAllAuditLogs() {
        return new ArrayList<>(auditLogs);
    }
}