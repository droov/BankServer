package server;

import java.io.*;
import java.util.*;
import java.net.*;

class UDPServer {
  
  protected static InetAddress ipAddress;
  protected static int port;
  
  public static void main(String args[]) throws Exception {
    System.out.println("Running Server");
    DatagramSocket serverSocket = new DatagramSocket(9876);
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    
    Parser parser = new Parser();    
    while (true) {
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      serverSocket.receive(receivePacket);
      String sentence =
          new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
            
      InetAddress IPAddress = receivePacket.getAddress();
      ipAddress = IPAddress;
      System.out.println("RECEIVED FROM IP ADDRESS " + IPAddress.toString() +  " :  " + sentence);
      port = receivePacket.getPort();
      System.out.println("PORT: " + port);
      String serverResponse = parser.parseMessage(sentence);
      System.out.println("SENT TO IP ADDRESS " + IPAddress.toString() + " : " + serverResponse);
      sendData = serverResponse.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      serverSocket.send(sendPacket);
      
      // Sending data to monitors if they exist
      for (Map.Entry entry : parser.listOfClients.entrySet()) {
        if(((Client)entry.getValue()).getIsMonitor()){
          try{
            Thread.sleep(5000);
         }catch(InterruptedException ex){
            ex.printStackTrace();
         }
          //DatagramPacket monitorSendPacket = new DatagramPacket(sendData, sendData.length, ((Client)entry.getValue()).getIPAddress(), ((Client)entry.getValue()).getPort());
          DatagramPacket monitorSendPacket = new DatagramPacket(sendData, sendData.length, ((Client)entry.getValue()).getIPAddress(), 9877);
          System.out.println("SENT TO MONITOR AT IP ADDRESS " + ((Client)entry.getValue()).getIPAddress().toString() + " : " + serverResponse);
          serverSocket.send(monitorSendPacket);
        }         
      }
    }
  }
}
