package server;

import java.io.*;
import java.util.*;
import java.net.*;

class UDPServer {
  
  protected static InetAddress ipAddress;
  
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
      System.out.println("RECEIVED: " + sentence);
      
      InetAddress IPAddress = receivePacket.getAddress();
      ipAddress = IPAddress;
      int port = receivePacket.getPort();
      String serverResponse = parser.parseMessage(sentence);      
      sendData = serverResponse.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
      serverSocket.send(sendPacket);
    }
  }
}
