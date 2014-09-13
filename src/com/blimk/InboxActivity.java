package com.blimk;

import java.util.List;
import com.blimk.R;
import com.parse.Parse;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActivityManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

@SuppressLint("NewApi")
public class InboxActivity extends FragmentActivity {
	
	// When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    InboxPagerAdapter mInboxPagerAdapter;
    ViewPager mViewPager;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_PROGRESS);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setProgressBarIndeterminateVisibility(true);
		setProgressBarIndeterminate(true);
		
		final ActionBar actionBar = getActionBar();
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mInboxPagerAdapter =
                new InboxPagerAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mInboxPagerAdapter);
		
		// Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				// When the tab is selected, switch to the
	            // corresponding page in the ViewPager.
	            mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
				
			}
	    	
	    };
	    
	    mViewPager = (ViewPager) findViewById(R.id.pager);
	    mViewPager.setOnPageChangeListener(
	            new ViewPager.SimpleOnPageChangeListener() {
	                @Override
	                public void onPageSelected(int position) {
	                    // When swiping between pages, select the
	                    // corresponding tab.
	                    getActionBar().setSelectedNavigationItem(position);
	                }
	            });
	    
/*	    actionBar.addTab(
                actionBar.newTab()
                        .setText("Sent")
                        .setTabListener(tabListener)); */
	    Tab all = actionBar.newTab()
                .setText("All")
                .setTabListener(tabListener);
	    actionBar.addTab(all);
	/*    actionBar.addTab(
                actionBar.newTab()
                        .setText("Received")
                        .setTabListener(tabListener)); */
	    actionBar.selectTab(all);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.inbox, menu);
		
		
		MenuItem cameraButton = (MenuItem)menu.findItem(R.id.camera);
		cameraButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent newIntent = new Intent(InboxActivity.this,CameraActivity.class);
	        	startActivity(newIntent);
	        	finish(); 

				return false;
			}
		});
		
		MenuItem contactsButton = (MenuItem)menu.findItem(R.id.contacts);
		contactsButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent newIntent = new Intent(InboxActivity.this,Contacts.class);
	        	startActivity(newIntent);
				
				return false;
			}
		});
		
		return true;
	}
	
	

	
}
