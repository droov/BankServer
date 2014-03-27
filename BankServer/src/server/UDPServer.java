package server;

import java.io.*;
import java.util.*;
import java.net.*;

/*
 * Class that communicates with the client using the UDP Transfer Protocol 
 */
class UDPServer {

	protected static InetAddress ipAddress;
	protected static int port;
	protected static Timer timer = new Timer();
	protected static Parser parser = new Parser();
	protected static DatagramSocket serverSocket;

	public UDPServer() throws SocketException {
		serverSocket = new DatagramSocket(9876);
	}

	public static void main(String args[]) throws Exception {
		System.out.println("Running Server");
		UDPServer udp = new UDPServer();
		// DatagramSocket serverSocket = new DatagramSocket(9876);
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		byte[] sendMonitorData = new byte[1024];
		String monitorResponse;
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(),
					receivePacket.getOffset(), receivePacket.getLength());

			InetAddress IPAddress = receivePacket.getAddress();
			ipAddress = IPAddress;
			System.out.println("RECEIVED FROM IP ADDRESS "
					+ IPAddress.toString() + " :  " + sentence);
			port = receivePacket.getPort();
			String decryptedSentence = Parser.decrypt(sentence);
			System.out.println("DECRYPTED MESSAGE FROM IP ADDRESS "
					+ IPAddress.toString() + " :  " + decryptedSentence);
			String serverResponse = parser.parseMessage(decryptedSentence);
			System.out.println("SERVER MESSAGE TO IP ADDRESS " + IPAddress.toString()
					+ " : " + serverResponse);
			String encryptedResponse = Parser.encrypt(serverResponse);
			System.out.println("SENT TO IP ADDRESS " + IPAddress.toString()
					+ " : " + encryptedResponse);
			sendData = encryptedResponse.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

			// Sending data to monitors if they exist
			for (Map.Entry entry : parser.listOfClients.entrySet()) {
				if (((Client) entry.getValue()).getIsMonitor()) {
					monitorResponse = encryptedResponse + "\r\n";
					sendMonitorData = monitorResponse.getBytes();
					DatagramPacket monitorSendPacket = new DatagramPacket(
							sendMonitorData, sendMonitorData.length,
							((Client) entry.getValue()).getIPAddress(), ((Client) entry.getValue()).getPort());					
					System.out.println("SERVER MESSAGE TO MONITOR AT IP ADDRESS "
							+ ((Client) entry.getValue()).getIPAddress().toString()
							+ " : " + serverResponse);
					System.out.println("PORT = " + ((Client) entry.getValue()).getPort());
					System.out.println("SENT TO MONITOR AT IP ADDRESS "
							+ ((Client) entry.getValue()).getIPAddress()
									.toString() + " : " + encryptedResponse);
					udp.serverSocket.send(monitorSendPacket);
				}
			}
		}
	}
}

class MonitorTimer extends TimerTask {
	public void run() {
		String serverResponse = "Monitor time interval has ended. You shall receive no further messages...";
		String encryptedResponse = Parser.encrypt(serverResponse);
		byte[] sendData = new byte[1024];
		sendData = encryptedResponse.getBytes();

		for (Map.Entry entry : UDPServer.parser.listOfClients.entrySet()) {
			if (((Client) entry.getValue()).getIsMonitor()) {
				((Client) entry.getValue()).setIsMonitor(false);
				DatagramPacket monitorSendPacket = new DatagramPacket(sendData,
						sendData.length,
						((Client) entry.getValue()).getIPAddress(), ((Client) entry.getValue()).getPort());
				System.out.println("SERVER MESSAGE TO MONITOR AT IP ADDRESS "
						+ ((Client) entry.getValue()).getIPAddress().toString()
						+ " : " + serverResponse);
				System.out.println("SENT TO MONITOR AT IP ADDRESS "
						+ ((Client) entry.getValue()).getIPAddress().toString()
						+ " : " + encryptedResponse);
				try {
					UDPServer.serverSocket.send(monitorSendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			UDPServer.timer.cancel();
		}
	}
}