package com.mjac.socialbackup.gui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Group;
import javax.swing.event.ChangeEvent;

import com.mjac.socialbackup.ChangeDispatcher;
import com.mjac.socialbackup.state.RemotePeer;
import com.mjac.socialbackup.state.LocalPeer;

@SuppressWarnings("serial")
public class PeerDialog extends MutableDialog {
	private JTextField hostTextField;
	private JTextField portTextField;
	private JTextField emailTextField;
	private JTextField aliasTextField;
	private JTextField allocTextField;
	private boolean allocTextFieldChanged;

	protected LocalPeer servicePeer;
	protected ChangeDispatcher changeDispatcher;

	private RemotePeer client;

	private JTextField allocUsedTextField;
	private JTextField allocRemoteTextField;

	public PeerDialog(LocalPeer servicePeer,
			ChangeDispatcher changeDispatcher, RemotePeer newClient) {
		super((JFrame) null, "Edit friend");
		this.client = newClient;
		dialogSetup(servicePeer, changeDispatcher);
	}

	private void dialogSetup(LocalPeer servicePeer,
			ChangeDispatcher changeDispatcher) {
		this.servicePeer = servicePeer;
		this.changeDispatcher = changeDispatcher;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		createGui();
		updateGui();
	}

	@Override
	protected void createGui() {
		GroupLayout layout = new GroupLayout(this.getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel hostLabel = new JLabel("Host");
		hostTextField = new JTextField("");
		JTextArea hostDescription = new TextNote(
				"Your friend's internet address.");
		hostDescription.setRows(1);

		JLabel portLabel = new JLabel("Port");
		portTextField = new JTextField(LocalPeer.defaultPort);
		JTextArea portDescription = new TextNote("Your friend's internet port.");
		portDescription.setRows(1);

		JLabel aliasLabel = new JLabel("Alias");
		aliasTextField = new JTextField("");
		aliasTextField.setEditable(false);
		JTextArea aliasDescription = new TextNote(
				"The name used by your friend to identify themselves.");
		aliasDescription.setRows(1);

		JLabel emailLabel = new JLabel("Email");
		emailTextField = new JTextField("");
		emailTextField.setEditable(false);
		JTextArea emailDescription = new TextNote(
				"You use this to regain connection if you lose contact.");
		emailDescription.setRows(1);

		Font allocFont = new Font("Sanserif", Font.PLAIN, 12);

		JLabel allocLabel = new JLabel("Allocation");

		allocRemoteTextField = new JTextField("");
		allocRemoteTextField.setEditable(false);
		JLabel allocRemoteTextLabel = new JLabel("MB from friend");
		allocRemoteTextLabel.setFont(allocFont);

		allocTextField = new JTextField("");
		allocTextFieldChanged = false;
		allocTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				allocTextFieldChanged = true;
			}
		});
		JLabel allocTextLabel = new JLabel("MB for friend");
		allocTextLabel.setFont(allocFont);

		allocUsedTextField = new JTextField("");
		allocUsedTextField.setEditable(false);
		JLabel allocUsedTextLabel = new JLabel("MB used by friend");
		allocUsedTextLabel.setFont(allocFont);

		JTextArea allocDescription = new TextNote(
				"The storage you have allocated to your friend. If they exceed this they can no longer backup files, although any existing backups will be kept.");
		allocDescription.setRows(3);

		Group hgLabels = layout.createParallelGroup().addComponent(hostLabel)
				.addComponent(portLabel).addComponent(emailLabel).addComponent(
						aliasLabel).addComponent(allocLabel);
		Group hgFields = layout.createParallelGroup().addComponent(
				hostTextField, 50, 200, 200).addComponent(portTextField, 40,
				40, 40).addComponent(emailTextField, 50, 200, 200)
				.addComponent(aliasTextField, 50, 200, 200).addGroup(
						layout.createSequentialGroup().addComponent(
								allocUsedTextField, 40, 40, 40).addComponent(
								allocUsedTextLabel)).addGroup(
						layout.createSequentialGroup().addComponent(
								allocTextField, 40, 40, 40).addComponent(
								allocTextLabel));
		Group hg = layout.createParallelGroup().addGroup(
				layout.createSequentialGroup().addGroup(hgLabels).addGroup(
						hgFields)).addComponent(hostDescription).addComponent(
				portDescription).addComponent(aliasDescription).addComponent(
				emailDescription).addComponent(allocDescription);
		layout.setHorizontalGroup(hg);

		Group vg = layout.createSequentialGroup().addGroup(
				layout.createBaselineGroup(true, true).addComponent(hostLabel)
						.addComponent(hostTextField)).addComponent(
				hostDescription).addGroup(
				layout.createBaselineGroup(true, true).addComponent(portLabel)
						.addComponent(portTextField)).addComponent(
				portDescription).addGap(10).addGroup(
				layout.createBaselineGroup(true, true).addComponent(aliasLabel)
						.addComponent(aliasTextField)).addComponent(
				aliasDescription).addGap(10).addGroup(
				layout.createBaselineGroup(true, true).addComponent(emailLabel)
						.addComponent(emailTextField)).addComponent(
				emailDescription).addGap(10).addGroup(
				layout.createBaselineGroup(true, true).addComponent(allocLabel)
						.addComponent(allocTextField).addComponent(
								allocTextLabel)).addGroup(
				layout.createBaselineGroup(true, true).addComponent(
						allocUsedTextLabel).addComponent(allocUsedTextField))
				.addComponent(allocDescription);

		layout.setVerticalGroup(vg);

		setLayout(layout);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
	}

	@Override
	public void updateGui() {
		boolean handled = client.isHandled();

		hostTextField.setEditable(!handled);
		portTextField.setEditable(!handled);

		emailTextField.setText(client.getEmail());
		aliasTextField.setText(client.getAlias());
		hostTextField.setText(client.getHost());
		portTextField.setText(Integer.toString(client.getPort()));

		allocUsedTextField.setText(Long
				.toString(client.getChunkList().getSize() >> 20));

		// Only the user changes this

		if (!allocTextFieldChanged) {
			allocTextField.setText(Long.toString(client.getAllocation() >> 20));
		}
		allocRemoteTextField.setText(Long.toString(client.getAllocation() >> 20));
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		Object s = ce.getSource();
		if (s instanceof RemotePeer) {
			if (s == client) {
				updateGui();
			}
		}
	}

	@Override
	public boolean holdsObject(Object obj) {
		return client == obj;
	}

	@Override
	public Object getHeldObject() {
		return client;
	}
}
