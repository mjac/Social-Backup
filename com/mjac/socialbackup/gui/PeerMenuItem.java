package com.mjac.socialbackup.gui;

import java.awt.Font;
import java.awt.MenuItem;

import com.mjac.socialbackup.state.RemotePeer;

@SuppressWarnings("serial")
public class PeerMenuItem extends MenuItem {
	private RemotePeer peer;

	public PeerMenuItem(RemotePeer peer) {
		this.peer = peer;
		update();
	}

	public RemotePeer getAssociatedPeer() {
		return peer;
	}

	public void update() {
		setLabel(peer.getAlias() + " / " + peer.getEmail());
		setFont(peer.isHandled() ? activeFont() : inactiveFont());
	}

	static public Font activeFont() {
		return new Font("Sanserif", Font.BOLD, 12);
	}

	static public Font inactiveFont() {
		return new Font("Sanserif", Font.PLAIN, 12);
	}
}
