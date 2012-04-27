package com.mjac.socialbackup.msg;

import java.io.Serializable;

import org.joda.time.DateTime;

abstract public class Message implements Serializable {
	private static final long serialVersionUID = 1L;

	transient private DateTime receivedTime;

	public void setReceivedTime() {
		receivedTime = new DateTime();
	}

	public DateTime getReceivedTime() {
		return receivedTime;
	}

	public boolean valid() {
		return true;
	}
}
