package com.blimk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class ImageResultListActivity extends ListActivity {

	private ImageResultAdapter adapter;
	private List<MediaReply> replied = new ArrayList<MediaReply>();
	private List<MediaReply> notReplied = new ArrayList<MediaReply>();
	private List<MediaReply> timedOut = new ArrayList<MediaReply>();
	private List<MediaReply> allReplies;
	private Comparator<MediaReply> repliesComparator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.listview);
		
		Bundle extras = getIntent().getExtras();
		
		MainDataSource datasource = new MainDataSource(ImageResultListActivity.this);
		datasource.open();
		Media inputMedia = new Media();
		inputMedia.setId(extras.getLong("id"));
		inputMedia = datasource.getSentMedia(inputMedia);
		allReplies = datasource.getRepliesForMedia(inputMedia);
		datasource.close();
		
		for(MediaReply r : allReplies) {
			if(r.getAnswer() != null) {
				if(r.getAnswer().equals("x")) {
					timedOut.add(r);
				}
				else {
					replied.add(r);
				}
			}
			else notReplied.add(r);
		}
		
		repliesComparator = new Comparator<MediaReply>() {
			@Override
			public int compare(MediaReply o1, MediaReply o2) {
				//return o1.getUpdated_at().compareTo(o2.getUpdated_at());
				  return o2.getUpdated_at().compareTo(o1.getUpdated_at()); //descending
			}
		};
		
		Collections.sort(replied, repliesComparator);
		Collections.sort(notReplied, repliesComparator);
		Collections.sort(timedOut, repliesComparator);
		Collections.sort(allReplies, repliesComparator);
		
		adapter = new ImageResultAdapter(this, 0, allReplies, replied, notReplied, timedOut);
	    setListAdapter(adapter);
	    
	    IntentFilter filter = new IntentFilter("com.blimk.ImageResultListActivity_"+extras.getLong("id"));
	    registerReceiver(resetReceiver, filter);
	    
	    actionBarSetup(inputMedia);
	    
	    Bundle bundle = new Bundle();
	    bundle.putBoolean("replyViewActive", true);
	    bundle.putLong("id", extras.getLong("id"));
	    Intent i = new Intent();
        i.setAction("com.blimk.REPLY");
        i.putExtras(bundle);
        this.sendBroadcast(i);
	}
	
	
	@Override
	protected void onDestroy() {
		Bundle extras = getIntent().getExtras();
		
		Bundle bundle = new Bundle();
	    bundle.putBoolean("replyViewActive", true);
	    bundle.putLong("id", extras.getLong("id"));
	    Intent i = new Intent();
        i.setAction("com.blimk.REPLY");
        i.putExtras(bundle);
        this.sendBroadcast(i);
        
		unregisterReceiver(resetReceiver);
		super.onDestroy();
	}
	
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void actionBarSetup(Media media) {
	    ActionBar ab = getActionBar();
	    if(media.getQuestion() == null) {
	    	ab.setTitle("blimk");
	    }
	    else if(media.getQuestion().length() < 1) {
	    	ab.setTitle("blimk");
	    }
	    else {
	    	ab.setTitle(media.getQuestion());
	    }
	    byte[] content = media.getContent();
	    Bitmap bitmap  = BitmapFactory.decodeByteArray (content, 0, content.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
	    BitmapDrawable bitmapDrawable =  new BitmapDrawable(bitmap);//new BitmapDrawable(BitmapFactory.decodeByteArray(content, 0, content.length));
	    ab.setIcon(bitmapDrawable);
	    //ab.setSubtitle("Tap here to view sent image"); 
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.image_result_list, menu);
		
		MenuItem viewImageButton = (MenuItem)menu.findItem(R.id.view_image);
		viewImageButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				Bundle extras = getIntent().getExtras();
				Intent newIntent = new Intent(ImageResultListActivity.this,ImageResult.class);
				Bundle bundle = new Bundle();
				bundle.putLong("id", extras.getLong("id"));
				bundle.putString("question", extras.getString("question"));
	        	newIntent.putExtras(bundle);
	        	startActivity(newIntent);
				return false;
			}
		});
		
		MenuItem deleteMediaButton = (MenuItem)menu.findItem(R.id.delete_media);
		deleteMediaButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem arg0) {
				Bundle extras = getIntent().getExtras();
				
				MainDataSource datasource = new MainDataSource(ImageResultListActivity.this);
				datasource.open();
				Media media = new Media();
				media.setId(extras.getLong("id"));
				datasource.deleteSentMedia(media);
				datasource.close();
				
				ParseQuery<ParseObject> query=null;
				query = ParseQuery.getQuery("Media");
				query.whereEqualTo("senderNumber", ((MyApplication) ImageResultListActivity.this.getApplication()).getPhoneNumber());
				query.whereEqualTo("senderLocalId", extras.getLong("id"));
				
				final FindCallback<ParseObject> parseCallback = new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> scoreList, ParseException e) {
						if (e == null) {
							if(scoreList.size() > 0) {
								ParseObject object = scoreList.get(0);
								object.deleteInBackground();
							}
							else {
								
							}
				        } else {
				            Log.d("score", "Error: " + e.getMessage());
				        }
						
					}
				    
				};

				query.findInBackground(parseCallback);
				Toast.makeText(ImageResultListActivity.this, "Media deleted", Toast.LENGTH_SHORT).show();
				finish();
				return false;
			}
			
		});
		
		return super.onCreateOptionsMenu(menu);
	}



	public BroadcastReceiver resetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			MediaReply mediaReply = new MediaReply();
			mediaReply.setAnswer(extras.getString("answer"));
			mediaReply.setPhoneNumber(extras.getString("replyNumber"));
			mediaReply.setReadStatus(extras.getString("read_status"));
			
			if(mediaReply.getAnswer().equals("x")) {
				int position=0;
				if(timedOut.size() > 0) {
					position = adapter.getPosition(timedOut.get(0));  //get the current adapter position of the most recent element
					MediaReply tmpReply = timedOut.get(0);   //get the most recent element because we have to remove it and reinsert it
					adapter.remove(timedOut.get(0));
					adapter.insert(tmpReply, position);
				}
				else {
					position = adapter.getPosition(notReplied.get(0));  //if no timedout entries exist, get the most recent not replied
				}
				adapter.insert(mediaReply, position);
				timedOut.add(0, mediaReply);
			}
			else {
				int position=0;
				if(replied.size() > 0) {
					position = adapter.getPosition(replied.get(0));  //get the current adapter position of the most recent element
					MediaReply tmpReply = replied.get(0);   //get the most recent element because we have to remove it and reinsert it
					adapter.remove(replied.get(0));
					adapter.insert(tmpReply, position);
				}
				else if(timedOut.size() > 0) {
					position = adapter.getPosition(timedOut.get(0));
				}
				else {
					position = adapter.getPosition(notReplied.get(0));
				}
				adapter.insert(mediaReply, position);
				replied.add(0, mediaReply);
			}
			for(MediaReply mr : notReplied) {
				if(mediaReply.getPhoneNumber().equals(mr.getPhoneNumber())) {
					notReplied.remove(mr);
					int position = adapter.getPosition(mr);
					int adapterSize = adapter.getCount();
					
					//If this is not the last element in the list, remove and reinsert the next one down
					MediaReply mediaOneDown=null;
					if(position < adapterSize-1) {
						mediaOneDown = adapter.getItem(position+1);
					}
					adapter.remove(mr);
					if(mediaOneDown != null) {
						adapter.remove(mediaOneDown);
						adapter.insert(mediaOneDown, position);
					}
					break;
				}
			}
		
			adapter.notifyDataSetChanged();
		}
	};

}
