package client;

import java.io.*;
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
			String encryptedSentence = encrypt(sentence);
			System.out.println("SENT TO SERVER: " + encryptedSentence);
			sendData = encryptedSentence.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, 9876);
			clientSocket.send(sendPacket);
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			String modifiedSentence = new String(receivePacket.getData());			
			System.out.println("RECEIVED FROM SERVER: " + modifiedSentence);
			String decryptedSentence = decrypt(modifiedSentence.trim());
			System.out.println("DECRYPTED MESSAGE: " + decryptedSentence);
			clientSocket.close();
			if (decryptedSentence.contains("Client has been set as a monitor")) {
				monitor();
			}
		}
	}

	public static void monitor() throws Exception {
		System.out.println("Running Monitor Server");
		DatagramSocket serverSocket = new DatagramSocket(9877);
		byte[] receiveData = new byte[1024];
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(),
					receivePacket.getOffset(), receivePacket.getLength());
			System.out.println("RECEIVED FROM SERVER: " + sentence.trim());
			String decryptedSentence = decrypt(sentence.trim());
			System.out.println("DECRYPTED MESSAGE: " + decryptedSentence);
		}
	}
	
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
