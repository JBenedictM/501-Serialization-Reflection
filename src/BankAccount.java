/**
 * Class that holds a BankAccount which has a balance and an account number.
 * You can deposit money into the account, withdraw money from the account and
 * transfer money to another account.
 * author: Nathaly Verwaal
 * modified by Andy Ma
 */
public class BankAccount {
	private double interestRate = 0;
    private double balance = 0.0;
    private int accountNumber = 1;
    
	public BankAccount() {
		accountNumber = 0;
		balance = 0.0;
		interestRate = 0.0;
	}
	
	public BankAccount(double startBalance, int account_num, double interestRate) {
		this.balance = startBalance;
		this.accountNumber = account_num;
		this.interestRate = interestRate;
	}
    
	public BankAccount(BankAccount accountToCopy) {
		balance = accountToCopy.balance;
		accountNumber = accountToCopy.accountNumber;
	}
    /** 
     * This accessor methods returns the current balance in this account.
     * @return the current balance of the account.
     */
    public double getBalance() {
        return balance;
    }
    
    /**
     * This accessor method returns the account number of this account.
     * @return the account number of the account.
     */
    public int getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * This mutator method withdraws the specified amount if sufficient funds 
     * exist in this account.  The amount is required to be a positive amount.
     * @param amount the amount to withdraw from the account.
     */
    public void withdraw(double amount) {
        if (amount <= balance  && amount > 0) {
            balance -= amount;
        }
    }
    
    /**
     * This mutator method deposits the specified amount, which is required
     * to be positive.
     * @param amount the amount to deposit into the account.
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
    
    /**
     * This mutator method transfers the specified amount from this account
     * to the specified account if there is sufficient funds in this account.
     * The amount is required to be positive.
     * @param amount the amount to transfer.
     * @param toAccount the account to transfer the funds to.
     */
    public void transfer(double amount, BankAccount toAccount) {
        if (amount > 0 && balance >= amount) {
            withdraw(amount);
            toAccount.deposit(amount);
        }
    }

    /**
     * This accessor method returns a string representation of this account
     * in the format (<account_number>,<current_balance>).
     * @return a string representation of this account.
     */
    public String toString() {
        return String.format("%d,%.2f",accountNumber,balance);
    }
    
    /**
     * Returns true if this account is considered equal to the specified account.
     * Two BankAccounts are considered equal if they have the same account number.
     * @param other the account to check for equality with.
     */
    public boolean equals(BankAccount other) {
        return accountNumber == other.accountNumber;
    }
	
	/**
	 * Sets the interest rate of all back accounts.
	 * @param newInterestRate the value to which interestRate will be set; must be positive
	 */
	public void setInterestRate(double newInterestRate) {
		if (newInterestRate >= 0) {
			interestRate = newInterestRate;
		}
	}
	
	/**
	 * Adds interest to the account using the value of interestRate
	 */
	public void addInterest() {
		double interest = balance * interestRate;
		balance += interest;
	}
	
	public double get_interest_rate() {
		return this.interestRate;
	}
	
	
	
	
}
