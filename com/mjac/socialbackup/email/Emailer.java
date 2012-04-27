package com.mjac.socialbackup.email;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.mjac.socialbackup.state.LocalPeer;

public class Emailer {
	private static final Logger logger = Logger.getLogger(Emailer.class);

	// /**
	// * @todo use URL everywhere...
	// */
	// public static void main(String[] args) {
	// BasicConfigurator.configure();
	// }

	public static boolean mail(URI uri) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			try {
				desktop.mail(uri);
				return true;
			} catch (IOException e) {
				logger.warn("Problem calling email client", e);
			}
		}
		return false;
	}

	public static boolean invite(String toEmail, LocalPeer user) {
		return mail(inviteUri(toEmail, user));
	}

	public static URI inviteUri(String toEmail, LocalPeer user) {
		return inviteUri(toEmail, user.getAlias(), user.getEmail(),
				new InetSocketAddress(user.getHost(), user.getPort()), null);
	}

	static public URI inviteUri(String toEmail, String fromId,
			String fromEmail, InetSocketAddress fromAddr, String fromInvite) {
		try {
			return new URI("mailto:"
					+ encode(toEmail)
					+ "?"
					+ encodePart("from", fromId)
					+ "&"
					+ encodePart("subject", "Social Backup invite")
					+ "&"
					+ encodePart("body", body(fromId, fromEmail, fromAddr,
							fromInvite)));
		} catch (Exception e) {
			logger.error("Could not compose invite", e);
			return null;
		}
	}

	static private String body(String fromId, String fromEmail,
			InetSocketAddress fromAddr, String fromInvite) {
		return fromId
				+ " ("
				+ fromEmail
				+ ") would like you to use Social Backup.\nYou can download Social Backup from http://socialbackup.com/\n\n"
				+ "To add "
				+ fromId
				+ ", right click the Social Backup icon and select \"Add friend...\".\n"
				+ "In the window that pops up, enter the following details:\n"
				+ "\nHost: " + fromAddr.getHostName() + "\nPort: "
				+ fromAddr.getPort()
				+ (fromInvite == null ? "" : "\nInvite code: " + fromInvite);
	}

	static private String encodePart(String part, String value)
			throws UnsupportedEncodingException {
		return encode(part) + "=" + encode(value);
	}

	static private String encode(String str)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
	}
}
