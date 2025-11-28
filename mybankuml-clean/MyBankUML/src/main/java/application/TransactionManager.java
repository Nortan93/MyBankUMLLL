package application;

import java.math.BigDecimal;
import java.util.List;

import data.DatabaseRepository;
import model.Account;
import model.Transaction;
import util.SecurityUtils;

public class TransactionManager {
    private DatabaseRepository database;

    public TransactionManager(DatabaseRepository database) {
        this.database = database;
    }

    /**
     * NEW: Get transaction history for a specific account
     */
    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return database.findTransactionsByAccount(accountNumber);
    }

    public void transfer(String sourceAccNum, String targetAccNum, BigDecimal amount) throws Exception {
        if (sourceAccNum.equals(targetAccNum)) {
            throw new Exception("Cannot transfer to the same account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception("Transfer amount must be positive");
        }

        // Load both accounts
        Account source = database.findAccountByNumber(sourceAccNum)
                .orElseThrow(() -> new Exception("Source account not found"));
        Account target = database.findAccountByNumber(targetAccNum)
                .orElseThrow(() -> new Exception("Target account not found"));

        // Validate Source Balance
        if (source.getBalance().compareTo(amount) < 0) {
            throw new Exception("Insufficient Funds for Transfer");
        }

        // Execute Transfer (In a real DB, this would be a @Transactional block)
        // Here we modify objects and save strictly sequentially
        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));

        database.saveAccount(source);
        database.saveAccount(target);

        // Log Transaction
        Transaction tx = new Transaction(
            SecurityUtils.generateUUID(), 
            sourceAccNum, 
            targetAccNum, 
            amount, 
            Transaction.Type.TRANSFER
        );
        database.logTransaction(tx);
    }
}