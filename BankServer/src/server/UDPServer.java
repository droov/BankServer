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
			String serverResponse = parser.parseMessage(sentence);		
			System.out.println("SENT TO IP ADDRESS " + IPAddress.toString()
					+ " : " + serverResponse);
			sendData = serverResponse.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, port);
			serverSocket.send(sendPacket);

			// Sending data to monitors if they exist
			for (Map.Entry entry : parser.listOfClients.entrySet()) {
				if (((Client) entry.getValue()).getIsMonitor()) {
					monitorResponse = serverResponse + "\r\n";
					sendMonitorData = monitorResponse.getBytes();
					DatagramPacket monitorSendPacket = new DatagramPacket(
							sendMonitorData, sendMonitorData.length,
							((Client) entry.getValue()).getIPAddress(), 9877);
					System.out.println("SENT TO MONITOR AT IP ADDRESS "
							+ ((Client) entry.getValue()).getIPAddress()
									.toString() + " : " + serverResponse);
					udp.serverSocket.send(monitorSendPacket);
				}
			}
		}
	}
}

class MonitorTimer extends TimerTask {
	public void run() {
		String serverResponse = "Monitor time interval has ended. You shall receive no further messages...";
		byte[] sendData = new byte[1024];
		sendData = serverResponse.getBytes();

		for (Map.Entry entry : UDPServer.parser.listOfClients.entrySet()) {
			if (((Client) entry.getValue()).getIsMonitor()) {
				((Client) entry.getValue()).setIsMonitor(false);
				DatagramPacket monitorSendPacket = new DatagramPacket(sendData,
						sendData.length,
						((Client) entry.getValue()).getIPAddress(), 9877);
				System.out.println("SENT TO MONITOR AT IP ADDRESS "
						+ ((Client) entry.getValue()).getIPAddress().toString()
						+ " : " + serverResponse);
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