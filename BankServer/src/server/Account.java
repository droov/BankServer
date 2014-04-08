package server;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/*
 * Class that maintains details of an Account 
 */
public class Account {
	// Instance Variables
	private int accountNumber;
	private String name;
	private String password;
	private String currency;
	private double balance;
	private ArrayList<String> transaction;
	int temp;
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	// Default constructor
	Account() {
		transaction = new ArrayList<String>();
		transaction.add(now() + " Account was opened");
	}

	// Parametrized constructor
	Account(int accountNumber, String name, String password, String currency,
			Double balance) {
		String time = now();
		transaction = new ArrayList<String>();
		transaction.add(time + " Account was opened");
		this.accountNumber = accountNumber;
		transaction.add(time + " Account number was set to "
				+ Integer.toString(accountNumber));
		this.name = name;
		transaction.add(time + " Name of account holder was set to " + name);
		this.password = password;
		transaction.add(time + " Password was set to " + password);
		this.currency = currency;
		transaction.add(time + " Currency type of the account was set to "
				+ currency);
		this.balance = balance;
		transaction.add(time + " Balance was set to "
				+ Parser.roundBalance(balance));
	}

	// Get and set methods for the instance variables
	public int getAccountNumber() {
		return accountNumber;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getCurrency() {
		return currency;
	}

	public double getBalance() {
		return balance;
	}

	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
		transaction.add(now() + " Account number was set to "
				+ Integer.toString(accountNumber));
	}

	public void getName(String name) {
		this.name = name;
		transaction.add(now() + " Name of account holder was set to " + name);
	}

	public void setPassword(String password) {
		this.password = password;
		transaction.add(now() + " Password was set to " + password);
	}

	public void setCurrency(String currency) {
		this.currency = currency;
		transaction.add(now() + " Currency type of the account was set to "
				+ currency);
	}

	public void setBalance(double balance) {
		this.balance = balance;
		transaction.add(now() + " Balance was set to "
				+ Double.toString(balance));
	}

	public String getTransactions() {
		String message = "";
		for (int index = 0; index < transaction.size(); index++) {
			message = message + transaction.get(index) + "\r\n";
		}
		message = message + "All balances stated are in " + currency;
		return message;
	}

	// Helper method to calculate current time
	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}
}
