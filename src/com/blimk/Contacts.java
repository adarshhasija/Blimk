package com.blimk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

@SuppressLint("NewApi")
public class Contacts extends ListActivity {
	
	private static final String INDIA_PREFIX = "+91";
	
	final List<String> cloud = new ArrayList<String>();
	final List<String> localNumber = new ArrayList<String>();
	final List<String> localName = new ArrayList<String>();
	final HashMap<String, String> local=new HashMap<String, String>();
	List<String> dbNumbers;
	ArrayAdapter<String> arrayAdapter = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		
		arrayAdapter = new ArrayAdapter<String>(
	            Contacts.this, 
	            android.R.layout.simple_list_item_1,
	            localName );
		setListAdapter(arrayAdapter);
		
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
			String name = data.getString(nameIndex);
			if(-1 == number.indexOf("+91", 0)) {
				number = INDIA_PREFIX + number;
			}
		    if(-1 == localNumber.indexOf(number)) { 
		    	localNumber.add(number); 
		    	local.put(number, name);
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
					MainDataSource datasource = new MainDataSource(Contacts.this);
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
					setTitle("Contacts");
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
	}

	private void setList() {
		Cursor data = getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, null, null,  Phone.DISPLAY_NAME + " ASC");
		
		final List<String> cloud = new ArrayList<String>();
		final List<String> localNumber = new ArrayList<String>();
		final List<String> localName = new ArrayList<String>();
		final HashMap<String, String> local=new HashMap<String, String>();
		
		int i=0;
		data.moveToFirst();
		while (data.isAfterLast() == false) 
		{
			//String number = data.getString(data.getColumnIndex(Phone.NUMBER));
			int numberIndex = data.getColumnIndex(Phone.NUMBER);
			int nameIndex = data.getColumnIndex(Phone.DISPLAY_NAME);
			String number = data.getString(numberIndex).replaceAll("\\s+","");
			String name = data.getString(nameIndex);
		    localNumber.add(number);
		    local.put(number, name);
		    i++;
		    data.moveToNext();
		}
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> userList, ParseException e) {
				if (e == null) {
					for(ParseObject user: userList) {
						cloud.add(user.getString("phone").replaceAll("\\s+",""));
					}
					localNumber.retainAll(cloud);
					Iterator<String> mapIterator = local.keySet().iterator();
					String key;
					while(mapIterator.hasNext()) {
						key=(String)mapIterator.next();
						if(localNumber.contains(key)) {
							localName.add((String)local.get(key));
						}
					}
					//Log.d("cloud", "**********clouds*********************"+a.get(0).toString()+"*******************size******************");
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
			                Contacts.this, 
			                android.R.layout.simple_list_item_1,
			                localName );
			        // Bind to our new adapter.
			        setListAdapter(arrayAdapter);
		        } else {
		            Log.d("score", "Error: " + e.getMessage()+"*************************************");
		        }
				
			}
		}); 
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.contacts, menu);
		
		final MenuItem progressButton = (MenuItem)menu.findItem(R.id.progress);
		progressButton.setVisible(false);
		
		MenuItem tellAFriendButton = (MenuItem)menu.findItem(R.id.tell_a_friend);
		tellAFriendButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent waIntent = new Intent(Intent.ACTION_SEND);
			    waIntent.setType("text/plain");
			            String text = "This app is cool, try it out: https://play.google.com/store/apps/details?id=com.blimk";
			    //waIntent.setPackage("com.whatsapp");
			    if (waIntent != null) {
			        waIntent.putExtra(Intent.EXTRA_TEXT, text);
			        startActivity(Intent.createChooser(waIntent, "Send invite using"));
			    } else {
			        
			    }
				
				return false;
			}
		}); 
		
		final MenuItem refreshButton = (MenuItem)menu.findItem(R.id.refresh);
		refreshButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				refreshButton.setVisible(false);
				progressButton.setActionView(R.layout.action_progressbar);
	            progressButton.expandActionView();
				progressButton.setVisible(true);
				
				List<MenuItem> menuList = new ArrayList<MenuItem>();
				menuList.add(progressButton);
				menuList.add(refreshButton);
				
				setTitle("Loading...");
				
				getUpdatedDeviceContactsList();
				getCloudContacts(menuList);
				
				return false;
			}
		});
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    // Respond to the action bar's Up/Home button
		    case android.R.id.home:
		        Intent upIntent = new Intent(this,InboxActivity.class);  //NavUtils.getParentActivityIntent(this);
		        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
		            // This activity is NOT part of this app's task, so create a new task
		            // when navigating up, with a synthesized back stack.
		            TaskStackBuilder.create(this)
		                    // Add all of this activity's parents to the back stack
		                    .addNextIntentWithParentStack(upIntent)
		                    // Navigate up to the closest parent
		                    .startActivities();
		        } else {
		            // This activity is part of this app's task, so simply
		            // navigate up to the logical parent activity.
		            NavUtils.navigateUpTo(this, upIntent);
		        }
		        return true;
	    }
		return super.onOptionsItemSelected(item);
	}
	
	

	
}
