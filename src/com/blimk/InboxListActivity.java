package com.blimk;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InboxListActivity extends ListActivity {
	
	List<Media> sentMediaList = null;
	List<Media> receivedMediaList = null;
	List<Media> mainMediaList = null;
	List<Media> mediaList = null;
	private InboxAdapter inboxAdapter;
	MainDataSource datasource;
	Button btnLoadMore;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		datasource = new MainDataSource(this);
	    datasource.open();
	    receivedMediaList = datasource.getAllReceivedMedia();
	    sentMediaList = datasource.getAllSentMedia(10-receivedMediaList.size());
	    datasource.close();
	    
	    mainMediaList = new ArrayList<Media>(sentMediaList);
	    mainMediaList.addAll(receivedMediaList);
		mediaList = mainMediaList; 
		
		Collections.sort(mediaList, new Comparator<Media>() {
			  public int compare(Media o1, Media o2) {
				  return o2.getUpdated_at().compareTo(o1.getUpdated_at()); //descending
			  }
			});

			inboxAdapter = new InboxAdapter(this, 0, mediaList);
		    setListAdapter(inboxAdapter);
		    
		    IntentFilter filter = new IntentFilter("com.blimk.InboxBroadcast");
		    this.registerReceiver(resetReceiver, filter);
		    registerForContextMenu(getListView());
		    
		 // Creating a button - Load More
			btnLoadMore = new Button(this);
			btnLoadMore.setText("Load More");
			btnLoadMore.setOnClickListener(new View.OnClickListener() {
			    @Override
			    public void onClick(View arg0) {
			        // Starting a new async task
			       new loadMoreListView().execute();
			    }
			});
			 
			datasource.open();
			int sentMediaSize = datasource.getAllSentMedia().size();
		    //int remainingMediaSize = sentMediaSize - (10 - receivedMediaList.size()); //total sent media - what is visible
			datasource.close();
			if(sentMediaSize > 10) getListView().addFooterView(btnLoadMore);
	    
	}
	
	@Override
	public void onStart() {
		TextView emptyView = new TextView(this);
		((ViewGroup) getListView().getParent()).addView(emptyView);
		emptyView.setText("Your blimk inbox is currently empty. Tap the contacts button to view your"+
							" contacts or the camera button to click a picture");
		emptyView.setGravity(Gravity.CENTER);
		//final float scale = this.getResources().getDisplayMetrics().density;
		//int pixels = (int) (10 * scale + 0.5f);
		//emptyView.setWidth(pixels);
		getListView().setEmptyView(emptyView);
		
		super.onStart();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		//menu.add("View Image");
		super.onCreateContextMenu(menu, v, menuInfo);
	}


	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		Media media = new Media();
		Bundle bundle = new Bundle();
		bundle.putLong("id", media.getId());
		Intent newIntent = new Intent(this,ImageResult.class);
    	newIntent.putExtras(bundle);
    	startActivity(newIntent);
		return super.onContextItemSelected(item);
	}
	
	
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		Media media = mediaList.get(position);
		Bundle bundle = new Bundle();
		bundle.putByteArray("content", media.getContent());
		bundle.putString("senderLocalId", media.getSenderLocalId());
		bundle.putString("senderNumber", media.getSenderNumber());
		bundle.putString("question", media.getQuestion());
		
		//request code: last digit = type (1 = sent, 2 = received)
		//all previous digits are position
		int requestCode = position;
    	requestCode = requestCode * 10;
    	
		if(media.getSenderLocalId() != null) {
			if(!isNetworkAvailable()) {
				Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}
			bundle.putString("senderLocalId", media.getSenderLocalId());
			Intent newIntent = new Intent(this,ImageReply.class);
        	newIntent.putExtras(bundle);
        	
        	//This means the last digit will be 1, meaning this is incoming question
        	requestCode++;
        	startActivityForResult(newIntent, requestCode);
		}
		else {
			bundle.putLong("id", media.getId());
			List<MediaReply> mediaReply = media.getReplies();
			int thumbsUp=0;
			int thumbsDown=0;
			int timedOut=0;
			int noResponse=0;
			int unread=0;
			for(MediaReply m : mediaReply) {
				if(m.getAnswer() == null) {
					noResponse++;
				}
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
			}
			bundle.putInt("thumbsUp", thumbsUp);
			bundle.putInt("thumbsDown", thumbsDown);
			bundle.putInt("timedOut", timedOut);
			bundle.putInt("noResponse", noResponse);
			bundle.putInt("unread", unread);
			
			Intent newIntent = new Intent(this,ImageResultListActivity.class);
        	newIntent.putExtras(bundle);
        	//this means the last digit will be 2, meaning my question
        	requestCode += 2;
        	startActivityForResult(newIntent, requestCode);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode % 2 == 0) {
			Log.d("OnActivityResult", "******************ITS SENT**************"+requestCode);
			int position = (requestCode-1)/10;
			Media media = mediaList.get(position);
			inboxAdapter.remove(media);
			datasource.open();
			media = datasource.getSentMedia(media);
			datasource.close();
			if(-1 != media.getId()) {
				//inboxAdapter.remove(media);
				inboxAdapter.insert(media, position);
			}
			inboxAdapter.notifyDataSetChanged();
		}
		else {
			Log.d("OnActivityResult", "******************ITS RECEIVED**************"+requestCode);
			int position = (requestCode-1)/10;
			Media media = mediaList.get(position);
			datasource.open();
			datasource.deleteReceivedMedia(media);
			datasource.close();
			receivedMediaList.remove(media);
		/*	for(Media m : receivedMediaList) {
				if(m.getId() == media.getId()) receivedMediaList.remove(m);
			}	*/
			inboxAdapter.remove(media);
        	inboxAdapter.notifyDataSetChanged();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onDestroy() {
		this.unregisterReceiver(resetReceiver);
		super.onDestroy();
	}
	
	public BroadcastReceiver resetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String type = extras.getString("type");
			if(type.equals("received")) {
				long mediaId = extras.getLong("id");
				Media queryMedia = new Media();
				queryMedia.setId(mediaId);
				MainDataSource datasource = new MainDataSource(getApplicationContext());
				datasource.open();
				Media newMedia = datasource.getReceivedMedia(queryMedia);
				datasource.close();
				receivedMediaList.add(0, newMedia);
				inboxAdapter.insert(newMedia, 0);
				inboxAdapter.notifyDataSetChanged();
			}
			else {
				long mediaId = extras.getLong("id");
				Media queryMedia = new Media();
				queryMedia.setId(mediaId);
				MainDataSource datasource = new MainDataSource(getApplicationContext());
				datasource.open();
				Media newMedia = datasource.getSentMedia(queryMedia);
				sentMediaList = datasource.getAllSentMedia();
				datasource.close();
				//Get the object that we want to replace
				for(Media m : mediaList) {
					if(m.getId() == mediaId) {
						queryMedia = m;
					}
				}
				inboxAdapter.remove(queryMedia);
				inboxAdapter.insert(newMedia, 0);
				inboxAdapter.notifyDataSetChanged();
			}
			
		}
	};
	
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) this.getSystemService(this.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.inbox, menu);
		
		
		MenuItem cameraButton = (MenuItem)menu.findItem(R.id.camera);
		cameraButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent newIntent = new Intent(InboxListActivity.this,CameraActivity.class);
	        	startActivity(newIntent); 

				return false;
			}
		});
		
		MenuItem contactsButton = (MenuItem)menu.findItem(R.id.contacts);
		contactsButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent newIntent = new Intent(InboxListActivity.this,Contacts.class);
	        	startActivity(newIntent);
				
				return false;
			}
		});
		
		return true;
	}
	
	
	private class loadMoreListView extends AsyncTask<Void, Void, Void> {
		 
		ProgressDialog pDialog;
		
	    @Override
	    protected void onPreExecute() {
	        // Showing progress dialog before sending http request
	        pDialog = new ProgressDialog(InboxListActivity.this);
	        pDialog.setMessage("Please wait..");
	        pDialog.setIndeterminate(true);
	        pDialog.setCancelable(false);
	        pDialog.show();
	    }
	 
	    protected Void doInBackground(Void... unused) {
	        runOnUiThread(new Runnable() {
	            public void run() {
	              List<Media> tempList = inboxAdapter.getList();
	              Media tempMedia = tempList.get(tempList.size() - 1);
	              Timestamp updatedAt = tempMedia.getUpdated_at();
	              
	              datasource.open();
	              List<Media> tempSentList = datasource.getAllSentMedia(tempMedia, 10);
	              Log.d("WOW", "***********"+tempSentList.size()+"*****************");
	              datasource.close();
	              if(tempSentList.size() > 1) {
	            	  inboxAdapter.addAll(tempSentList);
	            	  inboxAdapter.notifyDataSetChanged();
	              }
	              if(tempSentList.size() < 10) {
	            	  btnLoadMore.setVisibility(View.INVISIBLE);
	              }
	              
	            } 
	        });
			return null; 
	    }       
	 
	    protected void onPostExecute(Void unused) {
	        // closing progress dialog
	        pDialog.dismiss();
	    } 
	}

}
