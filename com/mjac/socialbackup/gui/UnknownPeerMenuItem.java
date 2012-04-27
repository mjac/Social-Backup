package com.mjac.socialbackup.gui;

import java.awt.MenuItem;

import com.mjac.socialbackup.services.SslConnection;

@SuppressWarnings("serial")
public class UnknownPeerMenuItem extends MenuItem {
	private SslConnection sslPeer;

	public UnknownPeerMenuItem(SslConnection sslPeer) {
		this.sslPeer = sslPeer;
	}

	public void update() {
		setLabel(sslPeer.getAlias() + " / " + sslPeer.getEmail());
	}

	public SslConnection getAssociatedSslPeer() {
		return sslPeer;
	}
}
