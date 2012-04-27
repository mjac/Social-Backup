package com.mjac.socialbackup.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import javax.crypto.SecretKey;
import javax.net.ssl.KeyManagerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;

public class KeystoreManager {
	private static final Logger logger = Logger
			.getLogger(KeystoreManager.class);

	KeyStore ks;
	File location;
	char[] password = new char[] {};

	public KeystoreManager(File location, char[] password)
			throws KeyStoreException {
		ks = KeyStore.getInstance("BKS");
		setLocation(location);
		setPassword(password);
	}

	public void setLocation(File location) {
		this.location = location;
	}

	public void setPassword(char[] password) {
		this.password = password;
	}

	public KeyStore blank() throws NoSuchAlgorithmException,
			CertificateException, IOException {
		ks.load(null, password);
		return ks;
	}

	public KeyStore keystore() {
		return ks;
	}

	public void store() throws IOException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException {
		FileOutputStream fos = new FileOutputStream(location);
		ks.store(fos, password);
		fos.close();
	}

	public KeyManagerFactory factory() throws NoSuchAlgorithmException,
			UnrecoverableKeyException, KeyStoreException {
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, password);
		return kmf;
	}

	public void load() throws NoSuchAlgorithmException, CertificateException,
			IOException {
		FileInputStream fStream = new FileInputStream(location);
		ks.load(fStream, password);
		fStream.close();

		// KeyStore newStore = createKeys();
		// BouncyKeyGen.addSelfSigned(newStore, "", null);

		// createKeyFactory(newStore);
	}

	public String getRawId(Key key) {
		String id = getId(key);
		if (id == null) {
			return null;
		}

		return id.substring(0, id.indexOf('-'));
	}

	public String getId(Key key) {
		try {
			Enumeration<String> idEnum = ks.aliases();
			for (; idEnum.hasMoreElements();) {
				String id = (String) idEnum.nextElement();

				Key keyCheck = ks.getKey(id, password);
				if (keyCheck.equals(key)) {
					return id;
				}

				Certificate certCheckCertificate = ks.getCertificate(id);
				if (certCheckCertificate != null
						&& certCheckCertificate.getPublicKey().equals(key)) {
					return id;
				}
			}
		} catch (Exception e) {
			logger.warn("Error getting id", e);
		}
		return null;
	}

	public boolean updateCert(String alias, String uid) {
		String id = aliasForType(alias, "private");
		try {
			PrivateKey privateKey = null;
			Key key = ks.getKey(id, password);

			if (key instanceof PrivateKey) {
				privateKey = (PrivateKey) key;
			} else {
				throw new Exception("Key for ID " + id
						+ " is not a private key");
			}

			PublicKey publicKey = ks.getCertificate(id).getPublicKey();

			Key currentKey = ks.getKey(id, password);
			Certificate newCert = BouncyCastleGenerator.generateCertificate(
					new KeyPair(publicKey, privateKey), uid);

			ks.setKeyEntry(id, currentKey, password,
					new Certificate[] { newCert });

			return true;
		} catch (Exception e) {
			logger.warn("Could not update private key certificate chain for "
					+ id, e);
			return false;
		}
	}

	// public void keystoreGui()
	// {
	// // Password incorrect or keystore not found
	// KeystoreDialog kd = new KeystoreDialog(configKeystore, e);
	// kd.setVisible(true);
	// KeystoreDialog.resultTypes result = kd.getResult();
	//
	// if (!(result == KeystoreDialog.resultTypes.tryagain || result ==
	// KeystoreDialog.resultTypes.create)) {
	// break;
	// }
	//
	// String ksFile = kd.getFile();
	// config.setProperty("keystore", ksFile);
	// saveConfig();
	//
	// ksPass = kd.getPassword();
	//
	// if (result == KeystoreDialog.resultTypes.tryagain) {
	// continue;
	// }
	// }

	// public KeyStore createKeystore()
	// {
	//
	// // Create the keystore.
	// try {
	// KeyStore ksNew = createKeys();
	// BouncyKeyGen.addSelfSigned(ksNew, "", null);
	// FileOutputStream fos = new FileOutputStream(ksFile);
	// ksNew.store(fos, ksPass);
	// fos.close();
	// return ksNew;
	// } catch (Exception e2) {
	// JOptionPane.showMessageDialog(null, e2.getMessage(),
	// "Error creating keystore",
	// JOptionPane.ERROR_MESSAGE);
	// logger.error("Keystore creation failed", e2);
	// }
	// }

	public Key getKey(String alias) throws GeneralSecurityException {
		return ks.getKey(alias, password);
	}

	public PrivateKey getPrivateKey(String alias)
			throws GeneralSecurityException {
		Key privateKey = getKey(aliasForType(alias, "private"));
		if (privateKey == null) {
			return null;
		} else if (privateKey instanceof PrivateKey) {
			return (PrivateKey) privateKey;
		}
		throw new GeneralSecurityException("Key corresponding to " + alias
				+ " is not a private key");
	}

	public PublicKey getPublicKey(String alias) throws GeneralSecurityException {
		Key publicKey = getKey(aliasForType(alias, "public"));
		if (publicKey == null) {
			return null;
		} else if (publicKey instanceof PublicKey) {
			return (PublicKey) publicKey;
		}
		throw new GeneralSecurityException("Key corresponding to " + alias
				+ " is not a public key");
	}

	public SecretKey getSecretKey(String alias) throws GeneralSecurityException {
		Key secretKey = getKey(aliasForType(alias, "secret"));
		if (secretKey == null) {
			return null;
		} else if (secretKey instanceof SecretKey) {
			return (SecretKey) secretKey;
		}
		throw new GeneralSecurityException("Key corresponding to " + alias
				+ " is not a secret key");
	}

	public void setKey(String alias, PrivateKey privateKey,
			Certificate[] certList) throws KeyStoreException {
		ks.setKeyEntry(aliasForType(alias, "private"), privateKey, password,
				certList);
	}

	public void setKey(String alias, PublicKey publicKey)
			throws KeyStoreException {
		ks
				.setKeyEntry(aliasForType(alias, "public"), publicKey,
						password, null);
	}

	public void setKey(String alias, SecretKey secretKey)
			throws KeyStoreException {
		ks
				.setKeyEntry(aliasForType(alias, "secret"), secretKey,
						password, null);
	}

	/** Assumes - not in domain of alias or type. */
	static private String aliasForType(String alias, String type) {
		return alias + "-" + type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Keystore in location " + location + " with password "
				+ password.toString() + "\n");

		try {
			Enumeration<String> idEnum = ks.aliases();
			for (; idEnum.hasMoreElements();) {
				String id = (String) idEnum.nextElement();
				sb.append("\tAlias #");
				sb.append(id);
				sb.append("\n");

				Key key = ks.getKey(id, password);
				if (key == null) {
					sb.append("\t\tkey not found\n");
				} else {
					sb.append("\t\tkey ");
					sb.append(key.hashCode());
					sb.append(" ");
					sb.append(key.getAlgorithm());
					sb.append(" ");
					sb.append(key.getFormat());
					sb.append(" ");
					sb.append(key.getEncoded().length);
					if (key instanceof PublicKey) {
						sb.append(" public");
					} else if (key instanceof SecretKey) {
						sb.append(" secret");
					} else {
						sb.append(" private");
					}
					sb.append("\n");
				}

				Certificate cert = ks.getCertificate(id);
				if (cert != null) {
					sb.append("\t\tcertificate ");
					sb.append(cert.hashCode());
					sb.append(" with public key ");
					sb.append(cert.getPublicKey().hashCode());
					sb.append("\n");
				}
			}
		} catch (Exception e) {
			return "Issue turning keystore into string " + e + "\n";
		}

		return sb.toString();
	}
}
