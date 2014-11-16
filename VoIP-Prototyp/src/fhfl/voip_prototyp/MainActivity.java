package fhfl.voip_prototyp;


import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
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


public class MainActivity extends Activity {	
	
	private static final String TAG = "fhfl.voip_prototyp.MainActivity";
	
	private AudioGroup audioGroup;
	private AudioStream voipStream;
	
	private TextView addressField;
	private EditText addressInput;
	private EditText portInput;
	private TextView callStateInfo;
	private Button connectButton;
	
	private enum CALL_STATES {IDLE, CONNECTED};
	private CALL_STATES callState = CALL_STATES.IDLE; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //get the view elements
        addressField = (TextView)(findViewById(R.id.addressField));
        addressInput = (EditText)(findViewById(R.id.addressInput));
        portInput = (EditText)(findViewById(R.id.portInput));
        callStateInfo = (TextView)(findViewById(R.id.callState));
        connectButton = (Button)(findViewById(R.id.connectButton));
        
        //get connection info
        WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        
        audioGroup = new AudioGroup();
        audioGroup.setMode(AudioGroup.MODE_ECHO_SUPPRESSION);
        try
        {
        	 voipStream = new AudioStream(InetAddress.getByName(ip));        	
        	 voipStream.setCodec(AudioCodec.AMR);
        	 voipStream.setMode(RtpStream.MODE_NORMAL);
        	 
        	 int port = voipStream.getLocalPort();
        	 addressField.setText("Eigene Adresse: " + ip + " : " + port);
        }
        catch (Exception e)
        {
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
     * @param view
     */
    public void callButtonClick(View view)
    {
    	Log.v(TAG, "callButtonClick(): callState = " + callState);
    	
    	if (audioGroup != null && voipStream != null)
    	{    		
    		switch(callState) 
        	{
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
     * join voidStream for call
     */
    public void connectCall()
    {
    	String remoteIP = addressInput.getText().toString();
		int remotePort = Integer.parseInt(portInput.getText().toString());
		
		try 
		{
			//connect stream to remote client
			voipStream.associate(InetAddress.getByName(remoteIP), remotePort);
			voipStream.join(audioGroup);    
			
			callState = CALL_STATES.CONNECTED;
    		callStateInfo.setText("Status: CONNECTED");
    		connectButton.setText("Disconnect");
		} 
		catch (Exception e) 
		{
			Log.e(TAG, "Exception:" + e.getStackTrace());
		}  
    }
    
    /**
     * disconnect from voipStream
     */
    public void disconnectCall()
    {
    	//leave group
		voipStream.join(null);	
		
		callState = CALL_STATES.IDLE;
		callStateInfo.setText("Status: IDLE");
		connectButton.setText("Connect");
    }
}
