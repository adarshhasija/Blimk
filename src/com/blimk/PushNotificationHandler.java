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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class PushNotificationHandler extends BroadcastReceiver {
	private Context context=null;
	private JSONObject json=null;

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
			this.context = context;
			this.json = json;
			queryParse();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setImage(ParseObject media) {
		byte[] photo = media.getBytes("content");
		String senderNumber = media.getString("senderNumber");
		String senderLocalId = media.getString("senderLocalId");
		//String defaultAnswer = media.getString("defaultAnswer");
		String question = media.getString("question");
		MainDataSource datasource = new MainDataSource(context);
		datasource.open();
		Media resultMedia = datasource.createReceivedMedia(photo, senderNumber, senderLocalId);
		datasource.createReceivedQuestion(resultMedia.getId(), question);
		datasource.close();
		
		generateNotification(resultMedia);
	}
	
	private void queryParse() {
		ParseQuery<ParseObject> query=null;
		try {
			String senderNumber = json.getString("senderNumber");
			String senderLocalId = json.getString("senderLocalId");
			
			query = ParseQuery.getQuery("Media");
			query.whereEqualTo("senderNumber", senderNumber);
			query.whereEqualTo("senderLocalId", senderLocalId);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		final FindCallback<ParseObject> parseCallback = new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					if(scoreList.size() > 0) {
						ParseObject object = scoreList.get(0);
						setImage(object);
					}
				/*	else {
						Handler handler = new Handler();
						handler.postDelayed(new Runnable(){
						@Override
						      public void run(){
								queryParse();
						   	  }
						}, 1000);   //repeat call after 1 second
						
					}	*/
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
				
			}
		    
		};

		query.findInBackground(parseCallback);
	}
	
	
	private void generateNotification(Media media) {
		ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
		List < ActivityManager.RunningTaskInfo > taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		if(componentInfo.getClassName().equalsIgnoreCase("com.blimk.InboxListActivity")) {
			Bundle bundle = new Bundle();
			bundle.putString("type", "received");
			bundle.putLong("id", media.getId());
			Intent i = new Intent();
	        i.setAction("com.blimk.InboxBroadcast");
	        i.putExtras(bundle);
	        context.sendBroadcast(i);
		}
		else {
			String senderNumber;
			String tickerText=null;
			String contentText = null;
			String received="";
			String replies="";
			try {
				senderNumber = json.getString("senderNumber");
				String name = getContactName(senderNumber);
				if(name == null) {
					tickerText = "New blimk from "+senderNumber;
				}
				else {
					tickerText = "New blink from "+name;	
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
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		        Intent intent = new Intent(context, InboxListActivity.class);
		        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		        //stackBuilder.addParentStack(InboxActivity.class);
		        stackBuilder.addNextIntent(intent);
		        //PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
		        PendingIntent contentIntent =
		                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		
		        NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		        //Bitmap icon = getLargeIcon();
		        
		        NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.eye_icon)
		        .setContentTitle("blimk")
		        //.setLargeIcon(icon)
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
		
		        mNotifM.notify(0, mBuilder.build());
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
