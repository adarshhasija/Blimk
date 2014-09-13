package com.blimk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.blimk.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountAuthenticatorActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Signup extends AccountAuthenticatorActivity {
	
	private static final String TAG = Signup.class.getName();
	private static final String INDIA_PREFIX = "+91";
	private Spinner spinner;
	private Map<String, String> isoMap = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		
		spinner = (Spinner) findViewById(R.id.countries_spinner);	
		String[] isoCountries = Locale.getISOCountries();
		ArrayList<String> countries = new ArrayList<String>();
		for(String countryISO : isoCountries) {
			Locale locale = new Locale("en", countryISO);
            String name = locale.getDisplayCountry();
            countries.add(name);
            isoMap.put(name, countryISO);
		}
		Collections.sort(countries);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, countries);
		
		dataAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
		Locale current = getResources().getConfiguration().locale;
		spinner.setSelection(countries.indexOf(current.getDisplayCountry()));
		
		TextView signup = (TextView) findViewById(R.id.signup_button);
		signup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!isNetworkAvailable()) {
					Toast.makeText(Signup.this, "No internet connection", Toast.LENGTH_SHORT).show();
					return;
				}
				EditText phone = (EditText) findViewById(R.id.phone);
				EditText full_name = (EditText) findViewById(R.id.full_name);
				String phoneNumber = phone.getText().toString();
				String fullName = full_name.getText().toString();
				if(phoneNumber.length() < 1 || fullName.length() < 1) {
					Toast.makeText(Signup.this, "One or more fields are blank. Please fill out all details", Toast.LENGTH_SHORT).show();
					return;
				}
				phoneNumber = phoneNumber.replaceAll("[^\\d+]", "");
				//if(phoneNumber.indexOf(INDIA_PREFIX, 0) == -1) {
				if(phoneNumber.indexOf("+", 0) == -1) {
					String country = spinner.getSelectedItem().toString();
					String countryISO = isoMap.get(country);
					String countryCode = Iso2Phone.getPhone(countryISO);
					phoneNumber = countryCode + phoneNumber;
				}
				//Save it as a global variable
				((MyApplication) Signup.this.getApplication()).setPhoneNumber(phoneNumber);
				((MyApplication) Signup.this.getApplication()).setName(fullName);
				//Then create a new installation object
				ParseInstallation installation = ParseInstallation.getCurrentInstallation();
				installation.put("phone", phoneNumber);
				installation.saveInBackground();
				//Finally create a local account
				Account account = new Account(phoneNumber, "com.blimk");  
				AccountManager mAccountManager = AccountManager.get(Signup.this);
				Bundle userdata = new Bundle();
				userdata.putString("name", fullName);
				if (mAccountManager.addAccountExplicitly(account, null, userdata)) {
			    	Bundle result = new Bundle();
			        result.putString(AccountManager.KEY_ACCOUNT_NAME, "blimk");
			        result.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.blimk");
			        result.putString("someKey", "stringData");
			        setAccountAuthenticatorResult(result);
			    }
				saveInParseCloud(phoneNumber, fullName);
				
				Intent newIntent = new Intent(Signup.this,InboxListActivity.class);
				startActivity(newIntent);
				finish();	
				
			}
		});
	}
	
	private void saveInParseCloud(final String phoneNumber, final String fullName) {
		ParseQuery<ParseObject> query=null;
		query = ParseQuery.getQuery("User");
		query.whereEqualTo("phone", phoneNumber);
		//query.whereEqualTo("fullName", fullName);
		
		final FindCallback<ParseObject> parseCallback = new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> scoreList, ParseException e) {
				if (e == null) {
					if(scoreList.size() > 0) {
						//User exists, do nothing
					}
					else {
						//User does not exist, create user
						ParseObject parseUser = new ParseObject("User");
						parseUser.put("phone", phoneNumber);
						parseUser.put("fullname", fullName);
						parseUser.saveInBackground();
					}
		        } else {
		            Log.d("score", "Error: " + e.getMessage());
		        }
				
			}
		    
		};

		query.findInBackground(parseCallback);
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	
}
