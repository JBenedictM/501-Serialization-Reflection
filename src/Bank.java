import java.util.ArrayList;

/**
 * Class that represents a Bank entity that contains a list of BankAccounts
 */
public class Bank{
    /** The list of bank accounts contained by this bank.*/
	private String bank_name;
	private int num_of_accounts;
    private double interest_rate;
	private ArrayList<BankAccount> accounts;
    
    
    
    
    public Bank(String name, int account_size, double int_rate) {
    	this.bank_name = name;
    	this.interest_rate = int_rate;
    	this.num_of_accounts = account_size;
    	accounts = new ArrayList<BankAccount>();
    	
    	for (int i=0; i<num_of_accounts; i++) {
    		accounts.add(new BankAccount(0.0, i, interest_rate));
    	}
    	
    }
    
    /**
     * Creates a new Bank that contains four accounts, each with a default
     * starting balance of $0.
     */
    public Bank() {
    	this("Generic Bank", 4, 0.0);
    }
    
    
    
    /**
     * Adds the specified account to this Bank.  Note that the actual
     * reference provided is used, which can be a potential privacy leak.
     * If an existing bank account is added to this Bank, we assume that
     * multiple entities may have control over the account and may all
     * modify this same account.
     * <p>
     * This method does not check if an account with the same account number
     * already exists.  Thus, it is possible that this bank contains two
     * accounts with the same account number.
     * <p>
     * This method adds the account as passed in directly, without making a copy of
     * of the account passed in.  This means that this is a potential memory leak.
     * This memory leak was allowed since it is possible multiple objects need
     * access and an ability to modify the same account.
     * <p>
     * @param account the account to add to this bank.
     */
    public void addAccount(BankAccount account) {
        accounts.add(account);
        num_of_accounts++;
    }
    
    /**
     * Transfer funds between the accounts specified by the account numbers.
     * If accounts with the specified account numbers don't exist, this method 
     * does not do anything.
     * <p>
     * This method uses the transfer method in BankAccount and passes a reference to
     * the actual instance stored in this Bank object of the account to transfer the 
     * money to. 
     * <p>
     * @param fromAccountNum the account number of the account to take the funds from.
     * @param toAccountNum the account number of the account to deposit the funds into.
     * @param amount the amount to transfer between the two accounts.
     */
    public void transfer(int fromAccountNum, int toAccountNum, double amount) {
        BankAccount fromAccount = null;
        BankAccount toAccount = null;
        for (BankAccount current : accounts) {
            if (current.getAccountNumber() == fromAccountNum) {
                fromAccount = current;
            }
            if (current.getAccountNumber() == toAccountNum) {
                toAccount = current;
            }
        }
        if (fromAccount != null && toAccount != null) {
            fromAccount.transfer(amount, toAccount);
        }
    }

    /**
     * Gets the account in this Bank that has the specified account number.
     * @param accountNumber the account number of the account to retrieve.
     * @return the bank account that has the specified account number.
     */
    public BankAccount getAccount(int accountNumber) {
        BankAccount account = null;
        for (BankAccount current : accounts) {
            if (current.getAccountNumber() == accountNumber) {
                account = new BankAccount(current);
            }
        }
        return account;
    }
    
    // getter methods for the private fields
    public String get_bank_name() {
    	return new String(this.bank_name);
    }
    
    public int get_number_of_accounts() {
    	return this.num_of_accounts;
    }
    
    public double get_interest_rate() {
    	return this.interest_rate;
    }
    
    
    
   
    
}
