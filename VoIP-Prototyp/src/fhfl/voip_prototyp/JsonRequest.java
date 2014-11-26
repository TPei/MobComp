package fhfl.voip_prototyp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.util.Log;

// Params,
// Progress,
// Result
/**
 * Macht ein Html Request an die übergeben url, erwartet einen einfachen String
 * i.e. parsed kein JSON
 * 
 * @author Lennart
 *
 */
public class JsonRequest extends AsyncTask<String, String, String> {

	private static final String TAG = "fhfl.voip_prototyp.GetJson";
	private int timeout = 2000;
	private URL url;
	private AsyncTaskCompleted listener;

	/**
	 * 
	 * @param url
	 *            - Url die angefragt wird
	 */
	public JsonRequest(String url) {
		setUrl(url);
	}

	/**
	 * 
	 * @param url
	 * @param timeout
	 */
	public JsonRequest(String url, int timeout) {
		setUrl(url);
		this.timeout = timeout;
	}

	/**
	 * Erstellt URL Object mit dem übergebenen String
	 * 
	 * @param url
	 */
	private void setUrl(String url) {
		try {
			this.url = new URL(url);
		} catch (MalformedURLException ex) {
			Log.e(TAG, ex.getMessage());
		}
	}

	/**
	 * Setzt den Listener auf dem onTaskCompleted aufgerufen wird. wenn die
	 * request fertig ist
	 * 
	 * @param listener
	 * @return JsonRequest
	 */
	public JsonRequest setListener(AsyncTaskCompleted listener) {
		this.listener = listener;
		return this;
	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected String doInBackground(String... params) {
		try {
			HttpURLConnection httpConn = (HttpURLConnection) this.url
					.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Content-length", "0");
			httpConn.setUseCaches(false);
			httpConn.setAllowUserInteraction(false);
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);
			httpConn.connect();
			int status = httpConn.getResponseCode();

			switch (status) {
			case 200:
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line.trim()); // + "\n"
				}
				br.close();
				return sb.toString();
			}

		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		// irgendwo muss das ergebnis noch auf ein ui element gesetzt werden
		Log.d(TAG, result);
		listener.onTaskCompleted(result);
	}

}
