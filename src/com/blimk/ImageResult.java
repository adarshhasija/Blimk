package com.blimk;

import java.util.List;
import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ImageResult extends Activity {
	
	private long id;
	private ImageView imgView;
	private TextView analysisView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_result);
		RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.imageResultLayout);
		relativeLayout.setBackgroundColor(0xFF000000);
		
		//relativeLayout = (RelativeLayout)findViewById(R.id.image_caption);
		//relativeLayout.setBackgroundColor(0x66FFFFFF);
		
		relativeLayout = (RelativeLayout)findViewById(R.id.image_replies_count);
		relativeLayout.setVisibility(View.INVISIBLE);
		//relativeLayout.setBackgroundColor(0x66FFFFFF);

		imgView = (ImageView)findViewById(R.id.image_preview);
		
		Bundle extras = getIntent().getExtras();
		id = extras.getLong("id");
		Media media = new Media();
		media.setId(id);
		MainDataSource datasource = new MainDataSource(this);
		datasource.open();
		media = datasource.getSentMedia(media);
		datasource.close();
		
		String question = extras.getString("question");
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

		analysisView = (TextView)findViewById(R.id.replies_count);
		//evaluateAndDisplayResults(media);
		setImageLocal(media);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Media media = new Media();
		media.setId(id);
		evaluateAndDisplayResults(media);
	}
	
	private void evaluateAndDisplayResults(Media media) {
	/*	MainDataSource datasource = new MainDataSource(this);
		datasource.open();
		List<MediaReply> replyList = datasource.getRepliesForMedia(media);
		datasource.close(); */
		List<MediaReply> replyList = media.getReplies();
		
		Bundle bundle = new Bundle();
		int thumbsUp=0;
		int thumbsDown=0;
		int timedOut=0;
		int noResponse=0;
		int unread=0;
		for(MediaReply m : replyList) {
			if(m.getAnswer() == null) {}
			else if(m.getAnswer().equals("y")) {
				thumbsUp++;
				if(m.getReadStatus().equals("unread")) {
					unread++;
				}
			}
			else if(m.getAnswer().equals("n")){
				thumbsDown++;
				if(m.getReadStatus().equals("unread")) {
					unread++;
				}
			}
			else if(m.getAnswer().equals("x")) {
				timedOut++;
				if(m.getReadStatus().equals("unread")) {
					unread++;
				}
			}
			else {
				noResponse++;
			}
		}
		bundle.putInt("thumbsUp", thumbsUp);
		bundle.putInt("thumbsDown", thumbsDown);
		bundle.putInt("timedOut", timedOut);
		bundle.putInt("noResponse", noResponse);
		bundle.putInt("unread", unread);
		
		analysisView.setText(getAnalysis(bundle));
	}
	
	private String getAnalysis(Bundle extras) {
		int thumbsUp = extras.getInt("thumbsUp");
		int thumbsDown = extras.getInt("thumbsDown");
		int timedOut = extras.getInt("timedOut");
		int noResponse = extras.getInt("noResponse");
		int unread = extras.getInt("unread");
		int totalReceived = thumbsUp + thumbsDown + timedOut;
		int total = thumbsUp + thumbsDown + timedOut + noResponse;
		
		String analysis=null;
		if(unread > 0) {
			if(unread == 1) {
				analysis = "1 new reply";
			}
			else {
				analysis = unread + " new replies";
			}
		}
		else if(totalReceived == total) {
			analysis = "All replies received";
		}
		else if(totalReceived == 0) {
			analysis = "No replies received";
		}
		else {
			analysis = totalReceived + " out of "+total+" replies received";
		}
		
		return analysis;
	}


	private void setImageLocal(Media media) {
			byte[] content = media.getContent();
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
	
	
	private void setImage() {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Media");
		//query.whereEqualTo("question", "aa");
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					byte[] photo = scoreList.get(0).getBytes("image");
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
					imgView.setImageBitmap(bitmap); 
					
					String question = scoreList.get(0).getString("question");
					TextView questionView = (TextView)findViewById(R.id.question);
					questionView.setText(question);
		            Log.d("score", "Retrieved " + scoreList.get(0).getString("question") + " scores");
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
				
			}
		    
		});
	}
	
	
	@Override
	protected void onDestroy() {
		//unregisterReceiver(resetReceiver);
		super.onDestroy();
	}


	public BroadcastReceiver resetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			Media media = new Media();
			media.setId(id);
			evaluateAndDisplayResults(media);
		}
	};

	
}
