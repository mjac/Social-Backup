package com.mjac.socialbackup.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mjac.socialbackup.crypto.BouncyCastleGenerator;

public class CertificateTest {
	static protected String getExtensionValue(X509Certificate xCert, String oid)
			throws IOException {
		String decoded = null;
		byte[] extensionValue = xCert.getExtensionValue(oid);

		if (extensionValue != null) {
			DERObject derObject = toDERObject(extensionValue);
			if (derObject instanceof DEROctetString) {
				DEROctetString derOctetString = (DEROctetString) derObject;

				derObject = toDERObject(derOctetString.getOctets());
				if (derObject instanceof DERUTF8String) {
					DERUTF8String s = DERUTF8String.getInstance(derObject);
					decoded = s.getString();
				}

			}
		}

		return decoded;
	}

	static private DERObject toDERObject(byte[] data) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(data);
		ASN1InputStream asnInputStream = new ASN1InputStream(inStream);
		return asnInputStream.readObject();
	}

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		KeyPair kp;
		try {
			kp = BouncyCastleGenerator.rsaPair();
			X509Certificate xCert = BouncyCastleGenerator.generateV3Certificate(kp);
			System.out.println(xCert.getIssuerX500Principal().getName());

			byte[] san = xCert
					.getExtensionValue(X509Extensions.SubjectAlternativeName
							.getId());
			if (san != null) {
				DEROctetString oct = (DEROctetString) (new ASN1InputStream(
						new ByteArrayInputStream(san)).readObject());
				ASN1Sequence asn1s = (ASN1Sequence) (new ASN1InputStream(oct
						.getOctets()).readObject());

				GeneralNames gns = new GeneralNames(asn1s);
				for (GeneralName gn : gns.getNames()) {
					System.out.println(gn.toString());
					System.out.println(gn.getName());
				}
			}

			return;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
