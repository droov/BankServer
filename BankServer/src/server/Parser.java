package server;

import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

/*
 * Class that interprets the client message and generates a response 
 */
public class Parser {

	// Instance variables
	private String messageReceived;
	int temp;
	private int lastAccNum;
	private int lastClientId;
	private LinkedHashMap<Integer, Account> listOfAccounts;
	protected LinkedHashMap<Integer, Client> listOfClients;
	private LinkedHashMap<Integer, String> receivedMessages;
	private LinkedHashMap<Integer, String> sentMessages;
	private CurrencyConverter currConverter;
	private boolean isAtMostOnce = true; // For implementing the At-Most-Once
											// strategy. Can be set to false to
											// implement the At-Least-Once
											// strategy

	// Default constructor
	public Parser() {
		messageReceived = "";
		lastAccNum = (int) (Math.random() * 1000000); // Generates a random 6
														// digit account number
		lastClientId = 11;
		listOfAccounts = new LinkedHashMap<Integer, Account>();
		listOfClients = new LinkedHashMap<Integer, Client>();
		receivedMessages = new LinkedHashMap<Integer, String>();
		sentMessages = new LinkedHashMap<Integer, String>();
		currConverter = new CurrencyConverter();
		UDPServer.timer.schedule(new MonitorTimer(), 0, 1000);
	}

	// Get and Set methods for Instance Variables
	public String getMessageReceived() {
		return messageReceived;
	}

	public void setMessageReceived(String s) {
		messageReceived = s;
	}

	public int getLastAccountNum() {
		return lastAccNum;
	}

	public void setLastAccountNum(int acn) {
		lastAccNum = acn;
	}

	public int getLastClientId() {
		return lastClientId;
	}

	public void setLastClientId(int cid) {
		lastClientId = cid;
	}

	// Method to parse the message and generate a response
	public String parseMessage(String message) {
		try {
			setMessageReceived(message);
			String reply = "";
			int requestId = 0;
			boolean flag = false;

			if (messageReceived.equalsIgnoreCase("000000")) {
				// Client connects to server for the first time
				for (Map.Entry entry : listOfClients.entrySet()) {
					if (((Client) entry.getValue()).getIPAddress().equals(
							UDPServer.ipAddress)) {
						return "Client already connected to the server...";
					}
				}
				return Integer.toString(createNewClient());
			} else if (messageReceived.equalsIgnoreCase("999999")) {
				// Client disconnects from server
				for (Map.Entry entry : listOfClients.entrySet()) {
					if (((Client) entry.getValue()).getIPAddress().equals(
							UDPServer.ipAddress)) {
						listOfClients.remove(entry.getKey());
						return "Client has been disconnected from the server...";
					}
				}
				return "Client not connected to the server...";
			} else {
				// Check to ensure clients IP Address has been registered
				// with the server and the request is from the correct client
				for (Map.Entry entry : listOfClients.entrySet()) {
					if (((Client) entry.getValue()).getIPAddress().equals(
							UDPServer.ipAddress)
							&& ((Client) entry.getValue()).getClientID() == Integer
									.parseInt(message.substring(0, 2))) {
						flag = true;
						break;
					}
				}
				if (flag == false)
					return "This client is not authorized to transact with the server";

				// Updating port of client (not monitor)
				for (Map.Entry entry : listOfClients.entrySet()) {
					if (((Client) entry.getValue()).getIPAddress().equals(
							UDPServer.ipAddress)
							&& ((Client) entry.getValue()).getIsMonitor() == false) {
						((Client) entry.getValue()).setPort(UDPServer.port);
						System.out.println("Port updated to "
								+ ((Client) entry.getValue()).getPort());
						break;
					}
				}
				// Used to un-marshall the message
				StringTokenizer stz = new StringTokenizer(message, "|");
				requestId = Integer.parseInt(stz.nextToken());
				// For At-Most-Once Implementation, duplicate messages are
				// filtered and not processed again
				if (receivedMessages.containsKey(requestId)
						&& isAtMostOnce == true) {
					return sentMessages.get(requestId);
				}
				receivedMessages.put(requestId, message);
				String operation = stz.nextToken();
				String name, password, currency;
				int accountNum, time;
				double balance, amount;

				if (operation.equalsIgnoreCase("OpenAcc")) {
					// Open Account
					name = stz.nextToken();
					password = stz.nextToken();
					currency = stz.nextToken();
					// To ensure currency is of enumerated type
					if (!(currency.equals("AUD") || currency.equals("USD")
							|| currency.equals("GBP") || currency.equals("SGD")
							|| currency.equals("EUR") || currency.equals("JPY")))
						return reply = " | | | | |Incorrect currency type. Please try again...";
					balance = Double.parseDouble(stz.nextToken());
					accountNum = openAccount(name, password, currency, balance);
					reply = requestId + "|" + accountNum + "|" + name + "|"
							+ currency + "|" + balance + "|" + "Account for "
							+ name + " created with Account Num " + accountNum;
				} else if (operation.equalsIgnoreCase("CloseAcc")) {
					// Close Account
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					reply = requestId + "|"
							+ closeAccount(accountNum, password);
				} else if (operation.equalsIgnoreCase("DepositAcc")) {
					// Deposit Money into Account
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					currency = stz.nextToken();
					amount = Double.parseDouble(stz.nextToken());
					reply = requestId
							+ "|"
							+ depositToAccount(name, accountNum, password,
									currency, amount);
				} else if (operation.equalsIgnoreCase("WithdrawAcc")) {
					// Withdraw Money from Account
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					currency = stz.nextToken();
					amount = Double.parseDouble(stz.nextToken());
					reply = requestId
							+ "|"
							+ withdrawFromAccount(name, accountNum, password,
									currency, amount);
				} else if (operation.equalsIgnoreCase("Monitor")) {
					// Enable Monitor
					String masterPassword = stz.nextToken();
					int port = Integer.parseInt(stz.nextToken());
					time = Integer.parseInt(stz.nextToken());
					reply = requestId + "|"
							+ setClientToMonitor(time, masterPassword, port);
				} else if (operation.equalsIgnoreCase("TransactionAcc")) {
					// Obtain list of transactions carried out on account
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					reply = requestId + "|" + "\n"
							+ getTransactionHistory(name, accountNum, password);
				} else if (operation.equalsIgnoreCase("TransferAcc")) {
					// Transfer money to another bank account
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					int receiverAccountNum = Integer.parseInt(stz.nextToken());
					amount = Double.parseDouble(stz.nextToken());
					reply = requestId
							+ "|"
							+ transferToAccount(name, accountNum, password,
									receiverAccountNum, amount);
				} else if (operation.equalsIgnoreCase("CheckAcc")) {
					// Check account balance
					name = stz.nextToken();
					password = stz.nextToken();
					accountNum = Integer.parseInt(stz.nextToken());
					reply = requestId + "|"
							+ checkAccountBalance(name, password, accountNum);
				} else {
					// Handling erroneous messages
					if (operation.equalsIgnoreCase("OpenAcc"))
						reply = " | | | | |Server error occured while parsing the message. Please try again later...";
					else
						reply = "Server error occured while parsing the message. Please try again later...";
				}

			}
			sentMessages.put(requestId, reply);
			return reply;
		} catch (Exception e) {
			// Catch any exceptions that may occur
			return "Server error occured while parsing the message. Please try again later...";
		}
	}

	// Method to create an account with a auto generated account number
	public int openAccount(String name, String password, String currency,
			double balance) {
		int accountNum = getLastAccountNum(); // generated in default
												// constructor
		setLastAccountNum(getLastAccountNum() + 1); // incremented to ensure
													// uniqueness
		Account newAccount = new Account(accountNum, name, password, currency,
				balance);
		listOfAccounts.put(accountNum, newAccount);
		return accountNum;
	}

	// Method to create a client with a auto generated client id
	public int createNewClient() {
		int clientId = getLastClientId();
		setLastClientId(getLastClientId() + 1);
		Client newClient = new Client(clientId, false, UDPServer.ipAddress,
				UDPServer.port); // new client object created
		listOfClients.put(clientId, newClient); // client object added to list
												// of active clients
		return clientId;
	}

	// Method to close an existing account
	public String closeAccount(int accountNum, String password) {
		if (listOfAccounts.containsKey(accountNum)) {
			// Ensures account exists
			if (listOfAccounts.get(accountNum).getPassword().equals(password)) {
				// Ensures credentials are correct and removes account
				listOfAccounts.remove(accountNum);
				return "Account with account number " + accountNum
						+ " has been closed";
			} else
				return "The details entered are incorrect.";
		}
		return "Account with account number " + accountNum + " not found";
	}

	// Method to withdraw money from an account
	public String withdrawFromAccount(String name, int accountNum,
			String password, String currency, double amount) {
		double convertedAmount = amount;
		if (listOfAccounts.containsKey(accountNum)) {
			if (listOfAccounts.get(accountNum).getPassword().equals(password)
					&& listOfAccounts.get(accountNum).getName().equals(name)) {
				if (!listOfAccounts.get(accountNum).getCurrency()
						.equalsIgnoreCase(currency))
					// Converts currency of withdrawal to currency of account
					// based on live forex rates
					convertedAmount = changeCurrency(currency, listOfAccounts
							.get(accountNum).getCurrency(), amount);
				if (convertedAmount == -1)
					return "Server error occured while processing the transaction. Please try again later...";
				if (listOfAccounts.get(accountNum).getBalance() >= convertedAmount) {
					// Ensures client has sufficient funds
					listOfAccounts.get(accountNum).setBalance(
							listOfAccounts.get(accountNum).getBalance()
									- convertedAmount);
					return "The withdrawal of amount " + amount + currency
							+ " from account " + accountNum
							+ " has been successful. Your new balance is "
							+ roundBalance(listOfAccounts.get(accountNum).getBalance())
							+ listOfAccounts.get(accountNum).getCurrency();
				} else {
					return "Insufficient funds available to complete transaction";
				}
			} else
				return "The details entered are incorrect.";
		}
		return "Account with account number " + accountNum + " not found";
	}

	// Method to deposit money to an account
	public String depositToAccount(String name, int accountNum,
			String password, String currency, double amount) {
		double convertedAmount = amount;
		if (listOfAccounts.containsKey(accountNum)) {
			if (listOfAccounts.get(accountNum).getPassword().equals(password)
					&& listOfAccounts.get(accountNum).getName().equals(name)) {
				if (!listOfAccounts.get(accountNum).getCurrency()
						.equalsIgnoreCase(currency))
					convertedAmount = changeCurrency(currency, listOfAccounts
							.get(accountNum).getCurrency(), amount);
				if (convertedAmount == -1)
					return "Server error occured while processing the transaction. Please try again later...";
				listOfAccounts.get(accountNum).setBalance(
						listOfAccounts.get(accountNum).getBalance()
								+ convertedAmount);
				return "The deposit of amount " + amount + currency
						+ " to account " + accountNum
						+ " has been successful. Your new balance is "
						+ roundBalance(listOfAccounts.get(accountNum).getBalance())
						+ listOfAccounts.get(accountNum).getCurrency();
			} else
				return "The details entered are incorrect.";
		}
		return "Account with account number " + accountNum + " not found";
	}

	// Method to check account balance
	public String checkAccountBalance(String name, String password,
			int accountNum) {
		if (listOfAccounts.containsKey(accountNum)) {
			if (listOfAccounts.get(accountNum).getPassword().equals(password)
					&& listOfAccounts.get(accountNum).getName().equals(name)) {
				return "The account balance of account number " + accountNum
						+ " is " + roundBalance(listOfAccounts.get(accountNum).getBalance())
						+ listOfAccounts.get(accountNum).getCurrency();
			} else
				return "The details entered are incorrect.";
		}
		return "Account with account number " + accountNum + " not found";
	}

	// Method to obtain transaction history associated with an account
	public String getTransactionHistory(String name, int accountNum,
			String password) {
		if (listOfAccounts.containsKey(accountNum)) {
			if (listOfAccounts.get(accountNum).getPassword().equals(password)
					&& listOfAccounts.get(accountNum).getName().equals(name)) {
				return listOfAccounts.get(accountNum).getTransactions();
			} else
				return "The details entered are incorrect.";
		}
		return "Account with account number " + accountNum + " not found";
	}

	// Method to transfer funds between accounts
	public String transferToAccount(String name, int accountNum,
			String password, int receiverAccountNum, double amount) {
		double convertedAmount = amount;
		if (listOfAccounts.containsKey(accountNum)
				&& listOfAccounts.containsKey(receiverAccountNum)) {
			if (listOfAccounts.get(accountNum).getPassword().equals(password)
					&& listOfAccounts.get(accountNum).getName().equals(name)) {
				if (!listOfAccounts
						.get(accountNum)
						.getCurrency()
						.equalsIgnoreCase(
								listOfAccounts.get(receiverAccountNum)
										.getCurrency()))
					// Convert transfer amount if receiver currency is different
					// from sender currency
					convertedAmount = changeCurrency(
							listOfAccounts.get(accountNum).getCurrency(),
							listOfAccounts.get(receiverAccountNum)
									.getCurrency(), amount);
				if (convertedAmount == -1)
					return "Server error occured while processing the transaction. Please try again later...";
				if (listOfAccounts.get(accountNum).getBalance() >= amount) {
					listOfAccounts.get(accountNum).setBalance(
							listOfAccounts.get(accountNum).getBalance()
									- amount);
					listOfAccounts.get(receiverAccountNum).setBalance(
							listOfAccounts.get(receiverAccountNum).getBalance()
									+ convertedAmount);
					return "The transfer of amount "
							+ amount
							+ listOfAccounts.get(accountNum).getCurrency()
							+ " from account "
							+ accountNum
							+ " to "
							+ listOfAccounts.get(receiverAccountNum)
									.getAccountNumber()
							+ " has been successful. Your new balance is "
							+ roundBalance(listOfAccounts.get(accountNum).getBalance())
							+ listOfAccounts.get(accountNum).getCurrency();
				} else {
					return "Insufficient funds available to complete transaction";
				}
			} else
				return "The details entered are incorrect.";
		}
		if (!listOfAccounts.containsKey(accountNum))
			return "Account with account number " + accountNum + " not found";
		return "Account with account number " + receiverAccountNum
				+ " not found";
	}

	// Helper method to convert an amount to another currency based on live
	// forex rates
	public double changeCurrency(String sourceCurrency,
			String destinationCurrency, double amount) {
		float exchangeRate = 1;
		try {
			exchangeRate = currConverter.convertCurrency(sourceCurrency,
					destinationCurrency);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		amount = amount * exchangeRate;
		amount = round(amount, 2, BigDecimal.ROUND_FLOOR);
		return amount;
	}

	// Method to set client as a monitor
	public String setClientToMonitor(int time, String masterPassword, int port) {
		if (!masterPassword.equals("key")) // Matches to master password entered
											// by client
			return "Master Password is incorrect. Monitor privileges not granted.";
		for (Map.Entry entry : listOfClients.entrySet()) {
			if (((Client) entry.getValue()).getIPAddress().equals(
					UDPServer.ipAddress)) {
				((Client) entry.getValue()).setIsMonitor(true);
				((Client) entry.getValue()).setPort(port);
				// Timer set in seconds				
				//((Client) entry.getValue()).timer.schedule(new MonitorTimer(((Client) entry.getValue()).getClientID()), time * 1000);
				((Client) entry.getValue()).setEndTime(UDPServer.systemTime+time);
			}
		}
		return "Client has been set as a monitor";
	}
	
	public static String roundBalance(double balance) {
		String shortBalance = "";
		shortBalance = String.valueOf(balance);
		shortBalance = shortBalance.substring(0,shortBalance.indexOf(".") + 3);
		return shortBalance;
	}

	// Helper method to round a double value to 2 decimal places
	public double round(double unrounded, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		String roundedString = rounded.toString();
		roundedString = roundedString.substring(0,
				roundedString.indexOf(".") + 2);
		return Double.parseDouble(roundedString);
	}

	// Helper method to implement Caesar's Encryption Algorithm
	// All characters are cyclically shifted left by 3 positions and string is
	// reversed
	public static String encrypt(String input) {
		int i, j, length = input.length();
		char ch;
		String result = "";
		for (i = 0; i < length; i++) {
			ch = input.charAt(i);
			if (Character.isLetter(ch)) {
				if (Character.isUpperCase(ch)) {
					for (j = 0; j < 3; j++) {
						ch--;
						if (ch < 'A')
							ch = 'Z';
					}
				} else if (Character.isLowerCase(ch)) {
					for (j = 0; j < 3; j++) {
						ch--;
						if (ch < 'a')
							ch = 'z';
					}
				}
			} else if (Character.isDigit(ch)) {
				for (j = 0; j < 3; j++) {
					ch--;
					if (ch < '0')
						ch = '9';
				}
			}
			result = ch + result;
		}
		return result;
	}

	// Helper method to implement Caesar's Decryption Algorithm
	// All characters are cyclically shifted right by 3 positions and string is
	// reversed
	public static String decrypt(String input) {
		int i, j, length = input.length();
		char ch;
		String result = "";
		for (i = 0; i < length; i++) {
			ch = input.charAt(i);
			if (Character.isLetter(ch)) {
				if (Character.isUpperCase(ch)) {
					for (j = 0; j < 3; j++) {
						ch++;
						if (ch > 'Z')
							ch = 'A';
					}
				} else if (Character.isLowerCase(ch)) {
					for (j = 0; j < 3; j++) {
						ch++;
						if (ch > 'z')
							ch = 'a';
					}
				}
			} else if (Character.isDigit(ch)) {
				for (j = 0; j < 3; j++) {
					ch++;
					if (ch > '9')
						ch = '0';
				}
			}
			result = ch + result;
		}
		return result;
	}
}