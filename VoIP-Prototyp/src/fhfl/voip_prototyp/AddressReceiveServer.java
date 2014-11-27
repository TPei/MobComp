package fhfl.voip_prototyp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AddressReceiveServer extends Thread{
	MainActivity activity;
	DatagramSocket dSocket;
	DatagramPacket incomingPacket;
	final int bufferSize = 1024;
	byte[] buffer = new byte[bufferSize];
	
	final int port = 50500;
	
	public AddressReceiveServer(MainActivity activity) throws IOException {
		this.activity = activity;
		//hier noch checken, an welche Interfaces gebunden wird
		dSocket = new DatagramSocket(port);
		incomingPacket = new DatagramPacket(buffer, bufferSize);
	}
	
	private void receive() throws IOException {
		dSocket.receive(incomingPacket);
		String message = new String(incomingPacket.getData(), 0 , incomingPacket.getLength());
		String remoteIP = message.substring(0, message.indexOf(":"));
		int remotePort = Integer.parseInt(message.substring(message.indexOf(":")+1));
		activity.setRemoteAddress(remoteIP, remotePort);
	}

	@Override
	public void run() {
		try {
			receive();
		} catch (IOException e) {
		}
		
	}
}
