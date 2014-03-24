package server;

import java.io.*;
import java.util.*;
import java.net.*;

class UDPServer {

  protected static InetAddress ipAddress;
  protected static int port;
  protected static Timer timer = new Timer();
  protected static Parser parser = new Parser();

  public static void main(String args[]) throws Exception {
    System.out.println("Running Server");
    DatagramSocket serverSocket = new DatagramSocket(9876);
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];



    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      String sentence =
          new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());

      InetAddress IPAddress = receivePacket.getAddress();
      ipAddress = IPAddress;
      System.out.println("RECEIVED FROM IP ADDRESS " + IPAddress.toString() + " :  " + sentence);
      port = receivePacket.getPort();
      System.out.println("PORT: " + port);
      String serverResponse = parser.parseMessage(sentence);
      System.out.println("SENT TO IP ADDRESS " + IPAddress.toString() + " : " + serverResponse);
      sendData = serverResponse.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      serverSocket.send(sendPacket);

      // Sending data to monitors if they exist
      for (Map.Entry entry : parser.listOfClients.entrySet()) {
        if (((Client) entry.getValue()).getIsMonitor()) {
          DatagramPacket monitorSendPacket =
              new DatagramPacket(sendData, sendData.length,
                  ((Client) entry.getValue()).getIPAddress(), 9877);
          System.out.println("SENT TO MONITOR AT IP ADDRESS "
              + ((Client) entry.getValue()).getIPAddress().toString() + " : " + serverResponse);
          serverSocket.send(monitorSendPacket);
        }
      }
    }
  }
}

class MonitorTimer extends TimerTask
{
  public void run() {    
    for (Map.Entry entry : UDPServer.parser.listOfClients.entrySet()) {
      if (((Client) entry.getValue()).getIsMonitor()) {
        ((Client) entry.getValue()).setIsMonitor(false);
        System.out.println("Timer has ended");
      }
    }
    UDPServer.timer.cancel();
  }
}
