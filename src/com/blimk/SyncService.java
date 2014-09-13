package com.blimk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {
	
	private static final Object _sync_adapter_lock = new Object();
    private static SyncAdapter _sync_adapter = null;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		//return _sync_adapter.getSyncAdapterBinder();
		MyAccountAuthenticator authenticator = new MyAccountAuthenticator(this);
        return authenticator.getIBinder();
	}

	@Override
	public void onCreate() {
		synchronized (_sync_adapter_lock) {
            if (_sync_adapter == null)
                _sync_adapter = new SyncAdapter(getApplicationContext(), false);
        }
	}

}
