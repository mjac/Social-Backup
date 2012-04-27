package com.mjac.socialbackup.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

@Deprecated
public class PropertiesDialog extends JDialog {
	private static final Logger logger = Logger.getLogger(PropertiesDialog.class);

	JTextField aliasText = new JTextField();
	JTextField emailText = new JTextField();

	JTextField hostText = new JTextField();
	JTextField portText = new JTextField();

	JButton cancelButton;

	Properties resultConfig;
	Properties startConfig;

	public PropertiesDialog(Properties properties,
			AbstractCollection<String> configFormatErrors) {
		super((JFrame) null, "Personal settings");

		startConfig = properties;

		createGuiElements();
		fillGuiElements(properties, configFormatErrors);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	public Properties result() {
		return resultConfig;
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

		Group vg = layout.createSequentialGroup().addGroup(
				layout.createBaselineGroup(true, true).addComponent(aliasLabel)
						.addComponent(aliasText))
				.addComponent(aliasDescription).addGap(20).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								emailLabel).addComponent(emailText))
				.addComponent(emailDescription).addGap(20).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								hostLabel).addComponent(hostText))
				.addComponent(hostDescription).addGap(20).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								portLabel).addComponent(portText))
				.addComponent(portDescription).addGap(20).addGroup(
						layout.createBaselineGroup(true, true).addComponent(
								changeButton).addComponent(cancelButton));
		layout.setVerticalGroup(vg);

		getRootPane().setDefaultButton(changeButton);

		setLayout(layout);
		setResizable(false);
		pack();
		pack();
		setLocationRelativeTo(null);

		// logger.trace(getPreferredSize());
		// setSize(getPreferredSize());
	}

	private void fillGuiElements(Properties properties,
			AbstractCollection<String> errors) {
		try {
			aliasText.setText(properties.getProperty("alias"));
		} catch (NullPointerException npe) {
			logger.error("Missing alias in provided configuration", npe);
		}

		try {
			emailText.setText(properties.getProperty("email"));
		} catch (NullPointerException npe) {
			logger.error("Missing email in provided configuration", npe);
		}

		try {
			hostText.setText(properties.getProperty("host"));
		} catch (NullPointerException npe) {
			logger.error("Missing host in provided configuration", npe);
		}

		try {
			portText.setText(properties.getProperty("port"));
		} catch (NullPointerException npe) {
			logger.error("Missing port in provided configuration", npe);
		}
	}

	private void processInput() {
		Properties nextConfig = (Properties) startConfig.clone();
		nextConfig.setProperty("alias", aliasText.getText());
		nextConfig.setProperty("email", emailText.getText());
		nextConfig.setProperty("host", hostText.getText());
		nextConfig.setProperty("port", portText.getText());

		ArrayList<String> errors = new ArrayList<String>();//DaemonProperties.validate(nextConfig);

		if (errors.size() < 1) {
			resultConfig = nextConfig;
			dispose();
			return;
		}

		JOptionPane.showMessageDialog(this, "Please check the fields: "
				+ StringUtils.join(errors, ", "), "Configuration issues",
				JOptionPane.ERROR_MESSAGE);
	}
}
