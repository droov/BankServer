package server;

import java.net.InetAddress;

public class Client {
	private int clientID;
	private boolean isMonitor;
	private InetAddress ipAddress;
	
	Client() {
		//default constructor
	}
	
	Client(int clientID, boolean isMonitor, InetAddress ipAddress) {
		this.clientID = clientID;
		this.isMonitor = isMonitor;
		this.ipAddress = ipAddress;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public boolean getIsMonitor() {
		return isMonitor;
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
