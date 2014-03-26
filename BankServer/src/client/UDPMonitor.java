package client;

import java.io.*;
import java.util.*;
import java.net.*;

class UDPMonitor {

	public static void main(String args[]) throws Exception {
		DatagramSocket clientSocket;
		while (true) {
			System.out.println("Running Client");
			BufferedReader inFromUser = new BufferedReader(
					new InputStreamReader(System.in));
			clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			String sentence = inFromUser.readLine();
			sendData = sentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9876);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());			
			System.out.println("FROM SERVER:" + modifiedSentence);
			clientSocket.close();
			if (modifiedSentence.contains("Client has been set as a monitor")) {
				monitor();
			}
		}
	}

	public static void monitor() throws Exception {
		System.out.println("Running Monitor Server");
		DatagramSocket serverSocket = new DatagramSocket(9877);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(),
					receivePacket.getOffset(), receivePacket.getLength());
			System.out.println("RECEIVED: " + sentence);
		}
	}
}
