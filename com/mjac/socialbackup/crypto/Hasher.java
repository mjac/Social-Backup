package com.mjac.socialbackup.crypto;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class Hasher {
	static enum hashTypes {
		sha1, none, crc32
	};

	static hashTypes defaultHashType = hashTypes.crc32;

	static public byte[] sha1InputStream1(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		MessageDigest hash = MessageDigest.getInstance("SHA1");

		DigestInputStream digestInputStream = new DigestInputStream(input, hash);
		while (digestInputStream.read() >= 0)
			;

		return hash.digest();
	}

	static public byte[] sha1InputStream2(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		BufferedInputStream bis = new BufferedInputStream(input);
		MessageDigest hash = MessageDigest.getInstance("SHA1");

		DigestInputStream digestInputStream = new DigestInputStream(bis, hash);
		while (digestInputStream.read() >= 0)
			;

		return hash.digest();
	}

	static public byte[] crcInputStream1(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		CRC32 crc = new CRC32();
		int byteIn;
		while ((byteIn = input.read()) >= 0)
			crc.update(byteIn);

		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(crc.getValue());
		return b;
	}

	static public byte[] crcInputStream2(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		CheckedInputStream cis = new CheckedInputStream(input, new CRC32());

		byte[] buf2 = new byte[128];
		while (cis.read(buf2) >= 0)
			;

		long checksum = cis.getChecksum().getValue();

		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(checksum);
		cis.close();
		return b;
	}

	static public byte[] crcInputStream3(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		BufferedInputStream bis = new BufferedInputStream(input); // 8kb chunks
																	// !!!
		CheckedInputStream cis = new CheckedInputStream(bis, new CRC32());

		byte[] buf2 = new byte[128];
		while (cis.read(buf2) >= 0)
			;

		long checksum = cis.getChecksum().getValue();

		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(checksum);
		cis.close();
		bis.close();
		return b;
	}

	static public byte[] crcInputStream4(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		CheckedInputStream cis = new CheckedInputStream(input, new CRC32());

		byte[] buf2 = new byte[1 << 13];
		while (cis.read(buf2) >= 0);
		long checksum = cis.getChecksum().getValue();
		cis.close();

		byte b[] = new byte[8];
		ByteBuffer buf = ByteBuffer.wrap(b);
		buf.putLong(checksum);

		return b;
	}

	static public byte[] hash(hashTypes type, InputStream input)
			throws NoSuchAlgorithmException, IOException {
		if (type == hashTypes.sha1) {
			return sha1InputStream2(input);
		} else if (type == hashTypes.crc32) {
			return crcInputStream4(input);
		} else {
			return null;
		}
	}

	static public byte[] hash(InputStream input)
			throws NoSuchAlgorithmException, IOException {
		return hash(defaultHashType, input);
	}
}
