package com.blimk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.blimk.R;


@SuppressLint("NewApi")
public class CameraFragment extends Fragment {
	private static final String TAG = MainActivity.class.getName();
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Camera mCamera=null;
    private CameraPreview mPreview;
    private FrameLayout preview;
    private LinearLayout flash;
    private LinearLayout switch_camera;
	
	public int getCurrentCamraId() {
		return currentCamraId;
	}

	public void setCurrentCamraId(int currentCamraId) {
		this.currentCamraId = currentCamraId;
	}

	public String getOriginalFlashSetting() {
		return originalFlashSetting;
	}

	public void setOriginalFlashSetting(String originalFlashSetting) {
		this.originalFlashSetting = originalFlashSetting;
	}

	private int currentCamraId = -1;
	private String originalFlashSetting;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, ".................Activity created");
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(TAG, ".................Activity resumed");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, ".................Activity started");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		inflater.inflate(R.layout.camera_preview, null);
		//connectCamera();
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	
	private void releaseCamera(){
        if (mCamera != null){
        	mCamera.stopPreview();
        	preview.removeAllViewsInLayout();
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
	
	private void connectCamera(){
		Log.d(TAG, ".................in connectCamera()");
		if(mCamera == null) {
			try {
				//int currentCameraId = mCameraFragment.getCurrentCamraId();
				int currentCameraId = 0;
				if(currentCameraId == -1) mCamera = Camera.open();
				else mCamera = Camera.open(currentCameraId);
	            setCameraView();
			} catch(Exception e) {
				
			}
		}
    }
	
	private void connectCamera(int cameraId) {
		if(mCamera == null) {
			try {
				mCamera = Camera.open(cameraId);
				//mCameraFragment.setCurrentCamraId(cameraId);
				setCameraView();
			} catch(Exception e) {
				
			}
		}
	}
	
	private void setCameraView() {
		Log.d(TAG, ".................in setCameraView()");
		//Getting the original settings so that we can reset them after we are done
		Camera.Parameters parameters = mCamera.getParameters();
        //mCameraFragment.setOriginalFlashSetting(parameters.getFlashMode());
   
        if(1 == getResources().getConfiguration().orientation) 
        	mCamera.setDisplayOrientation(90);
		mPreview = new CameraPreview(getActivity(), mCamera);
		preview.addView(mPreview);
		mCamera.startPreview();
		
	/*	ImageView flashImage = (ImageView) findViewById(R.id.flash_image);
        if(0 == Camera.Parameters.FLASH_MODE_ON.compareToIgnoreCase(parameters.getFlashMode())) {
        	flashImage.setImageResource(R.drawable.flash_on);
        }
        if(0 == Camera.Parameters.FLASH_MODE_OFF.compareToIgnoreCase(parameters.getFlashMode())) {
        	flashImage.setImageResource(R.drawable.flash_off);
        } */
	}

	
	
	

	
}
