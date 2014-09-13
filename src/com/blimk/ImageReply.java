package com.blimk;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.blimk.R;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.KinveyClientCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ImageReply extends Activity {
	
	private static final String TAG = ImageReply.class.getName();
	private ImageView imgView;
	private boolean replyPushSent = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_reply);
		
		Bundle extras = getIntent().getExtras();
		String question = extras.getString("question");
		
		RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.imagePreviewLayout);
		relativeLayout.setBackgroundColor(0xFF000000);
		
		if(question == null) {
			relativeLayout = (RelativeLayout)findViewById(R.id.image_caption);
			relativeLayout.setVisibility(View.INVISIBLE);
		}
		else if(question.length() > 0) {
			relativeLayout = (RelativeLayout)findViewById(R.id.image_caption);
			relativeLayout.setBackgroundColor(0x66FFFFFF);
			TextView questionView = (TextView)findViewById(R.id.question);
			questionView.setText(question);
		}
		else {
			relativeLayout = (RelativeLayout)findViewById(R.id.image_caption);
			relativeLayout.setVisibility(View.INVISIBLE);
		}
		
		ImageView thumbsUp = (ImageView)findViewById(R.id.thumbs_up);
		thumbsUp.setBackgroundColor(0x66FFFFFF);
		
		ImageView thumbsDown = (ImageView)findViewById(R.id.thumbs_down);
		thumbsDown.setBackgroundColor(0x66FFFFFF);
		
		final TextView timerText = (TextView)findViewById(R.id.timer);
		timerText.setBackgroundColor(0x66FFFFFF);
		
		imgView = (ImageView)findViewById(R.id.image_preview);
		
		setImageLocal();
		
		thumbsUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle extras = getIntent().getExtras();
				JSONObject jsonObj = new JSONObject();
				try {
					jsonObj.put("action", "com.blimk.REPLY");
		        	//jsonObj.put("alert", "This is my reply");
		        	jsonObj.put("senderLocalId", extras.getString("senderLocalId"));
		        	//Pull out the user number from the global variable
					String replyNumber = ((MyApplication) ImageReply.this.getApplication()).getPhoneNumber();
					jsonObj.put("replyNumber", replyNumber);
					jsonObj.put("reply", "y");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ParseQuery pushQuery = ParseInstallation.getQuery();
				pushQuery.whereEqualTo("phone", extras.getString("senderNumber"));
				ParsePush push = new ParsePush();
				push.setQuery(pushQuery); // Set our Installation query
				push.setData(jsonObj);
				//push.setMessage("This is a push notification that I sent");
				push.sendInBackground();
				replyPushSent=true;
				finish();
			}
		});
		
		thumbsDown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle extras = getIntent().getExtras();
				JSONObject jsonObj = new JSONObject();
				try {
					jsonObj.put("action", "com.blimk.REPLY");
		        	//jsonObj.put("alert", "This is my reply");
		        	jsonObj.put("senderLocalId", extras.getString("senderLocalId"));
		        	//Pull out the user number from the global variable
					String replyNumber = ((MyApplication) ImageReply.this.getApplication()).getPhoneNumber();
					jsonObj.put("replyNumber", replyNumber);
					jsonObj.put("reply", "n");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				ParseQuery pushQuery = ParseInstallation.getQuery();
				pushQuery.whereEqualTo("phone", extras.getString("senderNumber"));
				ParsePush push = new ParsePush();
				push.setQuery(pushQuery); // Set our Installation query
				push.setData(jsonObj);
				//push.setMessage("This is a push notification that I sent");
				push.sendInBackground();
				replyPushSent = true;
				finish();
			}
		});
		
		
		new CountDownTimer(11000, 1000) {

		     public void onTick(long millisUntilFinished) {
		         timerText.setText(" " + millisUntilFinished / 1000);
		     }

		     public void onFinish() {
		    	 if(replyPushSent) return;
		    	 Bundle extras = getIntent().getExtras();
					JSONObject jsonObj = new JSONObject();
					try {
						jsonObj.put("action", "com.blimk.REPLY");
			        	//jsonObj.put("alert", "This is my reply");
			        	jsonObj.put("senderLocalId", extras.getString("senderLocalId"));
			        	//Pull out the user number from the global variable
						String replyNumber = ((MyApplication) ImageReply.this.getApplication()).getPhoneNumber();
						jsonObj.put("replyNumber", replyNumber);
						jsonObj.put("reply", "x");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					ParseQuery pushQuery = ParseInstallation.getQuery();
					pushQuery.whereEqualTo("phone", extras.getString("senderNumber"));
					ParsePush push = new ParsePush();
					push.setQuery(pushQuery); // Set our Installation query
					push.setData(jsonObj);
					//push.setMessage("This is a push notification that I sent");
					push.sendInBackground();
					
				/*	Intent newIntent = new Intent(ImageReply.this, ImageResultListActivity.class);
					Bundle bundle = new Bundle();
					long id = Integer.parseInt(extras.getString("senderLocalId"));
					id = id-4;
					bundle.putLong("id", id);
					newIntent.putExtras(bundle);
					startActivity(newIntent);	*/
					
		    	 finish();
		     }
		  }.start();
	}
	
	private void setImageLocal() {
		Bundle extras = getIntent().getExtras();
		byte[] content = extras.getByteArray("content");
		Bitmap bitmap  = BitmapFactory.decodeByteArray (content, 0, content.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		if(rotation == 1) rotationAngle = 0;
		//if(rotation == Surface.ROTATION_180) rotationAngle = 90; 
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		imgView.setImageBitmap(bitmap);
		
	}
	
	@Override
	public void onBackPressed() {
		Bundle extras = getIntent().getExtras();
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("action", "com.blimk.REPLY");
        	//jsonObj.put("alert", "This is my reply");
        	jsonObj.put("senderLocalId", extras.getString("senderLocalId"));
        	//Pull out the user number from the global variable
			String replyNumber = ((MyApplication) ImageReply.this.getApplication()).getPhoneNumber();
			jsonObj.put("replyNumber", replyNumber);
			jsonObj.put("reply", "x");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ParseQuery pushQuery = ParseInstallation.getQuery();
		pushQuery.whereEqualTo("phone", extras.getString("senderNumber"));
		ParsePush push = new ParsePush();
		push.setQuery(pushQuery); // Set our Installation query
		push.setData(jsonObj);
		//push.setMessage("This is a push notification that I sent");
		push.sendInBackground();
		
		super.onBackPressed();
	}

	private void setImage() {
	/*	MainDataSource datasource = new MainDataSource(ImageReply.this);
		datasource.open();
		List<Media> mediaList = datasource.getAllMedia();
		Log.d("ImageReply", "*********************"+mediaList.size()+"**********ALEX************");
		datasource.close();
		Media media = mediaList.get(0);
		byte[] photo = media.getContent();
		Log.d("ImageReply", "*********************"+photo.length+"**********ALEX RIDER************");
		ImageView imgView = (ImageView)findViewById(R.id.image_preview);
		Bitmap bitmap  = BitmapFactory.decodeByteArray (photo, 0, photo.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		if(rotation == 1) rotationAngle = 0;
		//if(rotation == Surface.ROTATION_180) rotationAngle = 90; 
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		imgView.setImageBitmap(bitmap); */
		
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Media");
		//query.whereEqualTo("question", "aa");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					byte[] photo = scoreList.get(0).getBytes("image");
					String question = scoreList.get(0).getString("question");
					TextView questionView = (TextView)findViewById(R.id.question);
					questionView.setText(question);
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
				
			}
		    
		}); 
	}
	
	
	
	private class DownloadImage extends AsyncTask<Void, Void, Void> {
		byte[] mResult;

		@Override
		protected Void doInBackground(Void... arg0) {
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Media");
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> scoreList, ParseException e) {
					if(e == null) {
						byte[] photo = scoreList.get(0).getBytes("image");
						mResult = photo;
					} else {
						Log.d("score", "Error: " + e.getMessage());
						mResult = null;
					}
				}
			
			});

			return null;
		}

		protected void onPostExecute(byte[] result) {
			if(result != null) {
				byte[] photo = result;
				Bitmap bitmap  = BitmapFactory.decodeByteArray (photo, 0, photo.length);
				int width= bitmap.getWidth();
				int height= bitmap.getHeight();
				Matrix matrix = new Matrix();
				int rotationAngle = 90;
				int rotation = getWindowManager().getDefaultDisplay().getRotation();
				if(rotation == 1) rotationAngle = 0;
				//if(rotation == Surface.ROTATION_180) rotationAngle = 90; 
				matrix.setRotate(rotationAngle);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
				imgView.setImageBitmap(bitmap);
			}
			else {
				//Log.d(TAG, "**********************RESULT IS NULL***********************");
			}
			
			
		}
		
	}
	
	

}
