package com.blimk;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.blimk.R;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.Client;
import com.kinvey.java.LinkedResources.LinkedFile;
import com.kinvey.java.core.KinveyClientCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Base64;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//removal of contacts from list not implemented
public class SelectRecipients extends ListActivity {
	
	private static final String INDIA_PREFIX = "+91";
	
	final List<String> cloud = new ArrayList<String>();
	final List<String> localNumber = new ArrayList<String>();
	final List<String> localName = new ArrayList<String>();
	final HashMap<String, String> local=new HashMap<String, String>();
	final HashMap<String, String> localNameToNumber=new HashMap<String, String>();
	List<String> dbNumbers;
	ArrayAdapter<String> arrayAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		arrayAdapter = new ArrayAdapter<String>(
	            SelectRecipients.this, 
	            android.R.layout.simple_list_item_multiple_choice,
	            localName );
		setListAdapter(arrayAdapter);
		ListView listView = getListView();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		
        getUpdatedDeviceContactsList();
		if(!getDatabaseContacts()) {
			List<MenuItem> tmpList = new ArrayList<MenuItem>(); //sending in an empty list
			getCloudContacts(tmpList);
		}
	}
	
	@Override
	protected void onStart() {
		TextView emptyView = new TextView(this);
		((ViewGroup) getListView().getParent()).addView(emptyView);
		emptyView.setText("Your contacts list is currently empty. Please refresh to see which of your contacts is on blimk");
		emptyView.setGravity(Gravity.CENTER);
		getListView().setEmptyView(emptyView);
		super.onStart();
	}



	public void getUpdatedDeviceContactsList() {
		Cursor data = getContentResolver()
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
			if(-1 == number.indexOf("+91", 0)) {
				number = INDIA_PREFIX + number;
			}
			String name = data.getString(nameIndex);
		    if(-1 == localNumber.indexOf(number)) { 
		    	localNumber.add(number); 
		    	local.put(number, name);
		    	localNameToNumber.put(name, number);
		    }
		    i++;
		    data.moveToNext();
		}
		data.close();
	}
	
	public boolean getDatabaseContacts() {
		MainDataSource datasource = new MainDataSource(this);
		datasource.open();
		dbNumbers = datasource.getAllContacts();
		datasource.close();
		
		if(dbNumbers.size() > 0) {
			localNumber.retainAll(dbNumbers);
			Iterator<String> mapIterator = local.keySet().iterator();
			String key;
			while(mapIterator.hasNext()) {
				key=(String)mapIterator.next();
				if(localNumber.contains(key)) {
					//localName.add((String)local.get(key));
					arrayAdapter.add((String)local.get(key));
				}
			}
			arrayAdapter.notifyDataSetChanged();
			return true;
		}
		return false;
	}
	
	private void getCloudContacts(final List<MenuItem> list) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> userList, ParseException e) {
				if (e == null) {
					MainDataSource datasource = new MainDataSource(SelectRecipients.this);
					datasource.open();
					for(ParseObject user: userList) {
						if(-1 == dbNumbers.indexOf(user.get("phone"))) {
							datasource.createContact(user.getString("phone"));
							dbNumbers.add(user.getString("phone"));
							//cloud.add(user.getString("phone").replaceAll("\\s+",""));
						}
					}
					datasource.close();
					//localNumber.retainAll(cloud);
					localNumber.retainAll(dbNumbers);
					Iterator<String> mapIterator = local.keySet().iterator();
					String key;
					while(mapIterator.hasNext()) {
						key=(String)mapIterator.next();
						if(localNumber.contains(key)) {
							//localName.add((String)local.get(key));
							if(-1 == arrayAdapter.getPosition((String)local.get(key))) {
								arrayAdapter.add((String)local.get(key));
							}
						}
					}
					arrayAdapter.notifyDataSetChanged();
					
					for(int i=0; i < list.size(); i++) {
						if(i==0) list.get(i).setVisible(false); //This is the progress button
						else list.get(i).setVisible(true);
					}
					setTitle("Select Contacts");
		        } else {
		            Log.d("score", "Error: " + e.getMessage()+"*************************************");
		        }
				
			}
		}); 
	}
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		boolean prevState = l.isItemChecked(position);
		l.setItemChecked(position, prevState);
	}


	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.select_recipients, menu);
		
		final MenuItem progressButton = (MenuItem)menu.findItem(R.id.progress);
		progressButton.setVisible(false);
		
		final MenuItem sendNowButton = (MenuItem)menu.findItem(R.id.action_send_now);
		sendNowButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(!isNetworkAvailable()) {
					Toast.makeText(SelectRecipients.this, "No internet connection", Toast.LENGTH_SHORT).show();
					return false;
				}
				ListView listview = getListView();
				SparseBooleanArray checkedItemPositions = listview.getCheckedItemPositions();
				if(checkedItemPositions.size() < 1) {
					Toast.makeText(SelectRecipients.this, "No contacts have been selected", Toast.LENGTH_SHORT).show();
					return false;
				} 
				
				List<String> finalNumbers = new ArrayList<String>();
				//go through the entire array. If a position is declared checked in the sparse array, take it
				for(int i=0; i < arrayAdapter.getCount(); i++) {
					if(checkedItemPositions.get(i)) {
						String number = localNameToNumber.get(arrayAdapter.getItem(i));
						finalNumbers.add(number);
					}
				}
				//finalNumbers.add("987654321");
				
				Bundle extras = getIntent().getExtras();
				byte[] photo = extras.getByteArray("photo");
				MainDataSource datasource = new MainDataSource(SelectRecipients.this);
			    datasource.open();
			    Media result = datasource.createSentMedia(photo, finalNumbers);
			    datasource.createSentQuestion(result.getId(), extras.getString("question"));
			    datasource.close();
				int resultId = (int)result.getId();
				String resultIdString = Integer.toString(resultId); 
				ParseObject mObject = new ParseObject("Media");
				mObject.put("content", photo);
				//Pull out the user number from the global variable
				String senderNumber = ((MyApplication) SelectRecipients.this.getApplication()).getPhoneNumber();
				mObject.put("senderNumber", senderNumber);
				mObject.put("senderLocalId", resultIdString);
				mObject.put("question", extras.getString("question"));
				JSONObject jsonObj;
				List<ParsePush> notifList = new ArrayList<ParsePush>();
				try {
					for(String sendTo : finalNumbers) {
						jsonObj=new JSONObject();
			        	jsonObj.put("action", "com.blimk.NEW");
			        	jsonObj.put("senderNumber", senderNumber);
			        	jsonObj.put("question", extras.getString("question"));
						jsonObj.put("senderLocalId", resultIdString);
						ParseQuery pushQuery = ParseInstallation.getQuery();
						pushQuery.whereEqualTo("phone", sendTo);
						ParsePush push = new ParsePush();
						push.setQuery(pushQuery); // Set our Installation query
						push.setData(jsonObj);
						notifList.add(push);
						pushQuery = null;
						jsonObj=null;
						//push.setMessage("This is a push notification that I sent");
						//push.sendInBackground();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				for(ParsePush push : notifList) {
					push.sendInBackground();
				} 
				mObject.saveEventually();
				
				
				Intent inboxIntent = new Intent(SelectRecipients.this,InboxListActivity.class);
	        	startActivity(inboxIntent);
	        	setResult(2);
	        	finish(); 
				
				return false;
			}
		});
		
		final MenuItem selectAllButton = (MenuItem)menu.findItem(R.id.select_all);
		selectAllButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				ListView listview = getListView();
				for (int i = 0; i < listview.getCount(); i++)
				      listview.setItemChecked(i, true);
				
				return false;
			}
		});
		
		final MenuItem unSelectAllButton = (MenuItem)menu.findItem(R.id.unselect_all);
		unSelectAllButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				ListView listview = getListView();
				for (int i = 0; i < listview.getCount(); i++)
				      listview.setItemChecked(i, false);
				
				return false;
			}
		});
		
		final MenuItem refreshButton = (MenuItem)menu.findItem(R.id.refresh);
		refreshButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				refreshButton.setVisible(false);
				sendNowButton.setVisible(false);
				selectAllButton.setVisible(false);
				unSelectAllButton.setVisible(false);
				
				progressButton.setActionView(R.layout.action_progressbar);
	            progressButton.expandActionView();
				progressButton.setVisible(true);
				
				List<MenuItem> menuList = new ArrayList<MenuItem>();
				menuList.add(progressButton);
				menuList.add(refreshButton);
				menuList.add(unSelectAllButton);
				menuList.add(selectAllButton);
				menuList.add(sendNowButton);
				
				setTitle("Loading...");
				
				getUpdatedDeviceContactsList();
				getCloudContacts(menuList);
				
				return false;
			}
		});
		
		return true;
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}



}
