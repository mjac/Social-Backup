package com.mjac.socialbackup.crypto;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;

/** Bouncy Castle cryptography generators. */
public class BouncyCastleGenerator {

	public static SecretKey aesKey() throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyGenerator kgen = KeyGenerator.getInstance("AES", "BC");
		kgen.init(128);//128 bytes, not bits as stated in docs.
		return kgen.generateKey();
	}

	public static KeyPair rsaPair() throws NoSuchAlgorithmException,
			NoSuchProviderException {
		// RSA 128 bits
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
		kpGen.initialize(1024, new SecureRandom()); // is this 1024 bits or bytes?
		return kpGen.generateKeyPair();
	}

	// // New way of doing it... Not documented...
	// public static void generateV3Certificate2(KeyPair pair) {
	// X509v3CertificateBuilder certGen = new X509v3CertificateBuilder(
	// new X500Name("Social Backup"), BigInteger.valueOf(System
	// .currentTimeMillis()), new Date(System
	// .currentTimeMillis() - 10000), new Date(System
	// .currentTimeMillis() + 10000), new X500Name(
	// "Social Backup"), null);
	// }

	// http://www.bouncycastle.org/wiki/display/JA1/BC+Version+2+APIs
	public static X509Certificate generateV3Certificate(KeyPair pair)
			throws InvalidKeyException, NoSuchProviderException,
			SignatureException {

		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		X500Principal selfPrincipal = new X500Principal("UID=mjac");
		certGen.setIssuerDN(selfPrincipal);
		certGen.setSubjectDN(selfPrincipal);
		// The subject field identifies the entity associated with the public
		// key stored in the subject public key field. The subject name may be
		// carried in the subject field and/or the subjectAltName extension. If
		// the subject is a CA (e.g., the basic constraints extension, as
		// discussed in 4.2.1.10, is present and the value of cA is TRUE,) then
		// the subject field MUST be populated with a non-empty distinguished
		// name matching the contents of the issuer field (see sec. 4.1.2.4) in
		// all certificates issued by the subject CA. If subject naming
		// information is present only in the subjectAltName extension (e.g., a
		// key bound only to an email address or URI), then the subject name
		// MUST be an empty sequence and the subjectAltName extension MUST be
		// critical.
		// http://java.sun.com/javame/reference/apis/jsr219/javax/security/auth/x500/X500Principal.html

		Calendar date = Calendar.getInstance();
		certGen.setNotBefore(date.getTime());
		date.add(Calendar.YEAR, 100); // Certificate lasts 100 years...
		certGen.setNotAfter(date.getTime());

		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		// certGen.addExtension(X509Extensions.BasicConstraints, true,
		// new BasicConstraints(false));
		// certGen.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(
		// KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
		// certGen.addExtension(X509Extensions.ExtendedKeyUsage, true,
		// new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
		//
		// certGen.addExtension(X509Extensions.SubjectAlternativeName, false,
		// new GeneralNames(new GeneralName(GeneralName.rfc822Name,
		// "test@test.test")));

		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	public static X509Certificate generateCertificate(KeyPair pair, String uid)
			throws InvalidKeyException, NoSuchProviderException,
			SecurityException, SignatureException {
		X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		certGen.setIssuerDN(new X500Principal("O=Social Backup"));
		certGen.setSubjectDN(new X500Principal("UID=" + uid));

		Calendar date = Calendar.getInstance();
		certGen.setNotBefore(date.getTime());
		date.add(Calendar.YEAR, 100); // Certificate lasts 100 years...
		certGen.setNotAfter(date.getTime());

		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	public static X509Certificate generatePersonalCertificate(KeyPair pair,
			String alias, String email, String host, int port)
			throws InvalidKeyException, NoSuchProviderException,
			SecurityException, SignatureException {
		X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

		certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
		X500Principal selfPrincipal = new X500Principal("CN=" + alias + ",UID="
				+ email + ",L=" + host + ":" + port);
		certGen.setIssuerDN(selfPrincipal);
		certGen.setSubjectDN(selfPrincipal);

		Calendar date = Calendar.getInstance();
		certGen.setNotBefore(date.getTime());
		date.add(Calendar.YEAR, 100); // Certificate lasts 100 years...
		certGen.setNotAfter(date.getTime());

		certGen.setPublicKey(pair.getPublic());
		certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

		return certGen.generateX509Certificate(pair.getPrivate(), "BC");
	}

	public static void addSelfSigned(KeyStore ks, String name, char[] password)
			throws NoSuchProviderException, NoSuchAlgorithmException,
			InvalidKeyException, SignatureException, KeyStoreException {
		KeyPair kp = BouncyCastleGenerator.rsaPair();
		X509Certificate xCert = BouncyCastleGenerator.generateV3Certificate(kp);
		ks.setKeyEntry(name, kp.getPrivate(), password,
				new Certificate[] { xCert });
	}
}
