package com.blimk;

import java.util.List;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.PushService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;

public class MyApplication extends Application{

	@Override
	public void onCreate() {
		//Parse.initialize(this, "6fscgNHb7qwkxv9NIY5dqY0w7SVZGG7m52UfyYIq", "9i5oriDK6VR5ENkpetL60XuBu8VYdm4H943e020C");  //parse-dev
		Parse.initialize(this, "mgz7E9mCGYdWmY7bYuWt8agFgg2gWNtiQfCkv63E", "tC0xarnEuV86zS7Itjhmi1QRY3n14aivdJPMYnCY");  //parse-cloud
		PushService.setDefaultPushCallback(this, InboxActivity.class);
		
		AccountManager mAccountManager = AccountManager.get(getBaseContext());
		Account[] accounts = mAccountManager.getAccountsByType("com.blimk");
		if(accounts.length > 0) {
			Account account = accounts[0];
			phoneNumber = account.name;
		}
		
		super.onCreate();
	}

	private String phoneNumber;
	private String name;

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
