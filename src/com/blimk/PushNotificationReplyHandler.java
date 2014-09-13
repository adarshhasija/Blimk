package com.blimk;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

public class PushNotificationReplyHandler extends BroadcastReceiver {
	private Context context=null;
	public static long active_id = -1;

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		//This part is to notify the broadcast received of which result list view is active
		if(extras.getBoolean("replyViewActive") == true) {
			if(active_id == -1) {
				active_id = extras.getLong("id");
			}
			else {
				active_id = -1;
			}
			return;
		}
		
		try {
			this.context = context;
			JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			String replyNumber = json.getString("replyNumber");
			String answer = json.getString("reply");
			String senderLocalId = json.getString("senderLocalId");
			MainDataSource datasource = new MainDataSource(context);
		    datasource.open();
		    int rowsAffected = datasource.updateMediaContact(senderLocalId, replyNumber, "unread", answer);
		    Media media = new Media();
		    media.setId(Integer.parseInt(senderLocalId));
		    List<MediaReply> replyList = null;
		    if(rowsAffected > 0) {
		    	replyList = datasource.getRepliesForMedia(media); //If this media actually exists, get replies list
		    }
		    datasource.close();
		    if(rowsAffected == 0) {
		    	return; //If no rows were affected it means the media doesnt exist anymore. No need to create a notification
		    }
		    generateNotification(senderLocalId, replyNumber, answer);
		    for(MediaReply mr : replyList) {
		    	if(mr.getAnswer() == null) {
		    		return;
		    	}
		    }
		    //If all answers have been received, delete object from server
		    String myNumber = ((MyApplication) context.getApplicationContext()).getPhoneNumber();
		    deleteOnServer(myNumber,senderLocalId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void generateNotification(String senderLocalId, String replyNumber, String answer) {
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List < ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		if(componentInfo.getClassName().equalsIgnoreCase("com.blimk.InboxListActivity")) {
			Bundle bundle = new Bundle();
			bundle.putString("type", "reply");
			bundle.putLong("id", Integer.parseInt(senderLocalId));
			Intent i = new Intent();
	        i.setAction("com.blimk.InboxBroadcast");
	        i.putExtras(bundle);
	        context.sendBroadcast(i);
		}
		//If the current active id is the same as the incoming notification, send it
		else if(active_id == Integer.parseInt(senderLocalId)) {
			Bundle bundle = new Bundle();
			bundle.putLong("id", Integer.parseInt(senderLocalId));
			bundle.putString("replyNumber", replyNumber);
			bundle.putString("answer", answer);
			bundle.putString("read_status", "unread");
			Intent i = new Intent();
	        i.setAction("com.blimk.ImageResultListActivity_"+senderLocalId);
	        i.putExtras(bundle);
	        context.sendBroadcast(i);
	        //PackageManager packageManager = context.getPackageManager();
	        
		}	
		else {
			String senderNumber;
			String tickerText=null;
			String contentText = null;
			String received="";
			String replies="";
			String name = getContactName(replyNumber);
			if(name == null) {
				tickerText = "New reply from "+replyNumber;
			}
			else {
				tickerText = "New reply from "+name;
			}
			MainDataSource datasource = new MainDataSource(context);
			datasource.open();
			List<Media> receivedList = datasource.getAllReceivedMedia();
			List<Media> sentList = datasource.getAllSentMedia();
			datasource.close();
			int sentUnreadCount=0;
			for(Media m : sentList) {
				List<MediaReply> replyList = m.getReplies();
				for(MediaReply mr : replyList) {
					if(mr.getAnswer() != null) {
						if(mr.getReadStatus().equals("unread")) {
							sentUnreadCount++;
							break;
						}
					}
				}
			}
			if(receivedList.size() > 1) {
				received = receivedList.size() + " new blinks ";
			}
			else if(receivedList.size() == 1) {
				received = "1 new blink ";
			}
			if(sentUnreadCount > 1) {
				replies = "replies to "+sentUnreadCount+" blimks";
			}
			else if(sentUnreadCount == 1) {
				replies = "replies to 1 blimk";
			}
			//throw in an and
			if(received.length() > 0 && replies.length() > 0) {
				contentText = received + " and " + replies;
			}
			else {
				contentText = received + replies;
			}
			
			Intent intent = new Intent(context, InboxListActivity.class);
	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
	        stackBuilder.addParentStack(InboxListActivity.class);
	        stackBuilder.addNextIntent(intent);
	        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
	        PendingIntent contentIntent =
	                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	
	        NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
	        //Bitmap icon = getLargeIcon();
	        
	        NotificationCompat.Builder mBuilder =
	        new NotificationCompat.Builder(context)
	        .setSmallIcon(R.drawable.eye_icon)
	        //.setLargeIcon(icon)
	        .setContentTitle("blimk")
	        .setAutoCancel(true)
	        .setContentText(contentText)
	        .setTicker(tickerText);
	
	        mBuilder.setContentIntent(contentIntent);
	        int defaults = Notification.DEFAULT_SOUND;
	        //defaults |= Notification.FLAG_AUTO_CANCEL;
	        defaults |= Notification.DEFAULT_VIBRATE;
	        defaults |= Notification.FLAG_SHOW_LIGHTS;
	        defaults |= Notification.DEFAULT_LIGHTS;
	        mBuilder.setDefaults(defaults);
	        //mBuilder.setLights(0xff7d00ff, 1000, 5000); //purple
	
	        mNotifM.notify(1, mBuilder.build());
		}
	}
	
	private String getContactName(String receivedNumber) {
		String result = null;
		Cursor data = context.getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, null, null,  Phone.DISPLAY_NAME + " ASC");
		
		int i=0;
		data.moveToFirst();
		while (data.isAfterLast() == false) 
		{
			//String number = data.getString(data.getColumnIndex(Phone.NUMBER));
			int numberIndex = data.getColumnIndex(Phone.NUMBER);
			int nameIndex = data.getColumnIndex(Phone.DISPLAY_NAME);
			String number = data.getString(numberIndex).replaceAll("\\s+","");
			String name = data.getString(nameIndex);
		    if(number.equals(receivedNumber)) { 
		    	//result = name.split("\\s+")[0]; //return only the first name
		    	result = name;
		    }
		    i++;
		    data.moveToNext();
		}
		data.close();
		
		if(result == null) {
			//result = "Not found";
		}
		return result;
	}
	
	private void deleteOnServer(final String senderNumber, final String senderLocalId) {
		ParseQuery<ParseObject> query=null;
		query = ParseQuery.getQuery("Media");
		query.whereEqualTo("senderNumber", senderNumber);
		query.whereEqualTo("senderLocalId", senderLocalId);
		
		final FindCallback<ParseObject> parseCallback = new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					if(scoreList.size() > 0) {
						ParseObject object = scoreList.get(0);
						object.deleteInBackground();
					}
					else {
						Handler handler = new Handler();
						handler.postDelayed(new Runnable(){
						@Override
						      public void run(){
								deleteOnServer(senderNumber, senderLocalId);
						   	  }
						}, 1000);   //repeat call after 1 second
						
					}
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
				
			}
		    
		};

		query.findInBackground(parseCallback);
	}
	
	private Bitmap getLargeIcon() {
		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.eye_icon);
		
		int width= R.dimen.notification_large_icon_width; //notification_large_icon_width
		int height= R.dimen.notification_large_icon_height; //notification_large_icon_height
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		matrix.setRotate(rotationAngle);
		icon = Bitmap.createBitmap(icon, 0, 0, width, height, matrix, true);
		return icon;
	}

	
}
