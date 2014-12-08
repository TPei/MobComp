package fhfl.voip;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.util.Log;

/**
 * Klasse zum Empfangen eines UDP-Pakets auf Port 50500
 * Liest aus der Nachricht IP und Port und übergibt diese an die MainActivity
 *
 */
public class AddressReceiver extends Thread {
	private final static String TAG = "fhfl.voip_prototyp.AddressReceiver";
	private MainActivity activity;
	private DatagramSocket dSocket;
	private DatagramPacket incomingPacket;
	private final int bufferSize = 1024;
	private byte[] buffer = new byte[bufferSize];
	
	private final int port = 50500;
	
	public AddressReceiver(MainActivity activity) {
		this.activity = activity;
		try 
		{
			dSocket = new DatagramSocket(port);
			incomingPacket = new DatagramPacket(buffer, bufferSize);
		} 
		catch (Exception e) 
		{
			Log.e(TAG, "Exception");
		}
	}
	
	private void receive() throws IOException {
		Log.v(TAG, "receive()");
		dSocket.receive(incomingPacket);
		String message = new String(incomingPacket.getData(), 0 , incomingPacket.getLength());
		String remoteIP = message.substring(0, message.indexOf(":"));
		int remotePort = Integer.parseInt(message.substring(message.indexOf(":")+1));
		activity.setRemoteAddress(remoteIP, remotePort);
	}

	@Override
	public void run() {
		try 
		{
			receive();
		} 
		catch (Exception e) 
		{
			Log.e(TAG, "Exception in method receive()");
		}		
	}
}
