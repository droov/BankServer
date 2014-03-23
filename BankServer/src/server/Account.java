package server;

import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Account {
	private int accountNumber;
	private String name;
	private String password;
	private String currency;
	private double balance;
	private ArrayList transaction;
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	Account() {
		ArrayList<String> transaction = new ArrayList<String>();
		transaction.add(now() + "Account was opened");
	}
	
	Account(int AccountNumber, String name, String password, String currency, Double balance) {
		ArrayList<String> transaction = new ArrayList<String>();
		transaction.add(now() + "Account was opened");
		this.accountNumber = accountNumber;
		transaction.add(now() + " Account number was set to " + Integer.toString(accountNumber));
		this.name = name;
		transaction.add(now() + " Name of account holder was set to " + name);
		this.password = password;
		transaction.add(now() + "Password was set to " + password);
		this.currency = currency;
		transaction.add(now() + "Currency type of the account was set to " + currency);
		this.balance = balance;
		transaction.add(now() + "Balance was set to " + Double.toString(balance));
		
	}

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
		transaction.add(now() + " Account number was set to " + Integer.toString(accountNumber));
	}
	
	public void getName(String name) {
		this.name = name;
		transaction.add(now() + " Name of account holder was set to " + name);
	}
	
	public void setPassword(String password) {
		this.password = password;
		transaction.add(now() + "Password was set to " + password);
	}
	
	public void setCurrency(String currency) {
		this.currency = currency;
		transaction.add(now() + "Currency type of the account was set to " + currency);
	}
	
	public void setBalance(double balance) {
		this.balance = balance;
		transaction.add(now() + "Balance was set to " + Double.toString(balance));
	}
	
	public static String now() {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	return sdf.format(cal.getTime());
	}

}
