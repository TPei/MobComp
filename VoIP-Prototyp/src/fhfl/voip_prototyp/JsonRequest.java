package fhfl.voip_prototyp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

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
	public JsonRequest(String url, AsyncTaskCompleted listener) {
		setUrl(url);
		setListener(listener);
	}

	/**
	 * 
	 * @param url
	 * @param timeout
	 */
	public JsonRequest(String url, int timeout, AsyncTaskCompleted listener) {
		setUrl(url);
		setListener(listener);
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
	public void setListener(AsyncTaskCompleted listener) {
		this.listener = listener;
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
				// liest den inputstream aus und fügt diesen zusammen
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
		Log.d(TAG, result);
		listener.onTaskCompleted(getIpFromJson(result));
	}

	/**
	 * Gibt wenn ein json String übergeben wird, die Values die in dem Feld "ip"
	 * oder "query" stehen zurück. Wenn keins der beiden Felder vorhanden ist
	 * wird der komplette String zurückgegeben. Wenn der übergebene String kein
	 * Json ist wird dieser komplett zurückgegeben.
	 * 
	 * @param jsonString
	 * @return IP-Address as String
	 */
	private String getIpFromJson(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			if (json.has("ip")) {
				return json.getString("ip");
			} else if (json.has("query")) {
				return json.getString("query");
			} else {
				return json.toString();
			}
		} catch (JSONException ex) {
			return jsonString;
		}
	}

}
