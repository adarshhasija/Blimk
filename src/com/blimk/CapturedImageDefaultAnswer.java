package com.blimk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.blimk.R;

public class CapturedImageDefaultAnswer extends Activity {
	
	private int rotation;
	private byte[] photo;
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		rotation = getWindowManager().getDefaultDisplay().getRotation();
		setContentView(R.layout.image_default_answer);
		
		RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.imagePreviewLayout);
		relativeLayout.setBackgroundColor(0xFF000000);
		
		ImageView thumbsUp = (ImageView)findViewById(R.id.thumbs_up);
		thumbsUp.setBackgroundColor(0x66FFFFFF);
		
		ImageView thumbsDown = (ImageView)findViewById(R.id.thumbs_down);
		thumbsDown.setBackgroundColor(0x66FFFFFF);
		
		TextView descriptionText = (TextView)findViewById(R.id.description);
		descriptionText.setBackgroundColor(0x66FFFFFF);
		
		Bundle extras = getIntent().getExtras();
		photo = extras.getByteArray("photo");
		
		ImageView imgView = (ImageView)findViewById(R.id.image_preview);
		bitmap  = BitmapFactory.decodeByteArray (photo, 0, photo.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		if(rotation == 1) rotationAngle = 0; 
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		imgView.setImageBitmap(bitmap); 
		
		thumbsUp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				next(true);
				
			}
		});
		
		thumbsDown.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				next(false);
				
			}
		});
		
	}
	
	private void next(boolean defaultAnswer) {
		Bundle extras = getIntent().getExtras();
		//photo = extras.getByteArray("photo");
		
		Bundle bundle = new Bundle();
        bundle.putByteArray("photo", extras.getByteArray("photo"));
        //bundle.putString("question", extras.getString("question"));
        bundle.putBoolean("defaultAnswer", defaultAnswer);
		
		Intent newIntent = new Intent(this,SelectRecipients.class);
		newIntent.putExtras(bundle);
		startActivity(newIntent);
		finish();
	}

	
}
