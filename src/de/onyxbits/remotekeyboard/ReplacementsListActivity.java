package de.onyxbits.remotekeyboard;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Provides a list of all the shortcut/phrase replacement patterns we got.
 * 
 * @author patrick
 * 
 */
public class ReplacementsListActivity extends ListActivity implements
		DialogInterface.OnClickListener {

	private static final int CONFIRMDELETE = 1;
	private static final int CONFIRMIMPORT = 2;

	private Cursor cursor;
	private int dialogType;
	private EditText urlinput;
	private static final String[] COLUMNS = { Schema.COLUMN_KEY,
			Schema.COLUMN_VALUE, Schema.COLUMN_ID };


	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.replacements_list);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SQLiteDatabase database = new Schema(this).getReadableDatabase();
		load(database);
		database.close();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		cursor.moveToPosition(position);
		Intent intent = new Intent(this, ReplacementActivity.class);
		intent.putExtra(ReplacementActivity.DBKEY, cursor.getString(0));
		intent.putExtra(ReplacementActivity.DBVAL, cursor.getString(1));
		intent.putExtra(ReplacementActivity.DBROW, cursor.getLong(2));
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.replacements_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.item_add_replacement: {
				startActivity(new Intent(this, ReplacementActivity.class));
				break;
			}
			case R.id.item_clear_list: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				dialogType = CONFIRMDELETE;
				builder.setTitle(R.string.title_confirm)
						.setMessage(R.string.msg_really_delete)
						.setPositiveButton(android.R.string.yes, this)
						.setNegativeButton(android.R.string.no, this).create().show();
				break;
			}
			case R.id.item_export: {
				doExport();
				break;
			}
			case R.id.item_import: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				dialogType = CONFIRMIMPORT;
				urlinput = new EditText(this);
				urlinput.setHint("http://");
				builder.setTitle(R.string.title_import).setView(urlinput)
						.setPositiveButton(android.R.string.yes, this)
						.setNegativeButton(android.R.string.no, this).create().show();
				break;
			}
			default: {
				return false;
			}
		}
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (dialogType) {
			case CONFIRMDELETE: {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					SQLiteDatabase database = new Schema(this).getWritableDatabase();
					database.delete(Schema.TABLE_REPLACEMENTS, null, null);
					load(database);
					if (RemoteKeyboardService.self != null) {
						RemoteKeyboardService.self.loadReplacements();
					}
					database.close();
				}
				break;
			}
			case CONFIRMIMPORT: {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					new ImportTask(this).execute(urlinput.getText().toString());
				}
				break;
			}
		}
	}

	/**
	 * Create the Listadapter and put it on display.
	 * 
	 * @param database
	 *          database handle
	 */
	protected void load(SQLiteDatabase database) {
		int[] to = { R.id.entry_key, R.id.entry_value };
		cursor = database.query(Schema.TABLE_REPLACEMENTS, COLUMNS, null, null,
				null, null, Schema.COLUMN_KEY);
		setListAdapter(new SimpleCursorAdapter(this, R.layout.entry, cursor,
				COLUMNS, to, 0));
		if (RemoteKeyboardService.self!=null) {
			RemoteKeyboardService.self.loadReplacements();
		}
	}

	/**
	 * Dump the database in JSON to the connected client.
	 */
	private void doExport() {
		if (TelnetEditorShell.self == null) {
			Toast.makeText(this, R.string.err_sendtonirvana, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		JSONObject json = new JSONObject(RemoteKeyboardService.self.replacements);
		TelnetEditorShell.self.showText(json.toString());
		Toast.makeText(this, R.string.app_dumped, Toast.LENGTH_SHORT).show();
	}

}
