package com.mjac.socialbackup.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Group;

@SuppressWarnings("serial")
public class KeystoreDialog extends JDialog {
	protected JTextField fileText = new JTextField();
	protected JPasswordField passChars = new JPasswordField();

	JButton cancelButton;

	boolean updated = false;

	public KeystoreDialog(File keystore) {
		super((JFrame) null, "Keystore configuration");

		createGuiElements();

		fileText.setText(keystore.getAbsolutePath());
	}

	public boolean updated() {
		return updated;
	}

	public String keystoreLocation() {
		return fileText.getText();
	}

	public char[] keystorePassword() {
		return passChars.getPassword();
	}

	private void createGuiElements() {
		GroupLayout layout = new GroupLayout(this.getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		JLabel keystoreLabel = new JLabel("Keystore");
		JLabel passwordLabel = new JLabel("Password");

		JButton openFile = new JButton("Open File");
		final Component parentRef = this;
		openFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(new File(fileText
						.getText()));
				chooser.setName("Choose keystore location");
				int returnVal = chooser.showOpenDialog(parentRef);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					fileText.setText(chooser.getSelectedFile()
							.getAbsolutePath());

				}
			}
		});

		JButton changeButton = new JButton("Change");
		changeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updated = true;
				dispose();
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
						layout.createParallelGroup()
								.addComponent(keystoreLabel).addComponent(
										passwordLabel)).addGroup(
						layout.createParallelGroup().addGroup(
								layout.createSequentialGroup().addComponent(
										fileText, 50, 200, 200).addComponent(
										openFile)).addComponent(passChars)))
				.addGroup(
						layout.createSequentialGroup().addGroup(
								layout.createSequentialGroup().addComponent(
										changeButton)
										.addComponent(cancelButton)));
		layout.setHorizontalGroup(hg);

		Group vg = layout.createSequentialGroup().addGroup(
				layout.createBaselineGroup(true, true).addComponent(
						keystoreLabel).addComponent(fileText).addComponent(
						openFile)).addGroup(
				layout.createBaselineGroup(true, true).addComponent(
						passwordLabel).addComponent(passChars)).addGroup(
				layout.createBaselineGroup(true, true).addComponent(
						changeButton).addComponent(cancelButton));
		layout.setVerticalGroup(vg);

		getRootPane().setDefaultButton(changeButton);

		setLayout(layout);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}
}
