package server;

import java.util.LinkedHashMap;
import java.util.StringTokenizer;

public class Parser {
  private String messageReceived;
  private int lastAccNum;
  private LinkedHashMap<Integer,Account> listOfAccounts;

  public Parser() {
    messageReceived = "";
    lastAccNum = (int) (Math.random() * 1000000);
    listOfAccounts = new LinkedHashMap<Integer,Account>();
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

  public String parseMessage(String message) {
    setMessageReceived(message);
    String reply = "";
    if (messageReceived.equalsIgnoreCase("000000")) {
      // Create a client and assign it a client id and return that to the server class
    } else {
      StringTokenizer stz = new StringTokenizer(message, "|");
      int requestId = Integer.parseInt(stz.nextToken());
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
        reply = requestId + "|" + accountNum + "|" + name + "|" + currency + "|" + balance + "|" + "Account for " + name + " created with Account Num " + accountNum;
      }
      else if (operation.equalsIgnoreCase("CloseAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
      } else if (operation.equalsIgnoreCase("DepositAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        currency = stz.nextToken();
        amount = Double.parseDouble(stz.nextToken());
      } else if (operation.equalsIgnoreCase("WithdrawAcc")) {
        name = stz.nextToken();
        password = stz.nextToken();
        accountNum = Integer.parseInt(stz.nextToken());
        currency = stz.nextToken();
        amount = Double.parseDouble(stz.nextToken());
      } else if (operation.equalsIgnoreCase("Monitor")) {
        time = Integer.parseInt(stz.nextToken());
      }
    }
    return reply;
  }

  public int openAccount(String name, String password, String currency, double balance) {
    int accountNum = getLastAccountNum();
    setLastAccountNum(getLastAccountNum() + 1);
    Account a = new Account(accountNum, name, password, currency, balance);

    return accountNum;
  }
}
