package com.mjac.socialbackup.actors;

import java.net.InetSocketAddress;

import com.mjac.socialbackup.email.EmailAddress;
import com.mjac.socialbackup.msg.StatusMessage;

public class Peer {
	protected String alias = "Anonymous";
	protected String email = "";
	protected String host = "";
	protected int port = 0;

	/** Allocation or allocation request. */
	protected long allocation = 0L;

	protected void updateStatus(StatusMessage status) {
		host = status.getHost();
		port = status.getPort();

		alias = status.getAlias();
		email = status.getEmail();

		long newAllocation = status.getAllocation();
		if (newAllocation >= 0L) {
			allocation = newAllocation;
		}
	}
	
	public boolean setValidAlias(String newAlias)
	{
		if (validAlias(newAlias)) {
			alias = newAlias;
			return true;
		}
		return false;
	}
	
	public boolean setValidEmail(String newEmail)
	{
		if (validEmail(newEmail)) {
			email = newEmail;
			return true;
		}
		return false;
	}
	
	public boolean setValidHostPort(String newHost, int newPort)
	{
		if (validHostPort(newHost, newPort)) {
			host = newHost;
			port = newPort;
			return true;
		}
		return false;
	}

	public String getAlias() {
		return alias;
	}

	public String getEmail() {
		return email;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public long getAllocation() {
		return allocation;
	}

	/** Copy peer status from ssl status in initial creation (for instance). */
	public void copyStatus(Peer pb) {
		alias = pb.alias;
		email = pb.email;
		host = pb.host;
		port = pb.port;
		allocation = pb.allocation;
	}

	static public boolean validEmail(String email) {
		return new EmailAddress(email).isValid();
	}

	static public boolean validHostPort(String host, int port) {
		try {
			new InetSocketAddress(host, port);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	static public boolean validAlias(String alias) {
		return alias.matches("[a-z0-9A-Z]{1,12}(?: [a-z0-9A-Z]{1,12}){0,3}");
	}
}
