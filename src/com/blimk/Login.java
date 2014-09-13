package com.blimk;

import com.blimk.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.LogInCallback;
import com.parse.SignUpCallback;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends AccountAuthenticatorActivity {
	
	private static final String TAG = Login.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login);
		
		TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
        String mPhoneNumber = tm.getLine1Number();
		
        EditText phoneNumber = (EditText) findViewById(R.id.phone_number);
        phoneNumber.setText(mPhoneNumber);
        
		TextView login = (TextView) findViewById(R.id.login_button);
		TextView signup = (TextView) findViewById(R.id.signup_button);
		TextView about_blinkit = (TextView) findViewById(R.id.about_blinkit);
		login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			/*	Intent waIntent = new Intent(Intent.ACTION_SEND);
			    waIntent.setType("text/plain");
			            String text = "HELLO www.google.com";
			    //waIntent.setPackage("com.whatsapp");
			    if (waIntent != null) {
			        waIntent.putExtra(Intent.EXTRA_TEXT, text);
			        startActivity(Intent.createChooser(waIntent, "Send invite using"));
			    } else {
			        
			    } */
			     
				EditText phoneNumber = (EditText) findViewById(R.id.phone_number);
				EditText displayName = (EditText) findViewById(R.id.display_name);
				
			/*	ParseUser.logInInBackground(username.getText().toString(), password.getText().toString(), new LogInCallback() {
					@Override
					public void done(ParseUser user, ParseException e) {
						if (user != null) {
							Log.d(TAG, "Login successful");
							
							Intent newIntent = new Intent(Login.this,User.class);
							startActivity(newIntent);
							finish();
						} else {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Login.this);
							 
							alertDialogBuilder.setTitle("Login error");
							alertDialogBuilder.setMessage(e.getMessage());
							alertDialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int arg1) {
									// TODO Auto-generated method stub
									dialog.cancel();
								}
							});
					
							AlertDialog alertDialog = alertDialogBuilder.create();
							alertDialog.show();
						}
					}
						
				}); */
				ParseObject mObject = new ParseObject("User");
				mObject.put("phone", phoneNumber.getText().toString());
				//mObject.saveEventually();
				
				Account account = new Account("abcdef", "com.blimk");  
				AccountManager mAccountManager = AccountManager.get(getBaseContext());
				Bundle userdata = new Bundle();
				userdata.putString("SERVER", "extra");
				
				Account[] accounts = mAccountManager.getAccounts();
			    String[] names = new String[accounts.length];
			    for (int i = 0; i < names.length; i++) {
			        names[i] = accounts[i].type;
			        Log.d(TAG, names[i]);
			    }
			    //mAccountManager.addAccount("com.blimk", null, null, null, null, null, null);
			    
			    if (mAccountManager.addAccountExplicitly(account, null, userdata)) {
			    	Bundle result = new Bundle();
			        result.putString(AccountManager.KEY_ACCOUNT_NAME, "blimk");
			        result.putString(AccountManager.KEY_ACCOUNT_TYPE, "com.blimk");
			        result.putString("someKey", "stringData");
			        setAccountAuthenticatorResult(result);
			    }
				
			}
		});
		
		signup.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(Login.this,Signup.class);
				startActivity(newIntent);
				finish();	
			}
		});
		
		about_blinkit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Login.this);
				 
				alertDialogBuilder.setTitle("About blinkIt");
				alertDialogBuilder.setMessage("This is an explanation of blinkIt");
				alertDialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
		
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();	
			}
		});
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	
}
