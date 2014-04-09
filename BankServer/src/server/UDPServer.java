package server;

import java.io.*;
import java.util.*;
import java.net.*;

/*
 * Class that communicates with the client using the UDP Transfer Protocol 
 */
class UDPServer {

	// Instance Variables
	protected static InetAddress ipAddress; 
	protected static int port;	
	protected static Timer timer = new Timer();
	protected static Parser parser = new Parser();
	protected static DatagramSocket serverSocket;
	protected static long systemTime = 0;
	protected static boolean useProbability = false;

	// Default constructor
	public UDPServer() throws SocketException {
		serverSocket = new DatagramSocket(9876); // Creates a new socket
													// connection listening to
													// port 9876
	}

	// Main method
	public static void main(String args[]) throws Exception {
		System.out.println("Running Server");
		UDPServer udp = new UDPServer();
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[10240];
		byte[] sendMonitorData = new byte[10240];
		String monitorResponse;
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			String sentence = new String(receivePacket.getData(),
					receivePacket.getOffset(), receivePacket.getLength()); // Extracting
																			// message
																			// into
																			// a
																			// String
																			// from
																			// the
																			// packet
			InetAddress IPAddress = receivePacket.getAddress();
			ipAddress = IPAddress; // Storing the IP Address of the incoming
									// message
			System.out.println("RECEIVED FROM IP ADDRESS "
					+ IPAddress.toString() + " :  " + sentence);
			port = receivePacket.getPort(); // Storing the port of the incoming
											// message
			String decryptedSentence = Parser.decrypt(sentence); // Decrypting
																	// incoming
																	// message
			System.out.println("DECRYPTED MESSAGE FROM IP ADDRESS "
					+ IPAddress.toString() + " :  " + decryptedSentence);
			String serverResponse = parser.parseMessage(decryptedSentence); // Generating
																			// server
																			// response
																			// to
																			// messsage
																			// received
			String encryptedResponse = "";
			if(!useProbability || decryptedSentence.equals("000000")){
			System.out.println("SERVER MESSAGE TO IP ADDRESS "
					+ IPAddress.toString() + " : " + serverResponse);
			encryptedResponse = Parser.encrypt(serverResponse); // Encrypting
																		// message
																		// to be
																		// sent
			System.out.println("SENT TO IP ADDRESS " + IPAddress.toString()
					+ " : " + encryptedResponse);
			sendData = encryptedResponse.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket); // Sending the message in a packet to
											// the IP Address and port from
											// which the message was received
			}
			else{
				double prob = Math.random();
				if(prob <= 0.5){
					System.out.println("SERVER MESSAGE TO IP ADDRESS "
							+ IPAddress.toString() + " : " + serverResponse);
					encryptedResponse = Parser.encrypt(serverResponse); // Encrypting
																				// message
																				// to be
																				// sent
					System.out.println("SENT TO IP ADDRESS " + IPAddress.toString()
							+ " : " + encryptedResponse);
					sendData = encryptedResponse.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(sendData,
							sendData.length, IPAddress, port);
					serverSocket.send(sendPacket); // Sending the message in a packet to
													// the IP Address and port from
													// which the message was received
				}
				else{
					System.out.println("The following message was not sent to the client to simulate packetloss :" + serverResponse);
					System.out.println("Probability: " + Double.toString(prob*100).substring(0,Double.toString(prob*100).indexOf(".")+3) + " %");
				}
				
			}
			// Sending data to monitors if they exist
			for (Map.Entry entry : parser.listOfClients.entrySet()) {
				if (((Client) entry.getValue()).getIsMonitor()) {
					monitorResponse = encryptedResponse; 
					sendMonitorData = monitorResponse.getBytes();
					DatagramPacket monitorSendPacket = new DatagramPacket(
							sendMonitorData, sendMonitorData.length,
							((Client) entry.getValue()).getIPAddress(),
							((Client) entry.getValue()).getPort());
					System.out
							.println("SERVER MESSAGE TO MONITOR AT IP ADDRESS "
									+ ((Client) entry.getValue())
											.getIPAddress().toString() + " : "
									+ serverResponse);
					System.out.println("PORT = "
							+ ((Client) entry.getValue()).getPort());
					System.out.println("SENT TO MONITOR AT IP ADDRESS "
							+ ((Client) entry.getValue()).getIPAddress()
									.toString() + " : " + encryptedResponse);
					udp.serverSocket.send(monitorSendPacket); // Message packets
																// sent to
																// monitor
				}
			}
		}
	}
}

/*
 * Class that implements the Timer function of the monitor
 */
class MonitorTimer extends TimerTask {

	// Method invoked on completion of the timer to set the monitor to false and
	// send it a final closing message
	public void run() {
		String serverResponse = "Monitor time interval has ended. You shall receive no further messages...";
		String encryptedResponse = Parser.encrypt(serverResponse);
		byte[] sendData = new byte[1024];
		sendData = encryptedResponse.getBytes();
		UDPServer.systemTime++;
		for (Map.Entry entry : UDPServer.parser.listOfClients.entrySet()) {
			if (((Client) entry.getValue()).getIsMonitor() && ((Client) entry.getValue()).getEndTime()<=UDPServer.systemTime) {
				((Client) entry.getValue()).setIsMonitor(false); // Set monitor
																	// status to
																	// false
				DatagramPacket monitorSendPacket = new DatagramPacket(sendData,
						sendData.length,
						((Client) entry.getValue()).getIPAddress(),
						((Client) entry.getValue()).getPort());
				System.out.println("SERVER MESSAGE TO MONITOR AT IP ADDRESS "
						+ ((Client) entry.getValue()).getIPAddress().toString()
						+ " : " + serverResponse);
				System.out.println("SENT TO MONITOR AT IP ADDRESS "
						+ ((Client) entry.getValue()).getIPAddress().toString()
						+ " : " + encryptedResponse);
				try {
					UDPServer.serverSocket.send(monitorSendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//			UDPServer.timer.cancel();
		}
	}
}