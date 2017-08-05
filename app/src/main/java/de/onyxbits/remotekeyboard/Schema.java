package de.onyxbits.remotekeyboard;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database definitions
 * @author patrick
 *
 */
public class Schema extends SQLiteOpenHelper {

	
	public static final String TABLE_REPLACEMENTS = "replacements";
	public static final String COLUMN_ID = "_id";
	private static final String DATABASE_NAME = "keyboard.db";
	private static final int DATABASE_VERSION = 1;
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_VALUE = "value";

  private static final String DATABASE_CREATE = "create table "
      + TABLE_REPLACEMENTS + "(" 
  		+ COLUMN_ID +" integer primary key autoincrement, " 
      + COLUMN_KEY + " text not null," 
      + COLUMN_VALUE + " text not null);";
	

  public Schema(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
		//db.execSQL("INSERT into filter (key,value) VALUES ('hello key','hello value')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    Log.w(Schema.class.getName(),
        "Upgrading database from version " + oldVersion + " to "
            + newVersion + ", which will destroy all old data");
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPLACEMENTS);
    onCreate(db);
	}

}
