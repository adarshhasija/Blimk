package com.blimk;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
	
	  private static final String DATABASE_NAME = "blimk.db";
	  private static final int DATABASE_VERSION = 2;
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_FOREIGN_KEY = "senderLocalId";

	  public static final String TABLE_MEDIA = "MEDIA";
	  public static final String SENT_MEDIA = "SENT_MEDIA";
	  public static final String RECEIVED_MEDIA = "RECEIVED_MEDIA";
	  public static final String COLUMN_CONTENT = "content";

	  // Database creation sql statement
	  private static final String MEDIA_DATABASE_CREATE = "create table "
	      + TABLE_MEDIA + "(" + COLUMN_ID
	      //+ " integer primary key autoincrement, " + COLUMN_CONTENT
	      + " string primary key, " + COLUMN_CONTENT
	      + " blob not null);";
	  
	  private static final String SENT_DATABASE_CREATE = "create table "
		      + SENT_MEDIA + "(" + COLUMN_ID
		      + " integer primary key autoincrement, " + COLUMN_CONTENT
		      //+ " string primary key, " + COLUMN_CONTENT
		      + " blob not null, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
	  
	  private static final String SENT_QUESTION_DATABASE_CREATE = "create table "
		      + "SENT_QUESTIONS" + "(" + COLUMN_ID
		      + " integer primary key autoincrement, senderLocalId integer not null, question text,"
		      + " FOREIGN KEY(senderLocalId) REFERENCES SENT_MEDIA(_id))";
	  
	  private static final String SENT_DEFAULT_ANSWER_DATABASE_CREATE = "create table "
		      + "SENT_DEFAULT_ANSWER" + "(" + COLUMN_ID
		      + " integer primary key autoincrement, senderLocalId integer not null, defaultAnswer text,"
		      + " FOREIGN KEY(senderLocalId) REFERENCES SENT_MEDIA(_id))";
	  
	  private static final String REPLIES_CREATE = "create table "
		      + "SENT_CONTACTS" + "(" + COLUMN_ID
		      + " integer primary key autoincrement, senderLocalId integer not null, phoneNumber text not null, answer text, read_status string, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
		      + "FOREIGN KEY(senderLocalId) REFERENCES SENT_MEDIA(_id));";
	  
	  private static final String RECEIVED_DATABASE_CREATE = "create table "
		      + RECEIVED_MEDIA + "(" + COLUMN_ID
		      //+ " integer primary key autoincrement, " + COLUMN_CONTENT
		      + " integer primary key autoincrement, senderNumber text not null, senderLocalId text not null, " + COLUMN_CONTENT 
		      + " blob not null, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
	  
	  private static final String RECEIVED_QUESTION_DATABASE_CREATE = "create table "
		      + "RECEIVED_QUESTIONS" + "(" + COLUMN_ID
		      //+ " integer primary key autoincrement, " + COLUMN_CONTENT
		      + " integer primary key autoincrement, senderLocalId text not null, question text not null);";
	  
	  private static final String CONTACTS_CREATE = "create table "
		      + "CONTACTS" + "(" + COLUMN_ID
		      + " integer primary key autoincrement, phoneNumber text not null, updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
	
    public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		//sqliteDB =  context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null );
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL(MEDIA_DATABASE_CREATE);
		db.execSQL("PRAGMA foreign_keys = ON");
		db.execSQL(SENT_DATABASE_CREATE);
		db.execSQL(SENT_QUESTION_DATABASE_CREATE);
		db.execSQL(REPLIES_CREATE);
		db.execSQL(RECEIVED_DATABASE_CREATE);
		db.execSQL(RECEIVED_QUESTION_DATABASE_CREATE);
		db.execSQL(CONTACTS_CREATE);
	}
	
	public void deleteDatabase(Context context) {
		context.deleteDatabase(DATABASE_NAME);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//Log.d("database", "**********on upgrade**************");
		Log.w(MySQLiteHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", which will destroy all old data");
		    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDIA);
		    onCreate(db);
	}
	
	public void addEntry( String name, byte[] image) throws SQLiteException{
	    ContentValues cv = new  ContentValues();
	    cv.put("name", name);
	    //cv.put(KEY_NAME,    name);
	    //cv.put(KEY_IMAGE,   image);
	    //sqliteDB.insert( DICTIONARY_TABLE_NAME, null, cv );
	}

}
