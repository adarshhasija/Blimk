package com.blimk;

import java.util.ArrayList;
import java.util.List;
import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseUser;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseException;
import com.parse.GetCallback;
import android.database.Cursor;
import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.ListActivity;
import android.content.CursorLoader;
import android.content.Loader;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

@SuppressLint("NewApi")
public class User extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final String TAG = User.class.getName();
	
	// This is the Adapter being used to display the list's data
    //SimpleCursorAdapter mAdapter;
	ArrayAdapter<String> mAdapter=null;
	
	// These are the Contacts rows that we will retrieve
  //  static final String[] PROJECTION = new String[] {ContactsContract.Data._ID, 
  //          ContactsContract.Data.DISPLAY_NAME, Phone.NUMBER};
  //  static final String[] PROJECTION = new String[] {ContactsContract.Data._ID, 
  //      ContactsContract.Data.DISPLAY_NAME, Phone.NUMBER};
    static final String[] PROJECTION = new String[] {Phone._ID, 
        Phone.DISPLAY_NAME, Phone.NUMBER};
	
 // This is the select criteria
 //   static final String SELECTION = "((" + 
 //           ContactsContract.Data.DISPLAY_NAME + " NOTNULL) AND (" +
 //           ContactsContract.Data.DISPLAY_NAME + " != '' ))";
    static final String SELECTION = "((" + 
            Phone.DISPLAY_NAME + " NOTNULL) AND (" +
           Phone.NUMBER + " != '' ))";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.user);
		
		// Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);
        
     // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
        
     // For the cursor adapter, specify which columns go into which views
     //   String[] fromColumns = {ContactsContract.Data.DISPLAY_NAME, Phone.NUMBER};
        String[] fromColumns = {Phone.DISPLAY_NAME, Phone.NUMBER};
        int[] toViews = {android.R.id.text1, android.R.id.text2 }; // The TextView in simple_list_item_1
        
     // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
     /*   mAdapter = new SimpleCursorAdapter(this, 
                android.R.layout.simple_list_item_2, null,
                fromColumns, toViews, 0); */
        setListAdapter(mAdapter);

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.user, menu);
		
		MenuItem signout = (MenuItem)menu.findItem(R.id.signout);
		signout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				//ParseUser.logOut();
				finish();
				return false;
			}
		});
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                PROJECTION, SELECTION, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
		final List<String> cloud = new ArrayList<String>();
		final List<String> local = new ArrayList<String>();

		data = getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, null, null,  Phone.DISPLAY_NAME + " ASC");
		
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
		query.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> userList, ParseException e) {
				if (e == null) {
		            //Log.d("score", "*********************************" + userList.get(0).getString("phone") + " ******************************");
					for(ParseObject user: userList) {
						cloud.add(user.getString("phone"));
					}
					
		        } else {
		            Log.d("score", "Error: " + e.getMessage()+"*************************************");
		        }
				
			}
		});
		int i=0;
		data.moveToFirst();
		while (data.isAfterLast() == false) 
		{
			//String number = data.getString(data.getColumnIndex(Phone.NUMBER));
			int columnIndex = data.getColumnIndex(Phone.NUMBER);
		    local.add(data.getString(columnIndex));
		    i++;
		    data.moveToNext();
		}
		
		cloud.retainAll(local);
		
		mAdapter = new ArrayAdapter<String>(
                this, 
                android.R.layout.simple_list_item_1,
                cloud );
		
        //mAdapter.swapCursor(data);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        //mAdapter.swapCursor(null);
		mAdapter = null;
		
	}

	
}
