package com.mjac.socialbackup.test;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class SymmetricKeygenTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			byte[] N = Hex.decode("cafebabefacedbaddecaf888");
			Security.addProvider(new BouncyCastleProvider());
			KeyGenerator kgen = KeyGenerator.getInstance("AES", "BC");
			kgen.init(128);//256bytes
			SecretKey key = kgen.generateKey();
			
			Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(N));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
