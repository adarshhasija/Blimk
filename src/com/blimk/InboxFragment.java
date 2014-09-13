package com.blimk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class InboxFragment extends ListFragment {
	public static final String ARG_OBJECT = "object";
	List<Media> sentMediaList = null;
	List<Media> receivedMediaList = null;
	List<Media> mainMediaList = null;
	List<Media> mediaList = null;
	private InboxAdapter inboxAdapter;
	MainDataSource datasource;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Bundle args = getArguments();
		int tab = args.getInt(ARG_OBJECT);
		String[] values = null;
		datasource = new MainDataSource(getActivity());
	    datasource.open();
	    sentMediaList = datasource.getAllSentMedia(20);
	    receivedMediaList = datasource.getAllReceivedMedia();
	    datasource.close();
	    
	    if(tab == 0) {
			mainMediaList = new ArrayList<Media>(sentMediaList);
		    mainMediaList.addAll(receivedMediaList);
			mediaList = mainMediaList; 
		}	
	/*	if(tab == 1) {
			mediaList = receivedMediaList;
		}	*/
		
		Collections.sort(mediaList, new Comparator<Media>() {
			  public int compare(Media o1, Media o2) {
				  return o2.getUpdated_at().compareTo(o1.getUpdated_at()); //descending
			  }
			});

			inboxAdapter = new InboxAdapter(getActivity(), 0, mediaList);
		    setListAdapter(inboxAdapter);
		    
		    IntentFilter filter = new IntentFilter("com.blimk.InboxBroadcast");
		    getActivity().registerReceiver(resetReceiver, filter);
		    
		    String s = ((MyApplication) getActivity().getApplication()).getName();
			//Log.d("InboxActivity", "******************"+s+"***********************");
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	

	@Override
	public void onStart() {
		TextView emptyView = new TextView(getActivity());
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
	public void onActivityCreated(Bundle savedInstanceState) {
		registerForContextMenu(getListView());
		super.onActivityCreated(savedInstanceState);
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
		Intent newIntent = new Intent(getActivity(),ImageResult.class);
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
				Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
				return;
			}
			bundle.putString("senderLocalId", media.getSenderLocalId());
			Intent newIntent = new Intent(getActivity(),ImageReply.class);
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
			
			Intent newIntent = new Intent(getActivity(),ImageResultListActivity.class);
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
	public void onDestroyView() {
		getActivity().unregisterReceiver(resetReceiver);
		super.onDestroyView();
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
				MainDataSource datasource = new MainDataSource(getActivity());
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
				MainDataSource datasource = new MainDataSource(getActivity());
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
	          = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
}
