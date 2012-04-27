package com.mjac.socialbackup.test;

import java.security.KeyPair;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mjac.socialbackup.crypto.BouncyCastleGenerator;
import com.mjac.socialbackup.crypto.OpenTrustManager;

public class SslServerTest {
	private static final Logger logger = Logger.getLogger(SslServerTest.class);
	static String keyStoreFile = "testserver.bks";

	/** Get from user input in future! */
	static char[] password = "temps".toCharArray();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();

		// System.setProperty("javax.net.debug", "all");

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
			ks.setKeyEntry("testserverkey", kp.getPrivate(), null,
					new Certificate[] { xCert });
			final KeyManagerFactory kmf = KeyManagerFactory
					.getInstance("SunX509");
			kmf.init(ks, null);

			SSLContext sslcontext = SSLContext.getInstance("TLS");

			sslcontext.init(kmf.getKeyManagers(), trustAllCerts,
					new SecureRandom());

			SSLServerSocketFactory ssf = sslcontext.getServerSocketFactory();

			logger.debug("Starting server on port 444");
			SSLServerSocket serversocket = (SSLServerSocket) ssf
					.createServerSocket(444);
			serversocket.setNeedClientAuth(true);
			serversocket.setWantClientAuth(true);

			while (true) {
				logger.debug("Accepting clients...");
				SSLSocket sslSoc = (SSLSocket) serversocket.accept();

				logger.debug("Client connected, creating handler thread");
				Thread sslThread = new SslClientHandlerTest(sslSoc);

				logger.debug("Giving client to handler");
				sslThread.start();
			}
		} catch (Exception e) {
			logger.debug("Server failed", e);
		}
	}
}
