package de.onyxbits.remotekeyboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Populate the replacement database by downloading a JSON file from a
 * webserver.
 * 
 * @author patrick
 * 
 */
class ImportTask extends AsyncTask<String, Integer, String> {

	public static final String TAG="de.onyxbits.remotekeyboard.ImportTask";
	
	private ReplacementsListActivity master;

	public ImportTask(ReplacementsListActivity master) {
		this.master = master;
	}

	@Override
	public void onPreExecute() {
		master.setProgressBarIndeterminate(true);
		master.setProgressBarVisibility(true);
	}

	@Override
	protected void onPostExecute(String result) {
		master.setProgressBarIndeterminate(false);
		master.setProgressBarVisibility(false);
		SQLiteDatabase database = new Schema(master).getReadableDatabase();
		master.load(database);
		database.close();
		Toast.makeText(master, result, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected String doInBackground(String... urls) {
		String result = "Success!";
		try {
			URL url = new URL(urls[0]);
			InputStream is = url.openStream(); // throws an IOException
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			JSONObject json = new JSONObject(sb.toString());
			Schema dbHelper = new Schema(master);
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			@SuppressWarnings("unchecked")
			Iterator <String>it = json.keys();
			while (it.hasNext()) {
				String key = it.next();
				ContentValues values = new ContentValues();
				values.put(Schema.COLUMN_KEY, key);
				values.put(Schema.COLUMN_VALUE, json.getString(key));
				database.insert(Schema.TABLE_REPLACEMENTS, null, values);
			}
			database.close();
		}
		catch (MalformedURLException exp) {
			result=master.getString(R.string.err_malformed_url);
			Log.w(TAG,exp);
		}
		catch (IOException exp) {
			result=master.getString(R.string.err_network_error);
			Log.w(TAG,exp);
		}
		catch (JSONException exp) {
			result = master.getString(R.string.err_corrupt_data);
			Log.w(TAG,exp);
		}
		return result;
	}

}
