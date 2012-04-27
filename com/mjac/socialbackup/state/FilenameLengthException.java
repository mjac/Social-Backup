package com.mjac.socialbackup.state;

@SuppressWarnings("serial")
public class FilenameLengthException extends Exception {
	public FilenameLengthException(char[] filename, char[] storage) {
		super("Filename length (" + filename.length
				+ " characters) is too long to store in " + storage.length
				+ " characters");
	}
}
