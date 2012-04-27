package com.mjac.socialbackup.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Group;
import javax.swing.event.ChangeEvent;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.services.SslConnection;
import com.mjac.socialbackup.state.RemotePeer;
import com.mjac.socialbackup.state.LocalPeer;

/**
 * @todo the contained sslPeer can change... actually can it? Yes it can... from
 *       null to active... instead create a SSL peer for the window instead of
 *       using null?
 * @author mjac
 * 
 */
@SuppressWarnings("serial")
public class SslConnectionDialog extends MutableDialog {
	protected JTextField hostTextField;
	protected JTextField portTextField;
	protected JButton connectButton;
	protected JButton disconnectButton;
	protected JLabel statusLabel;
	protected JTextField emailTextField;
	protected JTextField aliasTextField;
	protected JTextField allocTextField;

	protected LocalPeer servicePeer;
	protected ChangeDispatcher changeDispatcher;

	protected SslConnection sslConn;

	protected AbstractButton createButton;

	public SslConnectionDialog(LocalPeer servicePeer,
			ChangeDispatcher changeDispatcher, SslConnection newPeer) {
		super((JFrame) null, "Add a friend");

		this.servicePeer = servicePeer;
		this.changeDispatcher = changeDispatcher;
		this.sslConn = newPeer;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		createGui();
		updateGui();

		pack();
		setLocationRelativeTo(null);
	}

	@Override
	protected void createGui() {
		GroupLayout layout = new GroupLayout(this.getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel hostLabel = new JLabel("Host");
		hostTextField = new JTextField("");
		JTextArea hostDescription = new TextNote(
				"The internet address used by your friend to access their computer.");
		hostDescription.setRows(2);

		JLabel portLabel = new JLabel("Port");
		portTextField = new JTextField(LocalPeer.defaultPort);
		JTextArea portDescription = new TextNote(
				"Leave this as default unless you are setting up port forwarding.");
		portDescription.setRows(2);

		final SslConnectionDialog thisRef = this;
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				boolean valid = false;
				try {
					valid = sslConn.setValidHostPort(hostTextField.getText(),
							Integer.parseInt(portTextField.getText()));
				} catch (NumberFormatException nfe) {
				}

				if (!valid) {
					JOptionPane
							.showMessageDialog(
									thisRef,
									"Please check your entries in the host/port fields.\nIt was not possible to connect.",
									"Invalid host/port",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				boolean connected = servicePeer.connectTo(sslConn);

				if (!connected) {
					JOptionPane
							.showMessageDialog(
									thisRef,
									"Could not connect to your friend. Retry or attempt to contact your friend through another medium.",
									"Connection failed",
									JOptionPane.ERROR_MESSAGE);
				}

				// updateGuiStates(); <- replace this with message
				// dissemination!!!!!!!!!!
			}
		});
		disconnectButton = new JButton("Disconnect");
		disconnectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sslConn.disconnect();
			}
		});
		statusLabel = new JLabel("");

		JLabel aliasLabel = new JLabel("Alias");
		aliasTextField = new JTextField("");
		JTextArea aliasDescription = new TextNote(
				"Your friend's identifier. Composed of four to twelve letters (A-Z) and numbers (0-9).");
		aliasDescription.setRows(2);

		JLabel emailLabel = new JLabel("Email");
		emailTextField = new JTextField("");
		JTextArea emailDescription = new TextNote(
				"Your friend's email address. You use this to regain connection if you lose contact.");
		emailDescription.setRows(2);

		JLabel allocLabel = new JLabel("Allocation");
		allocTextField = new JTextField("");
		JLabel allocTextLabel = new JLabel("MB");
		JTextArea allocDescription = new TextNote(
				"How much storage do you want to share with them?");
		allocDescription.setRows(1);

		createButton = new JButton("Create");
		createButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long alloc = -1L;
				try {
					alloc = Integer.parseInt(allocTextField.getText()) << 20L;
				} catch (NumberFormatException nfe) {
				}

				if (alloc < 0L) {
					JOptionPane
							.showMessageDialog(
									thisRef,
									"Invalid allocation amount, please check the number set.",
									"Friend add failed",
									JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (!servicePeer.createPeer(sslConn, alloc)) {
					JOptionPane.showMessageDialog(thisRef,
							"Friend could not be created", "Friend add failed",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				thisRef.dispose();
			}
		});

		Group hgLabels = layout.createParallelGroup().addComponent(hostLabel)
				.addComponent(portLabel).addComponent(emailLabel).addComponent(
						aliasLabel).addComponent(allocLabel);
		Group hgFinishButtons = layout.createSequentialGroup().addComponent(
				createButton).addComponent(cancelButton);
		Group hgConnButtons = layout.createSequentialGroup().addComponent(
				connectButton).addComponent(disconnectButton).addComponent(
				statusLabel);
		Group hgFields = layout.createParallelGroup().addComponent(
				hostTextField, 50, 200, 200).addComponent(portTextField, 40,
				40, 40).addComponent(emailTextField, 50, 200, 200)
				.addComponent(aliasTextField, 50, 200, 200).addGroup(
						layout.createSequentialGroup().addComponent(
								allocTextField, 40, 40, 40).addComponent(
								allocTextLabel));
		Group hg = layout.createParallelGroup().addGroup(
				layout.createSequentialGroup().addGroup(hgLabels).addGroup(
						hgFields)).addGroup(hgConnButtons).addGroup(
				hgFinishButtons).addComponent(hostDescription).addComponent(
				portDescription).addComponent(aliasDescription).addComponent(
				emailDescription).addComponent(allocDescription);
		layout.setHorizontalGroup(hg);

		int textSize = hostTextField.getFont().getSize();

		Group vg = layout.createSequentialGroup().addGroup(
				layout.createBaselineGroup(true, true).addComponent(hostLabel)
						.addComponent(hostTextField)).addComponent(
				hostDescription).addGap(textSize).addGroup(
				layout.createBaselineGroup(true, true).addComponent(portLabel)
						.addComponent(portTextField)).addComponent(
				portDescription).addGap(textSize).addGroup(
				layout.createBaselineGroup(true, true).addComponent(
						connectButton).addComponent(disconnectButton)
						.addComponent(statusLabel)).addGap(2 * textSize)
				.addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								aliasLabel).addComponent(aliasTextField))
				.addComponent(aliasDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								emailLabel).addComponent(emailTextField))
				.addComponent(emailDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								allocLabel).addComponent(allocTextField)
								.addComponent(allocTextLabel)).addComponent(
						allocDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								createButton).addComponent(cancelButton));

		layout.setVerticalGroup(vg);

		setLayout(layout);
	}

	/**
	 * @todo Go through this and don't do SSL peer edits.
	 */
	@Override
	public void updateGui() {
		boolean peerConnected = sslConn != null && sslConn.isConnected();

		connectButton.setEnabled(!peerConnected);
		disconnectButton.setEnabled(peerConnected);
		createButton.setEnabled(peerConnected);

		hostTextField.setEditable(!peerConnected);
		portTextField.setEditable(!peerConnected);

		aliasTextField.setEditable(false);
		emailTextField.setEditable(false);

		if (peerConnected) {
			// if (StringUtils.isEmpty(hostTextField.getText())) {
			// hostTextField.setText(sslPeer.getHost());
			// }
			//
			// if (StringUtils.isEmpty(portTextField.getText())) {
			// portTextField.setText(Integer.toString(sslPeer.getPort()));
			// }
			hostTextField.setText(sslConn.getHost());
			portTextField.setText(Integer.toString(sslConn.getPort()));

			aliasTextField.setText(sslConn.getAlias());
			emailTextField.setText(sslConn.getEmail());

			// Was StringUtils.isEmpty(allocTextField.getText())
			if (!allocTextField.isFocusOwner()) {
				long myAlloc = servicePeer.getAllocation();
				long theirAlloc = sslConn.getAllocation();
				allocTextField.setText(Long
						.toString(((myAlloc + theirAlloc) / 2L) >> 20L));
			}
		} else {
			// User does not set these, peer does.
			aliasTextField.setText("");
			emailTextField.setText("");
			allocTextField.setText("");
		}

		// Update button if Peer associated to public key, Create button
		// otherwise
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		final SslConnectionDialog thisRef = this;
		Object s = ce.getSource();
		if (!(s instanceof SslConnection && s == sslConn)) {
			return;
		}

		updateGui();

		if (sslConn.hasAssociatedPeer()) {
			RemotePeer newPeer = sslConn.getPeer();
			JOptionPane.showMessageDialog(thisRef, "You are now connected to "
					+ newPeer.getAlias() + ".\n"
					+ "The connection properties will now close.",
					"Already connected", JOptionPane.INFORMATION_MESSAGE);
			sslConn = null;
			dispose();
		}
	}

	@Override
	public boolean holdsObject(Object obj) {
		return sslConn == obj;
	}

	@Override
	public Object getHeldObject() {
		return sslConn;
	}
}
