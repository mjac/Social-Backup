package com.mjac.socialbackup.crypto;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OpenTrustManager implements X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return new java.security.cert.X509Certificate[0];
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
		// Same as checkServerTrusted in peer to peer
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
	}

	static public TrustManager[] trustAllCertificates() {
		return new TrustManager[] { new OpenTrustManager() };
	}
}
