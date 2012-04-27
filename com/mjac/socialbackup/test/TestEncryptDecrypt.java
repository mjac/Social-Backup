package com.mjac.socialbackup.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;

//import org.junit.Test;

public class TestEncryptDecrypt {

	static public byte[] encryptDecrypt(byte[] input, CipherParameters key,
			int cipherMode) throws Exception {

		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] buf = cipherMode == Cipher.ENCRYPT_MODE ? new byte[127]
					: new byte[128];

			int blockSize = buf.length;
			AsymmetricBlockCipher cipher = new RSAEngine();

			if (cipherMode == Cipher.ENCRYPT_MODE) {
				cipher.init(true, key);
				for (int chunkPosition = 0; chunkPosition < input.length; chunkPosition += blockSize) {
					byte[] encText = null;
					if (input.length <= blockSize) {
						encText = cipher.processBlock(input, 0, input.length);
						bos.write(encText);
					} else {
						int len = bis.read(buf, 0, Math.min(blockSize,
								input.length - blockSize));
						encText = cipher.processBlock(buf, 0, len);
						bos.write(encText);
					}
				}
			} else {
				cipher.init(false, key);
				for (int chunkPosition = 0; chunkPosition < input.length; chunkPosition += blockSize) {
					byte[] encText = null;
					int len = bis.read(buf, 0, Math.min(blockSize, input.length
							- blockSize));
					encText = cipher.processBlock(buf, 0, len);
					bos.write(encText);
				}
			}

			bos.flush();
			return bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static AsymmetricCipherKeyPair generateKeys(int keySize) {
		RSAKeyPairGenerator r = new RSAKeyPairGenerator();
		r.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16),
				new SecureRandom(), keySize, 80));
		AsymmetricCipherKeyPair keys = r.generateKeyPair();
		return keys;
	}

	public static void main(String[] test) {
		testEncryptDecrypt();
	}

	// @Test
	static public void testEncryptDecrypt() {
		AsymmetricCipherKeyPair keyPair = generateKeys(1024);
		Random r = new Random();
		byte[] msgBytes = new byte[10000];
		r.nextBytes(msgBytes);

		byte[] encrypt, decrypt;
		try {
			encrypt = encryptDecrypt(msgBytes, keyPair.getPublic(),
					Cipher.ENCRYPT_MODE);
			decrypt = encryptDecrypt(encrypt, keyPair.getPrivate(),
					Cipher.DECRYPT_MODE);
			System.out.println(Arrays.equals(msgBytes, decrypt) ? "true"
					: "false");
			// assertTrue("Not equals!",Arrays.equals(msgBytes, decrypt));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}