package de.onyxbits.remotekeyboard;

import android.os.Bundle;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Editor for adding/editing shortcuts and their replacements.
 * 
 * @author patrick
 * 
 */
public class ReplacementActivity extends Activity implements
		EditText.OnEditorActionListener {

	/**
	 * for passing in the _id of a row to edit via an intent.
	 */
	public static final String DBROW = "de.onyxbits.remotekeyboard.dbrow";

	/**
	 * for passing in the value of the shortcut field via an intent.
	 */
	public static final String DBKEY = "de.onyxbits.remotekeyboard.dbkey";

	/**
	 * for passing in the value of the phrase field via an intent.
	 */
	public static final String DBVAL = "de.onyxbits.remotekeyboard.dbval";

	/**
	 * ID of the database row we are editing or -1 if we are suppose to create a
	 * new entry.
	 */
	private long rowid = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_replacement);
		((EditText) findViewById(R.id.input_shortcut))
				.setOnEditorActionListener(this);

		rowid = getIntent().getLongExtra(DBROW, -1);
		EditText key = (EditText) findViewById(R.id.input_shortcut);
		EditText val = (EditText) findViewById(R.id.input_phrase);
		key.setText(getIntent().getStringExtra(DBKEY));
		val.setText(getIntent().getStringExtra(DBVAL));
		key.setSelection(key.getText().length());
		val.setSelection(val.getText().length());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.replacement, menu);
		return true;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		String key = ((EditText) findViewById(R.id.input_shortcut)).getText()
				.toString();
		String val = ((EditText) findViewById(R.id.input_phrase)).getText()
				.toString();
		if (key.equals("") || val.equals("")) {
			return true;
		}

		Schema dbHelper = new Schema(this);
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(Schema.COLUMN_KEY, key);
		values.put(Schema.COLUMN_VALUE, val);
		if (rowid == -1) {
			database.insert(Schema.TABLE_REPLACEMENTS, null, values);
		}
		else {
			database.update(Schema.TABLE_REPLACEMENTS, values, Schema.COLUMN_ID + "="
					+ rowid, null);
		}
		database.close();
		if (RemoteKeyboardService.self != null) {
			RemoteKeyboardService.self.loadReplacements();
		}
		finish();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (rowid != -1) {
			Schema dbHelper = new Schema(this);
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			database.delete(Schema.TABLE_REPLACEMENTS, Schema.COLUMN_ID + " = "
					+ rowid, null);
			database.close();
			if (RemoteKeyboardService.self != null) {
				RemoteKeyboardService.self.loadReplacements();
			}
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
