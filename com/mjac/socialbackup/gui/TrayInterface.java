package com.mjac.socialbackup.gui;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.Daemon;
import com.mjac.socialbackup.email.Emailer;
import com.mjac.socialbackup.services.SslConnection;
import com.mjac.socialbackup.services.SslServer;
import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.LocalPeer;
import com.mjac.socialbackup.state.RemotePeer;

public class TrayInterface extends TrayIcon implements ChangeListener {
	protected LocalPeer localPeer;
	protected ChangeDispatcher changeDispatcher;

	protected Menu connectionMenu = new Menu("Friend requests");
	protected Menu friendMenu = new Menu("Friends");

	public MutableDialogMap fileDialogs = new MutableDialogMap();
	public MutableDialogMap peerDialogs = new MutableDialogMap();
	public MutableDialogMap sslPeerDialogs = new MutableDialogMap();

	private MenuItem startService;

	private MenuItem stopService;

	final static int friendRequestDelay = 1000;

	public TrayInterface(LocalPeer user, ChangeDispatcher changeDispatcher) {
		super(loadingImage());

		this.localPeer = user;
		this.changeDispatcher = changeDispatcher;

		setPopupMenu(createPopupMenu());

		fillPeers(user.getPeers());

		setImageAutoSize(true);
		setToolTip("Social Backup");
		setTitles();
	}

	private void setTitles() {
		setToolTip("Social Backup - " + localPeer.getAlias());
	}

	private PopupMenu createPopupMenu() {
		PopupMenu popup = new PopupMenu();

		MenuItem invitePeer = new MenuItem("Invite by email...");
		invitePeer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String s = (String) JOptionPane.showInputDialog(null,
						"What is your friend's email address?",
						"Invite friend", JOptionPane.QUESTION_MESSAGE, null,
						null, "");
				if (!StringUtils.isEmpty(s)) {
					Emailer.invite(s, localPeer);
				}
			}
		});
		popup.add(invitePeer);

		popup.addSeparator();

		popup.add(addFilesItem());
		popup.add(browseFilesItem());

		popup.addSeparator();

		MenuItem addPeer = new MenuItem("Add friend...");
		addPeer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SslConnection sslConn = localPeer.createSslConnection();
				sslPeerDialogs.add(new SslConnectionDialog(localPeer,
						changeDispatcher, sslConn));
			}
		});
		popup.add(addPeer);

		MenuItem peerExampleActive = new MenuItem("active peer");
		peerExampleActive.setFont(PeerMenuItem.activeFont());
		peerExampleActive.setEnabled(false);
		friendMenu.add(peerExampleActive);

		MenuItem peerExampleInactive = new MenuItem("inactive peer");
		peerExampleInactive.setFont(PeerMenuItem.inactiveFont());
		peerExampleInactive.setEnabled(false);
		friendMenu.add(peerExampleInactive);

		friendMenu.addSeparator();

		popup.add(friendMenu);
		popup.add(connectionMenu);

		popup.addSeparator();

		popup.add(createServiceMenu());
		popup.add(createSettingsMenu());

		popup.addSeparator();

		MenuItem defaultItem = new MenuItem("Exit");
		defaultItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Exiting...");
				System.exit(0);
			}
		});
		popup.add(defaultItem);
		return popup;
	}

	private MenuItem browseFilesItem() {
		MenuItem browseMenuItem = new MenuItem("Browse files...");
		browseMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!fileDialogs.focus(localPeer)) {
					fileDialogs.add(new FileDialog(localPeer));
				}
			}
		});
		return browseMenuItem;
	}

	private MenuItem addFilesItem() {
		MenuItem addFiles = new MenuItem("Add files...");
		addFiles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser("Add files to your backup");
				
				// todo - backup directories or at least multiple files!

				chooser.setMultiSelectionEnabled(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	
				int returnVal = chooser.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File selection = chooser.getSelectedFile();
					if (selection != null) {
						localPeer.addBackup(new Backup(selection));
					}
				}
			}
		});
		return addFiles;
	}

	/** Simple interface to accept/reject clients. */
	private Menu createServiceMenu() {
		Menu serviceMenu = new Menu("Service");

		startService = new MenuItem("Start");
		startService.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				localPeer.startService();
			}
		});
		serviceMenu.add(startService);

		stopService = new MenuItem("Stop");
		stopService.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				localPeer.stopService();
			}
		});
		stopService.setEnabled(false);
		serviceMenu.add(stopService);

		return serviceMenu;
	}

	/** Provide user a selection of customisation. */
	private Menu createSettingsMenu() {
		Menu serviceMenu = new Menu("Settings");

		MenuItem networkSettings = new MenuItem("Service");
		networkSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Daemon.editRunningService(localPeer);
				changeDispatcher.stateChanged(new ChangeEvent(localPeer));
			}
		});
		serviceMenu.add(networkSettings);

		MenuItem securitySettings = new MenuItem("Backup");
		securitySettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		serviceMenu.add(securitySettings);

		return serviceMenu;
	}

	private MenuItem peerMenuItem(RemotePeer clientPeer) {
		PeerMenuItem newPeerMenuItem = new PeerMenuItem(clientPeer);

		final RemotePeer clientRef = clientPeer;

		newPeerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!peerDialogs.focus(clientRef)) {
					peerDialogs.add(new PeerDialog(localPeer,
							changeDispatcher, clientRef));
				}
			}
		});

		return newPeerMenuItem;
	}

	private void fillPeers(Collection<RemotePeer> collection) {
		for (RemotePeer peer : collection) {
			friendMenu.add(peerMenuItem(peer));
		}
	}

	private void updatePeers(RemotePeer changedPeer) {
		for (int i = friendMenu.getItemCount() - 1; i >= 0; --i) {
			MenuItem menuItem = friendMenu.getItem(i);

			if (!(menuItem instanceof PeerMenuItem)) {
				continue;
			}

			PeerMenuItem peerMenuItem = (PeerMenuItem) menuItem;
			if (peerMenuItem.getAssociatedPeer() == changedPeer) {
				peerMenuItem.update();
				return;
			}
		}

		friendMenu.add(peerMenuItem(changedPeer));
	}

	private void updateSslPeers(SslConnection changedSslPeer) {
		UnknownPeerMenuItem menuItem = null;

		for (int i = connectionMenu.getItemCount() - 1; i >= 0; --i) {
			UnknownPeerMenuItem newMenuItem = (UnknownPeerMenuItem) connectionMenu
					.getItem(i);
			if (newMenuItem.getAssociatedSslPeer() == changedSslPeer) {
				menuItem = newMenuItem;
				break;
			}
		}

		// Remove if not connected or already assigned.
		if (!changedSslPeer.isConnected() || changedSslPeer.hasAssociatedPeer()) {
			if (menuItem != null) {
				connectionMenu.remove(menuItem);
			}
			return;
		}

		if (menuItem == null) {
			menuItem = new UnknownPeerMenuItem(changedSslPeer);

			final SslConnection sslPeerRef = changedSslPeer;
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!sslPeerDialogs.focus(sslPeerRef)) {
						SslConnectionDialog connDialog = new SslConnectionDialog(
								localPeer, changeDispatcher, sslPeerRef);
						sslPeerDialogs.add(connDialog);
					}
				}
			});

			connectionMenu.add(menuItem);

			TimerTask showMessageDelayed = new TimerTask() {
				@Override
				public void run() {
					showConnectionMessage(sslPeerRef);
				}
			};

			Timer timer = new Timer();
			timer.schedule(showMessageDelayed, friendRequestDelay);
		}

		menuItem.update();
	}

	private void showConnectionMessage(SslConnection sslPeerRef) {
		String who = sslPeerRef.getAlias();

		if (!StringUtils.isEmpty(sslPeerRef.getEmail())) {
			who += "/" + sslPeerRef.getEmail();
		}

		who += " (connecting from "
				+ sslPeerRef.getCurrentAddress().getAddress().toString() + ")";

		displayMessage(
				"Friend request",
				who
						+ " would like to join your Social Backup network. If you would like to add them, open your friend request menu and click on their name.",
				MessageType.INFO);
	}

	private void displayServiceStatus(SslServer s) {
		boolean isRunning = s.isRunning();
		setImage(s.isRunning() ? activeImage() : inactiveImage());

		startService.setEnabled(!isRunning);
		stopService.setEnabled(isRunning);
	}

	static Image loadingImage() {
		return new ImageIcon(
				TrayInterface.class
						.getResource("/com/mjac/socialbackup/graphics/handshakeicon.png"))
				.getImage();
	}

	static Image activeImage() {
		return new ImageIcon(
				TrayInterface.class
						.getResource("/com/mjac/socialbackup/graphics/handshakeicongreen.png"))
				.getImage();
	}

	static Image inactiveImage() {
		return new ImageIcon(
				TrayInterface.class
						.getResource("/com/mjac/socialbackup/graphics/handshakeiconred.png"))
				.getImage();
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		final ChangeEvent ceRef = ce;
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingStateChanged(ceRef);
			}
		});
	}

	public void swingStateChanged(ChangeEvent ce) {
		Object s = ce.getSource();
		if (s instanceof SslServer) {
			displayServiceStatus((SslServer) s);
		} else if (s instanceof SslConnection) {
			updateSslPeers((SslConnection) s);
			sslPeerDialogs.stateChanged(ce);
		} else if (s instanceof LocalPeer) {
			if (s == localPeer) {
				setTitles();
			}
		} else if (s instanceof RemotePeer) {
			updatePeers((RemotePeer) s);
			peerDialogs.stateChanged(ce);
		}
	}
}
