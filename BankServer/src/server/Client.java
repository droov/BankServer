package server;

import java.net.InetAddress;

/*
 * Class that maintains details of a Client 
 */
public class Client {
	// Instance Variables
	private int clientID;
	private boolean isMonitor;
	private int port;
	private InetAddress ipAddress;

	// Parametrized Constructor
	Client(int clientID, boolean isMonitor, InetAddress ipAddress, int port) {
		this.clientID = clientID;
		this.isMonitor = isMonitor;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	// Get and set methods for instance variables
	public int getClientID() {
		return clientID;
	}

	public boolean getIsMonitor() {
		return isMonitor;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public InetAddress getIPAddress() {
		return ipAddress;
	}

	public void setClientID(int clientID) {
		this.clientID = clientID;
	}

	public void setIsMonitor(boolean isMonitor) {
		this.isMonitor = isMonitor;
	}

	public void setIPAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
}