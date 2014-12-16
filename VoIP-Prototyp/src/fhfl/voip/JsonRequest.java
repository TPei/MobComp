package fhfl.voip;

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

/**
 * Macht ein Http-Request an die übergebene URL und übergibt die erhaltene IP oder bei
 * einer unerwarteten Antwort die komplette Response an den listener
 */
public class JsonRequest extends AsyncTask<String, String, String> {

	private static final String TAG = "fhfl.voip.JsonRequest";
	private int timeout = 2000;
	private URL url;

	/**
	 * listener der das Interface {@link AsyncTaskCompleted} implementiert
	 */
	private AsyncTaskCompleted listener;

	/**
	 * 
	 * @param url
	 *            - Url die angefragt wird
	 * @param listener
	 */
	public JsonRequest(String url, AsyncTaskCompleted listener) {
		Log.v(TAG, "JsonRequest Constr");
		setUrl(url);
		setListener(listener);
	}

	/**
	 * 
	 * @param url
	 *            - Url die angefragt wird
	 * @param timeout
	 *            - timeout in ms
	 * @param listener
	 */
	public JsonRequest(String url, int timeout, AsyncTaskCompleted listener) {
		Log.v(TAG, "JsonRequest Constr");
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
		Log.v(TAG, "setUrl(url): url = " + url);
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
		Log.v(TAG, "setListener(AsyncTaskCompleted listener)");
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected String doInBackground(String... params) {
		Log.v(TAG, "doInBackground()");
		try {
			// initialisiere Http-Verbindung
			HttpURLConnection httpConn = (HttpURLConnection) this.url
					.openConnection();
			httpConn.setRequestMethod("GET");
			httpConn.setRequestProperty("Content-length", "0");
			httpConn.setUseCaches(false);
			httpConn.setAllowUserInteraction(false);
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);

			// Verbinde
			httpConn.connect();

			// lies den Status Code aus lies die Response bei erfolgreicher
			// Antwort aus
			int status = httpConn.getResponseCode();
			Log.v(TAG, "HTTP-Response Code: " + status);
			switch (status) {
			case 200:
			case 201:
				// liest den inputstream aus und fügt diesen zusammen
				BufferedReader br = new BufferedReader(new InputStreamReader(
						httpConn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line.trim());
				}
				br.close();

				// es wird onPostExecute mit diesem Wert als Parameter
				// aufgerufen
				return sb.toString();
			}

		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
		Log.v(TAG, "onPostExecute(result): result = " + result);
		super.onPostExecute(result);

		// übergibt die ausgewertete Response an den listener, Auswertung siehe
		// getIpFromJson
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
		Log.v(TAG, "getIpFromJson(jsonString)");
		try {
			if (jsonString != null)
			{
				JSONObject json = new JSONObject(jsonString);
				if (json.has("ip")) {
					return json.getString("ip");
				} else if (json.has("query")) {
					return json.getString("query");
				} else {
					return json.toString();
				}
				
			}
			else return "";
		} catch (JSONException ex) {
			return jsonString;
		}
	}

}
