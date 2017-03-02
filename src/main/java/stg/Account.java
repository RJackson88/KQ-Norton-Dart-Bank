package stg;

import java.util.List;

/**
 * Created by rickjackson on 3/1/17.
 */
public class Account {
    private String accountType;
    private long balance;
    private double interestRate;
    private long overdraftPenalty;
    private long requiredMinimumBalance;
    private boolean isMinimumBalanceRequired;
    private List<RecurringTransaction> recurringTransactions;
    private Interest interest;
    
    public Account() {
        
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public long getBalance() {
        return balance;
    }
    
    void setBalance(long balance) {
        this.balance = balance;
    }
    
    public double getInterestRate() {
        return interestRate;
    }
    
    void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }
    
    public long getOverdraftPenalty() {
        return overdraftPenalty;
    }
    
    void setOverdraftPenalty(long overdraftPenalty) {
        this.overdraftPenalty = overdraftPenalty;
    }
    
    public long getRequiredMinimumBalance() {
        return requiredMinimumBalance;
    }
    
    void setRequiredMinimumBalance(long requiredMinimumBalance) {
        this.requiredMinimumBalance = requiredMinimumBalance;
    }
    
    public boolean isMinimumBalanceRequired() {
        return isMinimumBalanceRequired;
    }
    
    void setMinimumBalanceRequired(boolean isMinimumBalanceRequired) {
        this.isMinimumBalanceRequired = isMinimumBalanceRequired;
    }
    
    public Interest getInterest() {
        return interest;
    }
    
    void setInterest(Interest interest) {
        this.interest = interest;
    }
    
}
