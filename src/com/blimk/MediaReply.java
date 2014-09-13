package com.blimk;

import java.sql.Timestamp;

public class MediaReply {
	
	private long id;
	private long senderLocalId;
	private String phoneNumber=null;
	private String answer=null;
	private String readStatus=null;
	private Timestamp updated_at;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getSenderLocalId() {
		return senderLocalId;
	}
	public void setSenderLocalId(long senderLocalId) {
		this.senderLocalId = senderLocalId;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getReadStatus() {
		return readStatus;
	}
	public void setReadStatus(String readStatus) {
		this.readStatus = readStatus;
	}
	public Timestamp getUpdated_at() {
		return updated_at;
	}
	public void setUpdated_at(Timestamp updated_at) {
		this.updated_at = updated_at;
	}
	
	

}
