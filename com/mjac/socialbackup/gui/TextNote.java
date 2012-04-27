package com.mjac.socialbackup.gui;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class TextNote extends JTextArea {
	public TextNote(String text) {
		super(text);
		setBackground(null);
		setEditable(false);
		setBorder(null);
		setLineWrap(true);
		setWrapStyleWord(true);
		setFont(new Font("Sanserif", Font.PLAIN, 10));
		setFocusable(false);
		setMaximumSize(new Dimension(1000, 100));
	}
}
