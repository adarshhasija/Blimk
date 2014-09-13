package com.blimk;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MainDataSource {
	
	// Database fields
	  private SQLiteDatabase database;
	  private MySQLiteHelper dbHelper;
	  private String[] allSentColumns = { MySQLiteHelper.COLUMN_ID,
		      MySQLiteHelper.COLUMN_CONTENT, "updated_at" };
	  private String[] allQuestionColumns = { MySQLiteHelper.COLUMN_ID,
		      "senderLocalId", "question" };
	  private String[] allReceivedColumns = { MySQLiteHelper.COLUMN_ID,
	      MySQLiteHelper.COLUMN_CONTENT, "senderNumber", "senderLocalId", "updated_at" };
	  private String[] allRepliesColumns = { MySQLiteHelper.COLUMN_ID,
			  								"phoneNumber", "answer", "read_status", "senderLocalId", "updated_at" };
	  private String[] allContactsColumns = { MySQLiteHelper.COLUMN_ID,
											"phoneNumber", "updated_at" };
	  
	  public MainDataSource(Context context) {
		  dbHelper = new MySQLiteHelper(context);
	  }

	  public void open() throws SQLException {
		 database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
		 dbHelper.close();
	  }
	  
	  public void deleteDatabase(Context context) {
		  dbHelper.deleteDatabase(context);
	  }
	  
	  public void createContact(String phoneNumber) {
		  ContentValues values = new ContentValues();
		  values.put("phoneNumber", phoneNumber);
		  //if(name != null) values.put("name", name);
		  long insertId = database.insert("CONTACTS", null,
			        values);
		  if(insertId == -1) {
			  Log.e("createContacts", "*********************error: contact failed to save***************************");
		  }
	  }
	  
	  public Media createSentMedia(byte[] content, List<String> phoneNumbers) {
		    ContentValues values = new ContentValues();
		    //values.put(MySQLiteHelper.COLUMN_ID, content);
		    values.put(MySQLiteHelper.COLUMN_CONTENT, content);
		    long insertId = database.insert(MySQLiteHelper.SENT_MEDIA, null,
		        values);
		    for(String number : phoneNumbers) {
		    	createMediaContact(Integer.toString((int)insertId), number);
		    }
		    Media tmpMedia = new Media();
		    tmpMedia.setId(insertId);
		    Media newMedia = getSentMedia(tmpMedia);
		    
		    return newMedia;
	}
	  
	  public long createSentQuestion(long senderLocalId, String question) {
		    ContentValues values = new ContentValues();
		    values.put("senderLocalId", senderLocalId);
		    values.put("question", question);
		    long insertId = database.insert("SENT_QUESTIONS", null,
		        values);
		    
		    return insertId;
	}
	  
	  public long createReceivedQuestion(long senderLocalId, String question) {
		    ContentValues values = new ContentValues();
		    values.put("senderLocalId", senderLocalId);
		    values.put("question", question);
		    long insertId = database.insert("RECEIVED_QUESTIONS", null,
		        values);
		    
		    return insertId;
	}
	  
	  public Media createReceivedMedia(byte[] content, String senderNumber, String senderLocalId) {
		    ContentValues values = new ContentValues();
		    //values.put(MySQLiteHelper.COLUMN_ID, content);
		    values.put(MySQLiteHelper.COLUMN_CONTENT, content);
		    values.put("senderNumber", senderNumber);
		    values.put("senderLocalId", senderLocalId);
		    long insertId = database.insert(MySQLiteHelper.RECEIVED_MEDIA, null,
		        values);
		    Cursor cursor = database.query(MySQLiteHelper.RECEIVED_MEDIA,
		        allReceivedColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Media newMedia = null;
		    newMedia = cursorToReceivedMedia(cursor);
		    cursor.close();
		    return newMedia;
	}
	  
	  public void createMediaContact(String senderLocalId, String replyNumber) {
		  ContentValues values = new ContentValues();
		  values.put("senderLocalId", Integer.parseInt(senderLocalId));
		  values.put("phoneNumber", replyNumber);
		  long insertId = database.insert("SENT_CONTACTS", null,
			        values);
		  if(insertId == -1) {
			  Log.e("createMediaReply", "*********************error: media contact failed to save***************************");
		  }
		  else {
			  Log.d("createMediaReply", "*********************media save successful***************************"); 
		  }
	  }
	  
	  public int updateMediaContact(String senderLocalId, String replyNumber, String read_status, String answer) {
		  ContentValues values = new ContentValues();
		  values.put("read_status", read_status);
		  if(answer != null) values.put("answer", answer);
		  String whereClause = "senderLocalId =? AND phoneNumber=?";
		  String[] whereArgs = new String[] { senderLocalId, replyNumber };
		  
		  int rowsAffected = database.update("SENT_CONTACTS", values, whereClause, whereArgs);
		  //List<MediaReply> replyList = getRepliesForMedia(null);
		  if(rowsAffected == 0) {
			  Log.e("updateMediaReply", "*********************"+rowsAffected+"***************************");
		  }
		  else {
			  Log.d("updateMediaReply", "*********************UPDATED SUCCESSFULLY***************************");
		  }
		  
		  return rowsAffected;
	  }
	  
	  public void deleteMedia(Media media) {
		    long id = media.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete(MySQLiteHelper.TABLE_MEDIA, MySQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		  }
	  
	  public void deleteSentMedia(Media media) {
		    long id = media.getId();
		    deleteSentMediaQuestion(media);
		    //deleteSentMediaDefaultAnswer(media);
		    deleteSentMediaContacts(media);
		    database.delete(MySQLiteHelper.SENT_MEDIA, MySQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		    System.out.println("Sent media deleted with id: " + id);
		  }
	  
	  public void deleteSentMediaQuestion(Media media) {
		    long id = media.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete("SENT_QUESTIONS", MySQLiteHelper.COLUMN_FOREIGN_KEY
		        + " = " + id, null);
		    System.out.println("Sent media question deleted with id: " + id);
		  }
	  
	  public void deleteSentMediaDefaultAnswer(Media media) {
		    long id = media.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete("SENT_DEFAULT_ANSWER", MySQLiteHelper.COLUMN_FOREIGN_KEY
		        + " = " + id, null);
		    System.out.println("Sent media default answer deleted with id: " + id);
		  }
	  
	  public void deleteSentMediaContacts(Media media) {
		    long id = media.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete("SENT_CONTACTS", MySQLiteHelper.COLUMN_FOREIGN_KEY
		        + " = " + id, null);
		    System.out.println("Sent media contacts deleted with id: " + id);
		  }
	  
	  public void deleteReceivedMedia(Media media) {
		    long id = media.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete(MySQLiteHelper.RECEIVED_MEDIA, MySQLiteHelper.COLUMN_ID
		        + " = " + id, null);
		  }
	  
	  public Media getSentMedia(Media media) {
		  Cursor cursor = database.query(MySQLiteHelper.SENT_MEDIA,
			        allSentColumns, MySQLiteHelper.COLUMN_ID + " = " + media.getId(), null,
			        null, null, null);
			    if(!cursor.moveToFirst()) {
			    	return new Media();
			    }
			    Media newMedia = cursorToSentMedia(cursor);
			    newMedia.setQuestion(getQuestionForSentMedia(newMedia));
			    newMedia.setReplies(getRepliesForMedia(newMedia));
			    cursor.close();
			    return newMedia;
	  }
	  
	  public Media getReceivedMedia(Media media) {
		  Cursor cursor = database.query(MySQLiteHelper.RECEIVED_MEDIA,
			        allReceivedColumns, MySQLiteHelper.COLUMN_ID + " = " + media.getId(), null,
			        null, null, null);
			    cursor.moveToFirst();
			    Media newMedia = cursorToReceivedMedia(cursor);
			    newMedia.setQuestion(getQuestionForReceivedMedia(newMedia));
			    cursor.close();
			    return newMedia;
	  }
	  
	  public List<Media> getAllMedia() {
		    List<Media> mediaList = new ArrayList<Media>();

		    Cursor cursor = database.query(MySQLiteHelper.TABLE_MEDIA,
		        allSentColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      Media media = cursorToSentMedia(cursor);
		      
		      mediaList.add(media);
		      cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
		  }
	  
	  public String getQuestionForSentMedia(Media media) {
		  Cursor cursor = database.query("SENT_QUESTIONS",
			        allQuestionColumns, "senderLocalId="+media.getId(), null, null, null, null);
		  
		  if(cursor.moveToFirst()) {
			  String question = cursor.getString(2);
			  
			  return question;
		  }
		  cursor.close();
		  
		  return null;
	  }
	  
	  public String getQuestionForReceivedMedia(Media media) {
		  String result=null;
		  
		  Cursor cursor = database.query("RECEIVED_QUESTIONS",
			        allQuestionColumns, "senderLocalId="+media.getId(), null, null, null, null);
		  
		  if(cursor.moveToFirst()) {
			  String question = cursor.getString(2);
			  result =  question;
		  }
		  cursor.close();
		  
		  return result;
	  }
	  
	  public List<String> getAllContacts() {
		    //Map<String,String> contacts = new HashMap<String,String>();
		  List<String> contacts = new ArrayList<String>();

		    Cursor cursor = database.query("CONTACTS",
		        allContactsColumns, null, null, null, null, null);

		    if(cursor.moveToFirst()) {
		    	while (!cursor.isAfterLast()) {
		    		contacts.add(cursor.getString(1));
		    		//contacts.put(cursor.getString(1), cursor.getString(2));
		      		if(!cursor.moveToNext()) break;
		    	}
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return contacts;
		  }
	  
	  public List<Media> getAllSentMedia() {
		  List<Media> mediaList = new ArrayList<Media>();

		    Cursor cursor = database.query(MySQLiteHelper.SENT_MEDIA,
		        allSentColumns, null, null, null, null, null);

		    if(cursor.moveToFirst()) {
			    while (!cursor.isAfterLast()) {
			      Media media = cursorToSentMedia(cursor);
			      media.setQuestion(getQuestionForSentMedia(media));
			      media.setReplies(getRepliesForMedia(media));
			      mediaList.add(media);
			      cursor.moveToNext();
			    }
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
	  }
	  
	  public List<Media> getAllSentMedia(int limit) {
		  List<Media> mediaList = new ArrayList<Media>();
		  
		  String limitCondition = "updated_at DESC LIMIT "+limit;

		    Cursor cursor = database.query(MySQLiteHelper.SENT_MEDIA,
		        allSentColumns, null, null, null, null, limitCondition);

		    if(cursor.moveToFirst()) {
			    while (!cursor.isAfterLast()) {
			      Media media = cursorToSentMedia(cursor);
			      media.setQuestion(getQuestionForSentMedia(media));
			      media.setReplies(getRepliesForMedia(media));
			      mediaList.add(media);
			      cursor.moveToNext();
			    }
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
	  }
	  
	  public List<Media> getAllSentMedia(Media inputMedia, int limit) {
		  List<Media> mediaList = new ArrayList<Media>();
		  
		  String limitCondition = "updated_at DESC LIMIT "+limit;

		    Cursor cursor = database.query(MySQLiteHelper.SENT_MEDIA,
		        allSentColumns, "updated_at<'"+inputMedia.getUpdated_at()+"'", null, null, null, limitCondition);

		    if(cursor.moveToFirst()) {
			    while (!cursor.isAfterLast()) {
			      Media media = cursorToSentMedia(cursor);
			      media.setQuestion(getQuestionForSentMedia(media));
			      media.setReplies(getRepliesForMedia(media));
			      mediaList.add(media);
			      cursor.moveToNext();
			    }
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
	  }
	  
	  public List<MediaReply> getRepliesForMedia(Media media) {
		  String where = null;
		  if(media != null) { where = "senderLocalId="+media.getId(); }
		  List<MediaReply> repliesList = new ArrayList<MediaReply>();
		  Cursor cursor = database.query("SENT_CONTACTS",
			        allRepliesColumns, where, null, null, null, null);
		  if(cursor.moveToFirst()) {
			  while(!cursor.isAfterLast()) {
				  MediaReply mediaReply = cursorToMediaReply(cursor);
				  repliesList.add(mediaReply);
				  cursor.moveToNext();
			  }
		  }
		  cursor.close();
		  return repliesList;
	  }
	  
	  public List<Media> getAllReceivedMedia() {
		  List<Media> mediaList = new ArrayList<Media>();
		  
		  String limitCondition = "updated_at DESC";

		    Cursor cursor = database.query(MySQLiteHelper.RECEIVED_MEDIA,
		        allReceivedColumns, null, null, null, null, limitCondition);

		    if(cursor.moveToFirst()) {
			    while (!cursor.isAfterLast()) {
			      Media media = cursorToReceivedMedia(cursor);
			      media.setQuestion(getQuestionForReceivedMedia(media));
			      mediaList.add(media);
			      cursor.moveToNext();
			    }
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
	  }
	  
	  public List<Media> getAllReceivedMedia(Media inputMedia, int limit) {
		  List<Media> mediaList = new ArrayList<Media>();
		  
		  String limitCondition = "updated_at DESC LIMIT "+limit;

		    Cursor cursor = database.query(MySQLiteHelper.RECEIVED_MEDIA,
		        allReceivedColumns, "updated_at<'"+inputMedia.getUpdated_at()+"'", null, null, null, limitCondition);

		    if(cursor.moveToFirst()) {
			    while (!cursor.isAfterLast()) {
			      Media media = cursorToReceivedMedia(cursor);
			      media.setQuestion(getQuestionForReceivedMedia(media));
			      mediaList.add(media);
			      cursor.moveToNext();
			    }
		    }
		    // make sure to close the cursor
		    cursor.close();
		    return mediaList;
	  }
	  
	  
	  
	  private Media cursorToSentMedia(Cursor cursor) {
		    Media media = new Media();
		    //Log.d("cursorToSentMedia", "***************"+cursor.moveToFirst()+"*******************");
		    media.setId(cursor.getLong(0));
		    media.setContent(cursor.getBlob(1));
		    media.setUpdated_at(Timestamp.valueOf(cursor.getString(2)));
		    return media;
		  }
	  
	  private Media cursorToReceivedMedia(Cursor cursor) {
		    Media media = new Media();
		    media.setId(cursor.getLong(0));
		    media.setSenderNumber(cursor.getString(2));
		    media.setSenderLocalId(cursor.getString(3));
		    media.setContent(cursor.getBlob(1));
		    media.setUpdated_at(Timestamp.valueOf(cursor.getString(4)));
		    return media;
		  }
	  
	  private MediaReply cursorToMediaReply(Cursor cursor) {
		  MediaReply mediaReply = new MediaReply();
		  mediaReply.setId(cursor.getLong(0));
		  mediaReply.setPhoneNumber(cursor.getString(1));
		  mediaReply.setAnswer(cursor.getString(2));
		  mediaReply.setReadStatus(cursor.getString(3));
		  mediaReply.setSenderLocalId(cursor.getLong(4));
		  mediaReply.setUpdated_at(Timestamp.valueOf(cursor.getString(5)));
		  
		  return mediaReply;
	  }

}
