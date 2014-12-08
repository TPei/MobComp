package fhfl.voip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import android.util.Log;

/**
 * Klasse zum Versenden der eigenen IP und Port an einen Remoteclient über UDP Port 50500
 *
 */
class AddressSender extends Thread {
	private final static String TAG = "fhfl.voip_prototyp.AddressSender";
	final int remotePort = 50500;
	
	private String remoteIP = null;
	
	private DatagramSocket dSocket = null;
	private DatagramPacket outgoingPacket = null;
	
	private String addressString = null;
			
	public AddressSender(String remoteIP, String ownIP, int ownPort) {
		this.remoteIP = remoteIP;
		addressString = ownIP + ":" + ownPort;
		try 
		{
			dSocket = new DatagramSocket();
			outgoingPacket = new DatagramPacket(addressString.getBytes(), addressString.getBytes().length, new InetSocketAddress(remoteIP, remotePort));
		}
		catch (Exception e)
		{
			Log.e(TAG, "Exception in Constructor");
		}
	}	
	
	private void send() throws IOException 
	{
		Log.v(TAG, "send(" + remoteIP +") with data " + addressString);
		dSocket.send(outgoingPacket);
	}
	
	public void run() {
		try 
		{
			send();
		} 
		catch (IOException e) 
		{
			Log.e(TAG, "Exception in Method send()");
		}
	}
}
