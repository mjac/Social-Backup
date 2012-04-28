package com.mjac.socialbackup.msg;

import com.mjac.socialbackup.actors.Peer;

/** Hints at properties of the peer, without authority. */
public class StatusMessage extends Message {
	private static final long serialVersionUID = 1L;

	public String alias;
	public String email;
	public String host;
	public int port;

	/** Allocation request or actual allocation if peer is known. */
	public long allocation;

	public StatusMessage(String alias, String email, String host, int port,
			long allocation) {
		this.alias = alias;
		this.email = email;
		this.host = host;
		this.port = port;
		this.allocation = allocation;
	}

	public StatusMessage(Peer localPeer) {
		this.alias = localPeer.getAlias();
		this.email = localPeer.getEmail();
		this.host = localPeer.getHost();
		this.port = localPeer.getPort();
		this.allocation = localPeer.getAllocation();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getAlias() {
		return alias;
	}

	public String getEmail() {
		return email;
	}

	public long getAllocation() {
		return allocation;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof StatusMessage;
	}
}
