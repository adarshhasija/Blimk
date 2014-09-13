package com.blimk;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public class Media {
	private long id;
	private String senderLocalId=null;
	private String senderNumber=null;
	private byte[] content;
	private String question=null;
	private String defaultAnswer=null;
	private Timestamp updated_at;
	private List<MediaReply> replies;
	
	Media() {
		id = -1;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getSenderLocalId() {
	    return senderLocalId;
	  }

	  public void setSenderLocalId(String senderLocalId) {
	    this.senderLocalId = senderLocalId;
	  }

	  public String getSenderNumber() {
		return senderNumber;
	}

	public void setSenderNumber(String senderNumber) {
		this.senderNumber = senderNumber;
	}

	public byte[] getContent() {
	    return content;
	  }

	  public void setContent(byte[] content) {
	    this.content = content;
	  }

	public Timestamp getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(Timestamp updated_at) {
		this.updated_at = updated_at;
	}

	public String getDefaultAnswer() {
		return defaultAnswer;
	}

	public void setDefaultAnswer(String defaultAnswer) {
		this.defaultAnswer = defaultAnswer;
	}
	

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public List<MediaReply> getReplies() {
		return replies;
	}

	public void setReplies(List<MediaReply> replies) {
		this.replies = replies;
	}
	  

	  // Will be used by the ArrayAdapter in the ListView
	/*  @Override
	  public String toString() {
	    return comment;
	  } */

}
