package com.blimk;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blimk.R;

public class InboxAdapter extends ArrayAdapter<Media> {
	
	private final Context context;
	//private final String[] values;
	private final List<Media> mediaList;
	static class ViewHolder {
		  TextView firstLine;
		  TextView secondLine;
		  ImageView iconView;
		  boolean bitmapApplied=false;
		}

	public InboxAdapter(Context context, int resource, List<Media> values) {
		super(context, resource, values);
		this.context = context;
	    //this.values = values;
		this.mediaList = values;
	}
	
	public List<Media> getList() {
		return mediaList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		ViewHolder viewHolder;
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.inbox_row_layout, parent, false);
			
			viewHolder = new ViewHolder();
			viewHolder.firstLine = (TextView) convertView.findViewById(R.id.firstLine);
			viewHolder.secondLine = (TextView) convertView.findViewById(R.id.secondLine);
			viewHolder.iconView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
	/*	View rowView = inflater.inflate(R.layout.inbox_row_layout, parent, false);
		TextView firstLine = (TextView) rowView.findViewById(R.id.firstLine);
	    TextView secondLine = (TextView) rowView.findViewById(R.id.secondLine);
	    ImageView imageView = (ImageView) rowView.findViewById(R.id.icon); */
	    final Media media = mediaList.get(position);
	    List<MediaReply> replyList = media.getReplies();
	    
	    viewHolder.secondLine.setText("No status");
	    if (media.getSenderLocalId() != null) {
	      viewHolder.iconView.setImageResource(R.drawable.inbox);
	      viewHolder.firstLine.setText(getContactName(media.getSenderNumber()));
	      viewHolder.secondLine.setText("");
	    } else {
	    	if(replyList.size() > 1) {
	    		viewHolder.firstLine.setText("Sent to "+media.getReplies().size()+" contacts");
	    	}
	    	else if(replyList.size() > 0) {
	    		String recipientNumber = replyList.get(0).getPhoneNumber();
	    		viewHolder.firstLine.setText(getContactName(recipientNumber));
	    	}
	    	else {
	    		viewHolder.firstLine.setText("Unknown Recipient");
	    	}
	    		int repliesReceived=0;
	    		int unread=0;
	    		for(MediaReply m : replyList) {
	    			if(m.getAnswer() != null) {
	    				repliesReceived++;
	    				if(m.getReadStatus().equals("unread")) {
	    					unread++;
	    				}
	    			}
	    		}
	    		if(unread > 0) {
	    			if(unread == 1) {
	    				viewHolder.secondLine.setText("1 new reply received");
	    			}
	    			else {
	    				viewHolder.secondLine.setText(unread+" new replies received");
	    			}
	    			viewHolder.secondLine.setTypeface(null, Typeface.BOLD);
	    		}
	    		else if(repliesReceived == 0) {
	    			viewHolder.secondLine.setText("No replies received");
	    		}
	    		else if(repliesReceived == replyList.size()) {
	    			viewHolder.secondLine.setText("All replies received");
	    			viewHolder.secondLine.setTypeface(null, Typeface.NORMAL);
	    		}
	    		else if(repliesReceived == 1) {
	    			viewHolder.secondLine.setText(repliesReceived+" reply received");
	    		}
	    		else {
	    			viewHolder.secondLine.setText(repliesReceived+" replies received");
	    		}
	    	
	    	//setThumbImage(media,viewHolder.iconView);
	    		new AsyncTask<ViewHolder, Void, Bitmap>() {
	    		    private ViewHolder v;

	    		    @Override
	    		    protected Bitmap doInBackground(ViewHolder... params) {
	    		        v = params[0];
	    		        return setThumbImage(media, null);
	    		    }

	    		    @Override
	    		    protected void onPostExecute(Bitmap result) {
	    		     //   if (v.bitmapApplied == false) {
	    		            v.iconView.setImageBitmap(result);
	    		            v.bitmapApplied = true;
	    		     //   }
	    		        super.onPostExecute(result);
	    		    }
	    		}.execute(viewHolder); 
	    }

	    return convertView;
	}
	
	private Bitmap setThumbImage(Media media, ImageView imgView) {
		byte[] content = media.getContent();
		Bitmap bitmap  = BitmapFactory.decodeByteArray (content, 0, content.length);
		int width= bitmap.getWidth();
		int height= bitmap.getHeight();
		Matrix matrix = new Matrix();
		int rotationAngle = 90;
		//if(rotation == Surface.ROTATION_180) rotationAngle = 90; 
		matrix.setRotate(rotationAngle);
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(bitmap, 
                64, 64);
		return ThumbImage;
		//imgView.setImageBitmap(ThumbImage);
	}
	
	private String getContactName(String receivedNumber) {
		String result = null;
		String number = null;
		Cursor data = getContext().getContentResolver()
				.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER}, null, null,  Phone.DISPLAY_NAME + " ASC");
		
		int i=0;
		data.moveToFirst();
		while (data.isAfterLast() == false) 
		{
			//String number = data.getString(data.getColumnIndex(Phone.NUMBER));
			int numberIndex = data.getColumnIndex(Phone.NUMBER);
			int nameIndex = data.getColumnIndex(Phone.DISPLAY_NAME);
			number = data.getString(numberIndex).replaceAll("\\s+","");
			if(-1 == number.indexOf("+91", 0)) {
				number = "+91" + number;
			}
			String name = data.getString(nameIndex);
		    if(number.equals(receivedNumber)) { 
		    	//result = name.split("\\s+")[0]; //return only the first name
		    	result = name;
		    }
		    i++;
		    data.moveToNext();
		}
		data.close();
		
		if(result == null) {
			result = number;
		}
		return result;
	}
	
	

}
