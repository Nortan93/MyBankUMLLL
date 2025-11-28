package application;

import java.math.BigDecimal;
import java.util.List;

import data.DatabaseRepository;
import model.Account;
import model.Transaction;
import util.SecurityUtils;

public class AccountManager {
    private DatabaseRepository database;

    public AccountManager(DatabaseRepository database) {
        this.database = database;
    }

    /**
     * Get all accounts for a specific user
     */
    public List<Account> getAccountsByUserId(String userId) {
        return database.findAccountsByUserID(userId);
    }

    public void deposit(String accountNumber, BigDecimal amount) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Deposit amount must be positive");
        }

        Account account = getAccount(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        
        database.saveAccount(account);
        recordTransaction(accountNumber, null, amount, Transaction.Type.DEPOSIT);
    }

    public void withdraw(String accountNumber, BigDecimal amount) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Withdrawal amount must be positive");
        }

        Account account = getAccount(accountNumber);

        // REAL-TIME VALIDATION (Revision #3)
        if (account.getBalance().compareTo(amount) < 0) {
            throw new Exception("Insufficient Funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        
        database.saveAccount(account);
        recordTransaction(accountNumber, null, amount, Transaction.Type.WITHDRAWAL);
    }

    public BigDecimal getBalance(String accountNumber) throws Exception {
        return getAccount(accountNumber).getBalance();
    }

    // Helper to fetch account or throw error
    private Account getAccount(String accountNumber) throws Exception {
        return database.findAccountByNumber(accountNumber)
                .orElseThrow(() -> new Exception("Account not found: " + accountNumber));
    }

    private void recordTransaction(String source, String target, BigDecimal amount, Transaction.Type type) {
        Transaction tx = new Transaction(SecurityUtils.generateUUID(), source, target, amount, type);
        database.logTransaction(tx);
    }
}