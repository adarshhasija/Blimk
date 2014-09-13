package com.blimk;

import com.blimk.R;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class CameraActivity extends Activity implements SensorEventListener {
	
	private SensorManager mSensorManager;
	private Sensor mOrientation;
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static final String TAG = CameraActivity.class.getName();
	private Camera mCamera=null;
    private CameraPreview mPreview;
    private FrameLayout preview;
    private LinearLayout flash;
    private LinearLayout switch_camera;
    private ImageView inbox;
    private CameraFragment mCameraFragment;
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: "/* + e.getMessage()*/);
                return;
            }

            String cameraType = "back";
            if(mCameraFragment.getCurrentCamraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            	cameraType = "front";
            }
            Bundle bundle = new Bundle();
            bundle.putByteArray("photo", data);
            bundle.putString("cameraType", cameraType);

            Intent newIntent = new Intent(CameraActivity.this,CapturedImage.class);
            newIntent.putExtras(bundle);
            //startActivity(newIntent);
            startActivityForResult(newIntent,0);

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(mCameraFragment.getOriginalFlashSetting());
                mCamera.setParameters(parameters);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Parse.initialize(this, "mgz7E9mCGYdWmY7bYuWt8agFgg2gWNtiQfCkv63E", "tC0xarnEuV86zS7Itjhmi1QRY3n14aivdJPMYnCY");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_preview);
		if(!checkCameraHardware(this)) return;
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	    mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		
		FragmentManager fm = getFragmentManager();
	    mCameraFragment = (CameraFragment) fm.findFragmentByTag("cameraFragment");
	    
	    if (mCameraFragment == null) {
	        mCameraFragment = new CameraFragment();
	        fm.beginTransaction().add(mCameraFragment, "cameraFragment").commit();
	    }
		
		
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        flash = (LinearLayout) findViewById(R.id.flash_container);
        switch_camera = (LinearLayout) findViewById(R.id.switch_camera_container);
        inbox = (ImageView) findViewById(R.id.inbox);
        connectCamera();
        inbox.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent newIntent = new Intent(CameraActivity.this,InboxListActivity.class);
	        	startActivity(newIntent);
	        	finish();
			}
		});
        preview.setOnTouchListener(new OnSwipeTouchListener() {
        	public void onTouch() {
        		mCamera.takePicture(null, null, mPicture);
        	}
            public void onSwipeUp() {
            	toggleFlash();
            }
            public void onSwipeDown() {
            	toggleFlash();
            }
            public void onSwipeRight() {
            	switchCamera();
            	
            }
            public void onSwipeLeft() {
            	switchCamera();
            }
        });
        flash.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleFlash();
			}
		});
        switch_camera.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchCamera();
			}
		});
		
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == 0 && resultCode == 2) {
			finish();
		}
	}



	/** Check if this device has a camera */
	private boolean checkCameraHardware(Context context) {
	    if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
	        // this device has a camera
	        return true;
	    } else {
	        // no camera on this device
	        return false;
	    }
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
		if(mCamera == null) {
			try {
				int currentCameraId = mCameraFragment.getCurrentCamraId();
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
				mCameraFragment.setCurrentCamraId(cameraId);
				setCameraView();
			} catch(Exception e) {
				
			}
		}
	}
	
	private void setCameraView() {
		//Getting the original settings so that we can reset them after we are done
		Camera.Parameters parameters = mCamera.getParameters();
        mCameraFragment.setOriginalFlashSetting(parameters.getFlashMode());
   
        if(1 == getResources().getConfiguration().orientation) 
        	mCamera.setDisplayOrientation(90);
		mPreview = new CameraPreview(CameraActivity.this, mCamera);
		preview.addView(mPreview);
		mCamera.startPreview();
		
		ImageView flashImage = (ImageView) findViewById(R.id.flash_image);
		LinearLayout flashContainer = (LinearLayout) findViewById(R.id.flash_container);
		LinearLayout switchCamera = (LinearLayout) findViewById(R.id.switch_camera_container);
		if(Camera.getNumberOfCameras() < 2) {
			switchCamera.setVisibility(View.INVISIBLE);
		}
		if(parameters.getSupportedFlashModes() == null) {
			flashContainer.setVisibility(View.INVISIBLE);
		}
		else {
			flashContainer.setVisibility(View.VISIBLE);
	        if(0 == Camera.Parameters.FLASH_MODE_ON.compareToIgnoreCase(parameters.getFlashMode())) {
	        	flashImage.setImageResource(R.drawable.flash_on);
	        }
	        if(0 == Camera.Parameters.FLASH_MODE_OFF.compareToIgnoreCase(parameters.getFlashMode())) {
	        	flashImage.setImageResource(R.drawable.flash_off);
	        }
		}
	}
	
	private void toggleFlash() {
		Camera.Parameters parameters = mCamera.getParameters();      
        ImageView flashImage = (ImageView) findViewById(R.id.flash_image);
        
        if(mCameraFragment.getCurrentCamraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        	Toast.makeText(CameraActivity.this, "Sorry, no flash", Toast.LENGTH_SHORT).show();
        	return;
        }
        
        if(0 == Camera.Parameters.FLASH_MODE_ON.compareToIgnoreCase(parameters.getFlashMode())) {
        	parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        	flashImage.setImageResource(R.drawable.flash_off);
        }
        else if(0 == Camera.Parameters.FLASH_MODE_OFF.compareToIgnoreCase(parameters.getFlashMode())) {
        	parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
        	flashImage.setImageResource(R.drawable.flash_on);
        }
        mCamera.setParameters(parameters);
	}
	
	private void switchCamera() {
		releaseCamera();
    	if(mCameraFragment.getCurrentCamraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
    		connectCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
    		ImageView flashImage = (ImageView) findViewById(R.id.flash_image);
    		flashImage.setImageResource(R.drawable.flash_off);
    		mCameraFragment.setCurrentCamraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
    	}
    	else {
    		connectCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
    		mCameraFragment.setCurrentCamraId(Camera.CameraInfo.CAMERA_FACING_BACK);
    	}

	}
	
	
	@Override
	protected void onDestroy() {
		releaseCamera();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		releaseCamera();
		super.onPause();
	}

	@Override
	protected void onResume() {
		connectCamera();
		super.onResume();
	}
	

	@Override
	protected void onRestart() {
		connectCamera();
		super.onRestart();
	}

	@Override
	protected void onStart() {
		//Toast.makeText(CameraActivity.this, "started", Toast.LENGTH_SHORT).show();
		connectCamera();
		super.onStart();
	}

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	/*    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }  */
		
	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File("IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "aklfhlakhflwahflwhfjlkweahflewhflaw");
		if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			//Log.e(TAG, "aklfhlakhflwahflwhfjlkweahflewhflaw");
		}
	}
	
/*	void createExternalStoragePublicPicture() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory.  Note that you should be careful about
	    // what you place here, since the user often manages these files.  For
	    // pictures and other media owned by the application, consider
	    // Context.getExternalMediaDir().
	    File path = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File file = new File(path, "DemoPicture.jpg");

	    try {
	        // Make sure the Pictures directory exists.
	        path.mkdirs();

	        // Very simple code to copy a picture from the application's
	        // resource into the external file.  Note that this code does
	        // no error checking, and assumes the picture is small (does not
	        // try to copy it in chunks).  Note that if external storage is
	        // not currently mounted this will silently fail.
	        InputStream is = getResources().openRawResource(R.drawable.balloons);
	        OutputStream os = new FileOutputStream(file);
	        byte[] data = new byte[is.available()];
	        is.read(data);
	        os.write(data);
	        is.close();
	        os.close();

	        // Tell the media scanner about the new file so that it is
	        // immediately available to the user.
	        MediaScannerConnection.scanFile(this,
	                new String[] { file.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	                Log.i("ExternalStorage", "Scanned " + path + ":");
	                Log.i("ExternalStorage", "-> uri=" + uri);
	            }
	        });
	    } catch (IOException e) {
	        // Unable to create file, likely because external storage is
	        // not currently mounted.
	        Log.w("ExternalStorage", "Error writing " + file, e);
	    }
	}

	void deleteExternalStoragePublicPicture() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory and delete the file.  If external
	    // storage is not currently mounted this will fail.
	    File path = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File file = new File(path, "DemoPicture.jpg");
	    file.delete();
	}

	boolean hasExternalStoragePublicPicture() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory and check if the file exists.  If
	    // external storage is not currently mounted this will think the
	    // picture doesn't exist.
	    File path = Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES);
	    File file = new File(path, "DemoPicture.jpg");
	    return file.exists();
	} */

}
