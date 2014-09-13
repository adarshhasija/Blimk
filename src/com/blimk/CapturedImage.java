package com.blimk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.blimk.R;
import com.parse.ParseObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CapturedImage extends Activity implements MediaScannerConnectionClient {
	
	private static final String TAG = CapturedImage.class.getName();
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private MediaScannerConnection conn;
	private byte[] photo;
	private File pictureFile;
	private Bitmap bitmap;
	private int rotation;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG, "............................THIS CONFIG WILL CHANGE");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rotation = getWindowManager().getDefaultDisplay().getRotation();
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.image_preview);
		RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.imagePreviewLayout);
		relativeLayout.setBackgroundColor(0xFF000000);
		
		relativeLayout = (RelativeLayout)findViewById(R.id.image_caption);
		relativeLayout.setBackgroundColor(0x88FFFFFF);
		
		Bundle extras = getIntent().getExtras();
		photo = extras.getByteArray("photo");
		String cameraType = extras.getString("cameraType");
		
		ImageView imgView = (ImageView)findViewById(R.id.image_preview);
		bitmap  = BitmapFactory.decodeByteArray (photo, 0, photo.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		if(rotation == 1) {
			rotationAngle = 0;
		}
		else if(rotation == 0 && cameraType.equals("front")) {
			rotationAngle = 270;
		}
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		imgView.setImageBitmap(bitmap); 
		
		ImageView sendBtn = (ImageView)findViewById(R.id.send_button);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				int width= bitmap.getWidth();
				int height= bitmap.getHeight();
				rotation = getWindowManager().getDefaultDisplay().getRotation();
				int rotationAngle = 270;
				if(rotation == 1) {
					rotationAngle = 90;
				}
				Matrix matrix = new Matrix();
				matrix.setRotate(rotationAngle);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
				
				
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
				byte[] byteArray = stream.toByteArray();
				
				EditText question = (EditText)findViewById(R.id.question);
				
			/*	for(int i = 0; i < photo.length / 2; i++)
				{
				    byte temp = photo[i];
				    photo[i] = photo[photo.length - i - 1];
				    photo[photo.length - i - 1] = temp;
				} */
				
				Bundle bundle = new Bundle();
	            bundle.putByteArray("photo", photo);
	            //bundle.putByteArray("photo", byteArray);
	            bundle.putString("question", question.getText().toString());
				
				Intent newIntent = new Intent(CapturedImage.this,SelectRecipients.class);
				newIntent.putExtras(bundle);
				startActivityForResult(newIntent, 0);
				//finish();
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 0 && resultCode == 2) {
			setResult(2);
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_preview, menu);
		
		MenuItem aboutImageQuestionsButton = (MenuItem)menu.findItem(R.id.action_about_image_questions);
		aboutImageQuestionsButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CapturedImage.this);
		 
					alertDialogBuilder.setTitle("About questions");
					alertDialogBuilder.setMessage("Questions related to the image must be yes/no questions only. Question "+
								"length is limited to 20 characters");
					alertDialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
					});
			
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
				
				return false;
			}
		});
		
		MenuItem clearButton = (MenuItem)menu.findItem(R.id.action_clear_image);
		clearButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				finish();
				return false;
			}
		});
		
		MenuItem saveButton = (MenuItem)menu.findItem(R.id.action_save_image);
		saveButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	            if (pictureFile == null){
	                Log.d(TAG, "Error creating media file, check storage permissions");
	                return false;
	            }
	            
	            ByteArrayOutputStream stream = new ByteArrayOutputStream();
	            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
	            byte[] data = stream.toByteArray();
	            
	            try {
	                FileOutputStream fos = new FileOutputStream(pictureFile);
	                fos.write(data);
	                fos.close();
	                Toast.makeText(CapturedImage.this, "Image saved in gallery", Toast.LENGTH_SHORT).show();
	                startScan();
	            } catch (FileNotFoundException e) {
	                Log.d(TAG, "File not found: " + e.getMessage());
	            } catch (IOException e) {
	                Log.d(TAG, "Error accessing file: " + e.getMessage());
	            }
	            
				return false;
			}
		});
		
		return true;
	}

	
	
	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	  //  File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	  //            Environment.DIRECTORY_PICTURES), "blimk"); 
		File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "blimk"); 

	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("blimk", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	    	
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	private void startScan() 
	{ 
	    if(conn!=null) conn.disconnect();  
	    conn = new MediaScannerConnection(CapturedImage.this,CapturedImage.this); 
	    conn.connect(); 
	}

	@Override
	public void onMediaScannerConnected() {
		try{
	        conn.scanFile(pictureFile.getAbsolutePath(), null);	        
	       } catch (java.lang.IllegalStateException e){
	       }
		
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		conn.disconnect();
	}
	
}
