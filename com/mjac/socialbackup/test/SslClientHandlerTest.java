package com.mjac.socialbackup.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

public class SslClientHandlerTest extends Thread {
	private static final Logger logger = Logger.getLogger(SslClientHandlerTest.class);
	SSLSocket s;

	public SslClientHandlerTest(SSLSocket s) {
		this.s = s;
	}

	public void run() {
		logger.debug("Client handler started");
		logger.debug(s);

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(s
					.getInputStream()));
			String inLine;
			while (s.isConnected() && (inLine = in.readLine()) != "exit") {
				logger.debug(inLine);
			}
		} catch (IOException e) {
			logger.debug("Client had an issue, exiting", e);
		}
	}
}
