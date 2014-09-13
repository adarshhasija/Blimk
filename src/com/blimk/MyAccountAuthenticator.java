package com.blimk;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MyAccountAuthenticator extends AbstractAccountAuthenticator {
	
	private static final String TAG = MyAccountAuthenticator.class.getName();
	Context mContext;

	public MyAccountAuthenticator() {
		super(null);
	}
	
	public MyAccountAuthenticator(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String account_type,
			String auth_token_type, String[] required_features, Bundle options)
			throws NetworkErrorException {
		final Intent intent = new Intent(mContext, Login.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle reply = new Bundle();
        reply.putParcelable(AccountManager.KEY_INTENT, intent); 
		

        return reply;
	}

	@Override
	public Bundle getAccountRemovalAllowed(
			AccountAuthenticatorResponse response, Account account)
			throws NetworkErrorException {
		Bundle result = new Bundle();
	    boolean allowed = true; // or whatever logic you want here
	    result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, allowed);
	    return result;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse arg0,
			Account arg1, Bundle arg2) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse arg0, Account arg1,
			String arg2, Bundle arg3) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAuthTokenLabel(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse arg0, Account arg1,
			String[] arg2) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse arg0,
			Account arg1, String arg2, Bundle arg3)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

}
