package com.blimk;

import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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

public class ImageResultAdapter extends ArrayAdapter<MediaReply> {
	
	private final Context context;
	private List<MediaReply> values;
	private List<MediaReply> repliedList;
	private List<MediaReply> notRepliedList;
	private List<MediaReply> timedOut;

	public ImageResultAdapter(Context context, int resource, List<MediaReply> values, List<MediaReply> repliedList, List<MediaReply> notRepliedList,
								List<MediaReply> timedOut) {
		super(context, resource, values);
		this.context = context;
	    this.values = values;
	    this.repliedList = repliedList;
	    this.notRepliedList = notRepliedList;
	    this.timedOut = timedOut;
	}
	
	public void resetLists(List<MediaReply> values, List<MediaReply> repliedList, List<MediaReply> notRepliedList, List<MediaReply> timedOut) {
		this.values = values;
	    this.repliedList = repliedList;
	    this.notRepliedList = notRepliedList;
	    this.timedOut = timedOut;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View rowView = null;
		TextView separatorView = null;
		MediaReply mediaReply = values.get(position);
		//check if its the first of the answered list, if so: its the start of a section
		if(mediaReply.getAnswer() != null && repliedList.size() > 0) {
			if(mediaReply.getId() == repliedList.get(0).getId()) {
				rowView = inflater.inflate(R.layout.replies_row_section, parent, false);
				separatorView = (TextView) rowView.findViewById(R.id.replies_row_separator);
				separatorView.setText("Replied");
			}
			else {
				rowView = inflater.inflate(R.layout.replies_row, parent, false);
			}
		}
		else if(mediaReply.getAnswer() != null && timedOut.size() > 0) {
			if(mediaReply.getId() == timedOut.get(0).getId()) {
				rowView = inflater.inflate(R.layout.replies_row_section, parent, false);
				separatorView = (TextView) rowView.findViewById(R.id.replies_row_separator);
				separatorView.setText("Timed Out");
			}
			else {
				rowView = inflater.inflate(R.layout.replies_row, parent, false);
			}
		}
		//check if its the first of the unanswered list, if so: its the start of a section
		else if(mediaReply.getAnswer() == null && notRepliedList.size() > 0) { 
			if(mediaReply.getId() == notRepliedList.get(0).getId()) {
				rowView = inflater.inflate(R.layout.replies_row_section, parent, false);
				separatorView = (TextView) rowView.findViewById(R.id.replies_row_separator);
				separatorView.setText("Not replied");
			}
			else {
				rowView = inflater.inflate(R.layout.replies_row, parent, false);
			}
		}
		else {  
			rowView = inflater.inflate(R.layout.replies_row, parent, false);
		}	

		TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
		ImageView iconView = (ImageView) rowView.findViewById(R.id.icon);
		
		if(mediaReply.getAnswer() == null) {
			iconView.setImageResource(R.drawable.clock);
		}
		else if(mediaReply.getAnswer().equals("x")) {
			iconView.setImageResource(R.drawable.alarm_clock);
			if(mediaReply.getReadStatus().equals("unread")) {
				textView.setTypeface(null, Typeface.BOLD); // Typeface.NORMAL, Typeface.ITALIC etc.
				declareAnswerRead(mediaReply);
			}
		}
		else if(mediaReply.getAnswer().equals("y")) {
			iconView.setImageResource(R.drawable.thumbs_up);
			if(mediaReply.getReadStatus().equals("unread")) {
				textView.setTypeface(null, Typeface.BOLD); // Typeface.NORMAL, Typeface.ITALIC etc.
				declareAnswerRead(mediaReply);
			}
		}
		else if(mediaReply.getAnswer().equals("n")) {
			iconView.setImageResource(R.drawable.thumbs_down);
			if(mediaReply.getReadStatus().equals("unread")) {
				textView.setTypeface(null, Typeface.BOLD); // Typeface.NORMAL, Typeface.ITALIC etc.
				declareAnswerRead(mediaReply);
			}
		}  

	    textView.setText(getContactName(mediaReply.getPhoneNumber()));

	    return rowView;
	}
	
	private String getContactName(String receivedNumber) {
		String result = null;
		String number = null;
		Cursor data = context.getContentResolver()
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
	
	private void declareAnswerRead(MediaReply mediaReply) {
		MainDataSource datasource = new MainDataSource(context);
	    datasource.open();
	    datasource.updateMediaContact(Integer.toString((int)mediaReply.getSenderLocalId()), mediaReply.getPhoneNumber(), "read", mediaReply.getAnswer());
	    datasource.close();
	}
	
	

}
