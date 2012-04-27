package com.mjac.socialbackup.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mjac.socialbackup.crypto.BouncyCastleGenerator;
import com.mjac.socialbackup.crypto.OpenTrustManager;

public class SslClientTest {
	private static final Logger logger = Logger.getLogger(SslClientTest.class);
	static String keyStoreFile = "testclient.bks";

	/** Get from user input in future! */
	static char[] password = "tempclient".toCharArray();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();

		//System.setProperty("javax.net.debug", "ssl");

		Security.addProvider(new BouncyCastleProvider());

		final TrustManager[] trustAllCerts = OpenTrustManager
				.trustAllCertificates();

		// FileInputStream fStream = new FileInputStream(keyStoreFile);
		// ks.load(fStream, password);
		// fStream.close();

		try {
			KeyStore ks = KeyStore.getInstance("BKS");
			ks.load(null, null);

			KeyPair kp = BouncyCastleGenerator.rsaPair();
			X509Certificate xCert = BouncyCastleGenerator.generateV3Certificate(kp);
			ks.setKeyEntry("testse", kp.getPrivate(), null,
					new Certificate[] { xCert });

			final KeyManagerFactory kmf = KeyManagerFactory
					.getInstance("SunX509");
			kmf.init(ks, null);

			SSLContext sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(kmf.getKeyManagers(), trustAllCerts,
					new SecureRandom());

			SSLSocket socket = (SSLSocket) sslcontext.getSocketFactory()
					.createSocket("localhost", 777);

			BufferedReader consoleInput = new BufferedReader(
					new InputStreamReader(System.in));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));

			logger.trace("Waiting on handshake");
			socket.startHandshake();

			logger.trace("Send data using console");
			while (true) {
				String inLine = consoleInput.readLine();
				bw.write(inLine + "\n");
				bw.flush();
				if (inLine == "exit") {
					break;
				}
			}
		} catch (Exception e) {
			logger.debug("Client failed", e);
		}
	}
}
