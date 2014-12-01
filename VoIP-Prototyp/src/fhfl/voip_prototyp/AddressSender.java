package fhfl.voip_prototyp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import android.util.Log;

class AddressSender extends Thread {
	private final static String TAG = "fhfl.voip_prototyp.AddressSender";
	final int remotePort = 50500;
	
	private String remoteIP = null;
	
	private DatagramSocket dSocket = null;
	private DatagramPacket outgoingPacket = null;
	private DatagramPacket incomingPacket = null;
	
	private String addressString = null;
			
	public AddressSender(String remoteIP, String ownIP, int ownPort) {
		this.remoteIP = remoteIP;
		addressString = ownIP + ":" + ownPort;
		try {
			dSocket = new DatagramSocket();
			outgoingPacket = new DatagramPacket(addressString.getBytes(), addressString.getBytes().length, new InetSocketAddress(remoteIP, remotePort));
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception");
		}
	}	
	
	private void send() throws IOException {
		Log.v(TAG, "send(" + remoteIP +") with data " + addressString);
		dSocket.send(outgoingPacket);
	}
	
	public void run() {
		try {
			send();
		} catch (IOException e) {
			Log.e(TAG, "Exception in Method send()");
		}
	}
}
