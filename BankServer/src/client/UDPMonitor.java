package client;


import java.io.*;
import java.util.*;
import java.net.*; 

class UDPMonitor {    
  public static void main(String args[]) throws Exception {
    System.out.println("Running Server");
    DatagramSocket serverSocket = new DatagramSocket(9876);             
    byte[] receiveData = new byte[1024];             
    byte[] sendData = new byte[1024];             
    while(true) {                   
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);                   
      serverSocket.receive(receivePacket);                   
      String sentence = new String( receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());                   
      System.out.println("RECEIVED: " + sentence);                   
      InetAddress IPAddress = receivePacket.getAddress();                   
      int port = receivePacket.getPort();                   
      String capitalizedSentence = sentence.toUpperCase();                   
      sendData = capitalizedSentence.getBytes();                   
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);                   
      serverSocket.send(sendPacket);      
     }
       
    
  } 
} 