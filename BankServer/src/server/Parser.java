package server;

import java.math.BigDecimal;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class Parser {
  private String messageReceived;
  private int lastAccNum;
  private int lastClientId;
  private LinkedHashMap<Integer, Account> listOfAccounts;
  protected LinkedHashMap<Integer, Client> listOfClients;
  private LinkedHashMap<Integer, String> receivedMessages;
  private LinkedHashMap<Integer, String> sentMessages;
  private YahooCurrencyConverter currConverter;
  private boolean isAtMostOnce = true;

  public Parser() {
    messageReceived = "";
    // lastAccNum = (int) (Math.random() * 1000000);
    lastAccNum = 100;
    lastClientId = 11;
    listOfAccounts = new LinkedHashMap<Integer, Account>();
    listOfClients = new LinkedHashMap<Integer, Client>();
    receivedMessages = new LinkedHashMap<Integer, String>();
    sentMessages = new LinkedHashMap<Integer, String>();
    currConverter = new YahooCurrencyConverter();
  }

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

  public String parseMessage(String message) {
    setMessageReceived(message);
    String reply = "";
    int requestId = 0;
    boolean flag = false;

    if (messageReceived.equalsIgnoreCase("000000")) {
      return Integer.toString(createNewClient());
    } else if (messageReceived.equalsIgnoreCase("999999")) {
      for (Map.Entry entry : listOfClients.entrySet()) {
        if (((Client) entry.getValue()).getIPAddress().equals(UDPServer.ipAddress)) {
          listOfClients.remove(entry.getKey());
          break;
        }
      }
    } else {
      for (Map.Entry entry : listOfClients.entrySet()) {
        if (((Client) entry.getValue()).getIPAddress().equals(UDPServer.ipAddress)
            && ((Client) entry.getValue()).getClientID() == Integer.parseInt(message
                .substring(0, 2))) {
          flag = true;
          break;
        }
      }
      if (flag == false) return "This client is not authorized to transact with the server";

      for (Map.Entry entry : listOfClients.entrySet()) {
        if (((Client) entry.getValue()).getIPAddress().equals(UDPServer.ipAddress)) {
          ((Client) entry.getValue()).setPort(UDPServer.port);
          System.out.println("Port updated to " + ((Client) entry.getValue()).getPort());
          break;
        }
      }

      StringTokenizer stz = new StringTokenizer(message, "|");
      requestId = Integer.parseInt(stz.nextToken());
      if (receivedMessages.containsKey(requestId) && isAtMostOnce==true) {
        return sentMessages.get(requestId);
      }
      receivedMessages.put(requestId, message);
      String operation = stz.nextToken();
      String name, password, currency;
      int accountNum, time;
      double balance, amount;
      if (operation.equalsIgnoreCase("OpenAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        currency = stz.nextToken();
        balance = Double.parseDouble(stz.nextToken());
        accountNum = openAccount(name, password, currency, balance);
        reply =
            requestId + "|" + accountNum + "|" + name + "|" + currency + "|" + balance + "|"
                + "Account for " + name + " created with Account Num " + accountNum;
      } else if (operation.equalsIgnoreCase("CloseAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        reply = requestId + "|" + closeAccount(accountNum, password);
      } else if (operation.equalsIgnoreCase("DepositAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        currency = stz.nextToken();
        amount = Double.parseDouble(stz.nextToken());
        reply = requestId + "|" + depositToAccount(name, accountNum, password, currency, amount);
      } else if (operation.equalsIgnoreCase("WithdrawAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        currency = stz.nextToken();
        amount = Double.parseDouble(stz.nextToken());
        reply = requestId + "|" + withdrawFromAccount(name, accountNum, password, currency, amount);
      } else if (operation.equalsIgnoreCase("Monitor")) {
        time = Integer.parseInt(stz.nextToken());
        reply = requestId + "|" + setClientToMonitor(time);
      } else if (operation.equalsIgnoreCase("TransactionAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        reply = requestId + "|" + "\n" + getTransactionHistory(name, accountNum, password);
      } else if (operation.equalsIgnoreCase("TransferAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        int receiverAccountNum = Integer.parseInt(stz.nextToken());
        amount = Double.parseDouble(stz.nextToken());
        reply =
            requestId + "|"
                + transferToAccount(name, accountNum, password, receiverAccountNum, amount);
      }
      else if (operation.equalsIgnoreCase("CheckAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        reply = requestId + "|" + checkAccountBalance(name,password,accountNum);
      }
      
    }
    for (Map.Entry entry : listOfAccounts.entrySet()) {
      System.out.println(entry.getValue().toString());
    }

    for (Map.Entry entry : listOfClients.entrySet()) {
      System.out.println(entry.getValue().toString());
    }
    sentMessages.put(requestId, reply);
    return reply;
  }

  public int openAccount(String name, String password, String currency, double balance) {
    int accountNum = getLastAccountNum();
    setLastAccountNum(getLastAccountNum() + 1);
    Account newAccount = new Account(accountNum, name, password, currency, balance);
    listOfAccounts.put(accountNum, newAccount);
    return accountNum;
  }

  public int createNewClient() {
    int clientId = getLastClientId();
    setLastClientId(getLastClientId() + 1);
    Client newClient = new Client(clientId, false, UDPServer.ipAddress, UDPServer.port);
    listOfClients.put(clientId, newClient);
    return clientId;
  }

  public String closeAccount(int accountNum, String password) {
    if (listOfAccounts.containsKey(accountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)) {
        listOfAccounts.remove(accountNum);
        return "Account with account number " + accountNum + " has been closed";
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }

  public String withdrawFromAccount(String name, int accountNum, String password, String currency,
      double amount) {
    double convertedAmount = amount;
    if (listOfAccounts.containsKey(accountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)
          && listOfAccounts.get(accountNum).getName().equals(name)) {
        if (!listOfAccounts.get(accountNum).getCurrency().equalsIgnoreCase(currency))
          convertedAmount =
              changeCurrency(currency, listOfAccounts.get(accountNum).getCurrency(), amount);
        if (listOfAccounts.get(accountNum).getBalance() >= convertedAmount) {
          listOfAccounts.get(accountNum).setBalance(
              listOfAccounts.get(accountNum).getBalance() - convertedAmount);
          return "The withdrawal of amount " + amount + currency + " from account " + accountNum
              + " has been successful. Your new balance is "
              + listOfAccounts.get(accountNum).getBalance()
              + listOfAccounts.get(accountNum).getCurrency();
        } else {
          return "Insufficient funds available to complete transaction";
        }
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }

  public String depositToAccount(String name, int accountNum, String password, String currency,
      double amount) {
    double convertedAmount = amount;
    if (listOfAccounts.containsKey(accountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)
          && listOfAccounts.get(accountNum).getName().equals(name)) {
        if (!listOfAccounts.get(accountNum).getCurrency().equalsIgnoreCase(currency))
          convertedAmount =
              changeCurrency(currency, listOfAccounts.get(accountNum).getCurrency(), amount);
        listOfAccounts.get(accountNum).setBalance(
            listOfAccounts.get(accountNum).getBalance() + convertedAmount);
        return "The deposit of amount " + amount + currency + " to account " + accountNum
            + " has been successful. Your new balance is "
            + listOfAccounts.get(accountNum).getBalance()
            + listOfAccounts.get(accountNum).getCurrency();
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }

  public String checkAccountBalance(String name, String password, int accountNum) {
    if (listOfAccounts.containsKey(accountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)
          && listOfAccounts.get(accountNum).getName().equals(name)) {
        return "The account balance of account number " + accountNum + " is " + listOfAccounts.get(accountNum).getBalance() + listOfAccounts.get(accountNum).getCurrency();
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }
  
  public String getTransactionHistory(String name, int accountNum, String password) {
    if (listOfAccounts.containsKey(accountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)
          && listOfAccounts.get(accountNum).getName().equals(name)) {
        return listOfAccounts.get(accountNum).getTransactions();
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }

  public String transferToAccount(String name, int accountNum, String password, int receiverAccountNum, double amount) {
    double convertedAmount = amount;
    if (listOfAccounts.containsKey(accountNum) && listOfAccounts.containsKey(receiverAccountNum)) {
      if (listOfAccounts.get(accountNum).getPassword().equals(password)
          && listOfAccounts.get(accountNum).getName().equals(name)) {
        if (!listOfAccounts.get(accountNum).getCurrency().equalsIgnoreCase(listOfAccounts.get(receiverAccountNum).getCurrency()))
          convertedAmount =
              changeCurrency(listOfAccounts.get(accountNum).getCurrency(), listOfAccounts.get(receiverAccountNum).getCurrency(), amount);
        if (listOfAccounts.get(accountNum).getBalance() >= amount) {
          listOfAccounts.get(accountNum).setBalance(
              listOfAccounts.get(accountNum).getBalance() - amount);
          listOfAccounts.get(receiverAccountNum).setBalance(
            listOfAccounts.get(receiverAccountNum).getBalance() + convertedAmount);
          return "The transfer of amount " + amount + listOfAccounts.get(accountNum).getCurrency() + " from account " + accountNum
              + " to " + listOfAccounts.get(receiverAccountNum).getAccountNumber() + " has been successful. Your new balance is "
              + listOfAccounts.get(accountNum).getBalance()
              + listOfAccounts.get(accountNum).getCurrency();
        } else {
          return "Insufficient funds available to complete transaction";
        }
      } else
        return "The details entered are incorrect.";
    }
    return "Account with account number " + accountNum + " not found";
  }

  public double changeCurrency(String sourceCurrency, String destinationCurrency, double amount) {
    float exchangeRate = 1;
    try {
      exchangeRate = currConverter.convert(sourceCurrency, destinationCurrency);
    } catch (Exception e) {
      e.printStackTrace();
    }
    amount = amount * exchangeRate;
    amount = round(amount, 2, BigDecimal.ROUND_FLOOR);
    return amount;
  }

  public String setClientToMonitor(int time) {
    for (Map.Entry entry : listOfClients.entrySet()) {
      if (((Client) entry.getValue()).getIPAddress().equals(UDPServer.ipAddress)) {
        ((Client) entry.getValue()).setIsMonitor(true);
        UDPServer.timer.schedule(new MonitorTimer(), time * 1000);
      }
    }
    return "Client has been set as a monitor";
  }

  public static double round(double unrounded, int precision, int roundingMode) {
    BigDecimal bd = new BigDecimal(unrounded);
    BigDecimal rounded = bd.setScale(precision, roundingMode);
    return rounded.doubleValue();
  }
}
