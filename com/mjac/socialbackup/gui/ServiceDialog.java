package com.mjac.socialbackup.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Group;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.mjac.socialbackup.actors.LocalUser;

@SuppressWarnings("serial")
public class ServiceDialog extends JDialog {
	private static final Logger logger = Logger.getLogger(ServiceDialog.class);

	JTextField aliasText = new JTextField();
	JTextField emailText = new JTextField();

	JTextField hostText = new JTextField();
	JTextField portText = new JTextField();

	String hostOriginal;
	int portOriginal;
	String aliasOriginal;
	String emailOriginal;

	JButton cancelButton;
	LocalUser servicePeer;
	boolean changed = false;

	public ServiceDialog(LocalUser newService) {
		super((JFrame) null, "Service settings");

		servicePeer = newService;

		aliasOriginal = servicePeer.getAlias();
		emailOriginal = servicePeer.getEmail();
		hostOriginal = servicePeer.getHost();
		portOriginal = servicePeer.getPort();

		createGuiElements();
		fillGuiElements();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		pack();
		setLocationRelativeTo(null);
	}

	public void createGuiElements() {
		GroupLayout layout = new GroupLayout(this.getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel aliasLabel = new JLabel("Alias");
		JTextArea aliasDescription = new TextNote(
				"Your identifier to your friends. Composed of four to twelve letters (A-Z) and numbers (0-9). The more unique, the better.");
		aliasDescription.setRows(3);

		JLabel emailLabel = new JLabel("Email");
		JTextArea emailDescription = new TextNote(
				"Your email address. Friends use this to connect to you if they lose contact.");
		emailDescription.setRows(2);

		JLabel hostLabel = new JLabel("Host");
		JTextArea hostDescription = new TextNote(
				"The host is the internet address used by your friends to access your computer. Leave it blank if you are not behind a router.");
		hostDescription.setRows(3);

		JLabel portLabel = new JLabel("Port");
		JTextArea portDescription = new TextNote(
				"Leave this as default unless you are setting up port forwarding.");
		portDescription.setRows(2);

		JButton changeButton = new JButton("Change");
		changeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				processInput();
			}
		});

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				restore();
				dispose();
			}
		});

		Group hg = layout.createParallelGroup().addGroup(
				layout.createSequentialGroup().addGroup(
						layout.createParallelGroup().addComponent(aliasLabel)
								.addComponent(emailLabel).addComponent(
										hostLabel).addComponent(portLabel))
						.addGroup(
								layout.createParallelGroup().addComponent(
										aliasText, 50, 200, 200).addComponent(
										emailText, 50, 200, 200).addComponent(
										hostText, 50, 200, 200).addComponent(
										portText, 40, 40, 40))).addGroup(
				layout.createSequentialGroup().addComponent(changeButton)
						.addComponent(cancelButton)).addComponent(
				hostDescription).addComponent(aliasDescription).addComponent(
				emailDescription).addComponent(portDescription);
		layout.setHorizontalGroup(hg);

		int textSize = aliasText.getFont().getSize();

		Group vg = layout.createSequentialGroup().addGroup(
				layout.createBaselineGroup(true, true).addComponent(aliasLabel)
						.addComponent(aliasText))
				.addComponent(aliasDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								emailLabel).addComponent(emailText))
				.addComponent(emailDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								hostLabel).addComponent(hostText))
				.addComponent(hostDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								portLabel).addComponent(portText))
				.addComponent(portDescription).addGap(textSize).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								changeButton).addComponent(cancelButton));
		layout.setVerticalGroup(vg);

		getRootPane().setDefaultButton(changeButton);

		setLayout(layout);
	}

	private void fillGuiElements() {
		try {
			aliasText.setText(servicePeer.getAlias());
		} catch (NullPointerException npe) {
			logger.error("Missing alias in provided configuration", npe);
		}

		try {
			emailText.setText(servicePeer.getEmail());
		} catch (NullPointerException npe) {
			logger.error("Missing email in provided configuration", npe);
		}

		try {
			hostText.setText(servicePeer.getHost());
		} catch (NullPointerException npe) {
			logger.error("Missing host in provided configuration", npe);
		}

		try {
			portText.setText(Integer.toString(servicePeer.getPort()));
		} catch (NullPointerException npe) {
			logger.error("Missing port in provided configuration", npe);
		}
	}

	private void processInput() {
		ArrayList<String> errors = new ArrayList<String>();

		String aliasStr = aliasText.getText();
		if (!servicePeer.setValidAlias(aliasStr)) {
			errors.add("That alias is invalid.");
		}

		String emailStr = emailText.getText();
		if (!servicePeer.setValidEmail(emailStr)) {
			errors.add("That email is invalid.");
		}

		String hostStr = hostText.getText();
		String portStr = portText.getText();
		try {
			int portInt = Integer.parseInt(portStr);
			if (!servicePeer.setValidHostPort(hostStr, portInt)) {
				errors.add("Invalid host/port combination.");
			}
		} catch (NumberFormatException nfe) {
			errors.add("Invalid port number.");
		}

		if (errors.size() < 1) {
			dispose();
			return;
		}

		JOptionPane.showMessageDialog(this, "Please check these issues:\n - "
				+ StringUtils.join(errors, "\n - "), "Configuration issues",
				JOptionPane.ERROR_MESSAGE);
	}

	private void restore() {
		servicePeer.setValidAlias(aliasOriginal);
		servicePeer.setValidEmail(emailOriginal);
		servicePeer.setValidHostPort(hostOriginal, portOriginal);
	}

	public boolean changed() {
		return !(servicePeer.getAlias().equals(aliasOriginal)
				&& servicePeer.getEmail().equals(emailOriginal)
				&& servicePeer.getHost().equals(hostOriginal) && servicePeer
				.getPort() == portOriginal);
	}
}
