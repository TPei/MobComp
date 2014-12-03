package fhfl.voip_prototyp;

import java.net.InetAddress;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.rtp.AudioCodec;
import android.net.rtp.AudioGroup;
import android.net.rtp.AudioStream;
import android.net.rtp.RtpStream;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements AsyncTaskCompleted {

	private static final String TAG = "fhfl.voip_prototyp.MainActivity";
	
	private final String[] ipProvider = {
			"http://ipinfo.io/json", //ganzes json
			"http://ipinfo.io/ip", // nur ip als string
			"http://ip-api.com/json",
			"http://trackip.net/ip?json"
			
	};

	private AudioGroup audioGroup;
	private AudioStream voipStream;
	
	private AudioManager audioManager;

	private TextView addressField;
	private EditText addressInput;
	private EditText portInput;
	private TextView callStateInfo;
	private Button connectButton;
	private int port;
	private String ip;

	private enum CALL_STATES {
		IDLE, CONNECTED
	};

	private CALL_STATES callState = CALL_STATES.IDLE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
			
		
		// get the view elements
		addressField = (TextView) (findViewById(R.id.addressField));
		addressInput = (EditText) (findViewById(R.id.addressInput));
		portInput = (EditText) (findViewById(R.id.portInput));
		callStateInfo = (TextView) (findViewById(R.id.callState));
		connectButton = (Button) (findViewById(R.id.connectButton));

		// get connection info
		WifiManager wifiMan = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInf = wifiMan.getConnectionInfo();
		int ipAddress = wifiInf.getIpAddress();
		ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
				(ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
				(ipAddress >> 24 & 0xff));
		
		audioManager = (AudioManager)((Context)this).getSystemService(Context.AUDIO_SERVICE);

		audioGroup = new AudioGroup();
		audioGroup.setMode(AudioGroup.MODE_NORMAL);
		try {
			voipStream = new AudioStream(InetAddress.getByName(ip));
			voipStream.setCodec(AudioCodec.GSM);
			voipStream.setMode(RtpStream.MODE_NORMAL);

			port = voipStream.getLocalPort();
			addressField.setText("Eigene Adresse: " + ip + " : " + port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * call button was clicked
	 * 
	 * @param view
	 */
	public void callButtonClick(View view) {
		Log.v(TAG, "callButtonClick(): callState = " + callState);

		if (audioGroup != null && voipStream != null) {
			switch (callState) {
			case IDLE:
				connectCall();
				break;

			case CONNECTED:
				disconnectCall();
				break;
			}
		}
	}

	/**
	 * audio Mode Button was clicked
	 * 
	 * @param view
	 */
	public void audioModeBtnClick(View view) {
		final int[] modes = { AudioGroup.MODE_ECHO_SUPPRESSION,
				AudioGroup.MODE_MUTED, AudioGroup.MODE_NORMAL,
				AudioGroup.MODE_ON_HOLD };
		CharSequence[] items = { "MODE_ECHO_SUPPRESSION", "MODE_MUTED",
				"MODE_NORMAL", "MODE_ON_HOLD" };
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Mode").setItems(items,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// The 'which' argument contains the index position
						// of the selected item
						// int läuft von 0 bis length-1
						Log.d(TAG, "Itemclicked: " + which);
						// audioGroup.setMode(modes[which]);
					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	/**
	 * share IP via SMS
	 * @param view
	 */
	public void shareIPButtonClick(View view){
		/*
		Intent smsIntent = new Intent(Intent.ACTION_VIEW);
		smsIntent.setData(Uri.parse("sms:"));
		smsIntent.putExtra("sms_body", "Hey! Lass und Voipen. Meine ip ist: " + ip + " und mein Port ist: " + port); 
		startActivity(smsIntent);*/
		
		// bei mir auf dem Emulator startet das eine SMS und auf lollipop wo das failed eine email
		Intent intent=new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with it.
		intent.putExtra(Intent.EXTRA_TEXT, "Hey! Lass uns Voipen. Meine ip ist: " + ip + " und mein Port ist: " + port);

		startActivity(Intent.createChooser(intent, "Choose sharing action"));
		
		//startet Reiceiver zum Empfangen der Remote-Adresse
		new AddressReceiver(this).start();
		Toast.makeText(this, "AddressReceiver gestartet!", Toast.LENGTH_LONG).show();


	}

	/**
	 * join voidStream for call
	 */
	public void connectCall() {
		String remoteIP = addressInput.getText().toString();
		int remotePort = Integer.parseInt(portInput.getText().toString());

		try {
			// connect stream to remote client
			voipStream.associate(InetAddress.getByName(remoteIP), remotePort);
			voipStream.join(audioGroup);

			callState = CALL_STATES.CONNECTED;
			callStateInfo.setText("Status: CONNECTED");
			connectButton.setText("Disconnect");
			
			audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		} catch (Exception e) {
			Log.e(TAG, "Exception:" + e.getStackTrace());
		}
	}

	/**
	 * disconnect from voipStream
	 */
	public void disconnectCall() {
		// leave group
		voipStream.join(null);
		audioGroup.clear();

		callState = CALL_STATES.IDLE;
		callStateInfo.setText("Status: IDLE");
		connectButton.setText("Connect");
		
		audioManager.setMode(AudioManager.MODE_NORMAL);
	}

	/**
	 * Clickhandler
	 * @param view
	 */
	public void ipBtnClick (View view) {
		new JsonRequest(ipProvider[1], this).execute();
	}
	
	/**
	 * wird aufgerufen wenn die Html Request abgeschlossen ist
	 */
	@Override
	public void onTaskCompleted(String result) {
		// schreib das ergebnis in das ip feld
		ip = result;
		addressField.setText("Eigene Adresse: " + ip + " : " + port);
	}
	
	/**
	 * wird aufgerufen, wenn der AddressReceiverServer Adressdaten erh�lt
	 * @param remoteIP IP unter der der Remote-Client erreichbar ist
	 * @param remotePort Port unter dem der Remote-Client erreichbar ist
	 */
	protected void setRemoteAddress(String remoteIP, int remotePort) {
		Log.v(TAG, "setRemoteAddress() " + remoteIP + ":" + remotePort);
		//hier den stream associaten
		Toast.makeText(this, "Empfangen: " + remoteIP + ":" + remotePort, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * for testing....
	 */
	public void sendIP(View view) {
		String remIP = addressInput.getText().toString();
		new AddressSender(remIP, ip, port).start();
		Toast.makeText(this, "Gesendet: " + ip + ":" + port + " an " + remIP, Toast.LENGTH_LONG).show();
		Log.v(TAG, "sendIP() an " + remIP);
	}
}
