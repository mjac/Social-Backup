package com.mjac.socialbackup.state;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.joda.time.DateTime;

import com.mjac.socialbackup.Id;

public class ChunkData extends Chunk {
	private static final long serialVersionUID = 1L;

	static final public int filenameLimit = 255;

	/**
	 * Cannot let the size change due to filename length! Encrypt using chunk id
	 * as initialisation vector with private key
	 * 
	 * http://stackoverflow.com/questions/4512742/does-java-io-have-a-maximum-
	 * file-name-length 256 windows, unix
	 */
	protected char[] filename;

	/** Total size of complete backup. */
	protected long backupSize;

	// protected long crcChunk; // Not required as GCM provides authenticity
	protected byte[] backupHash;

	protected int index;
	protected int maxIndex;

	protected DateTime dateWritten;

	protected byte[] data;
	protected byte[] padding;

	public ChunkData(Id id, File file, int index, int maxIndex, DateTime written,
			long backupSize, byte[] hash) throws FilenameLengthException {
		super(id, 0);

		this.index = index;
		this.maxIndex = maxIndex;
		this.dateWritten = written;
		this.backupSize = backupSize;
		this.backupHash = hash;

		char[] filenameChars = file.getAbsolutePath().toCharArray();
		if (filenameChars.length > filenameLimit) {
			throw new FilenameLengthException(filenameChars, filename);
		}

		filename = Arrays.copyOf(filenameChars, filenameLimit + 1);
	}

	public void setPadding(int bytes) {
		padding = new byte[bytes];
		if (bytes > 0) {
			SecureRandom sr = new SecureRandom();
			sr.nextBytes(padding);
		}
	}

	public String getFilename() {
		return filename.toString();
	}

	public int getIndex() {
		return index;
	}

	public int getMaxIndex() {
		return maxIndex;
	}

	public DateTime getDateWritten() {
		return dateWritten;
	}

	public byte[] getData() {
		return data;
	}

	public long getBackupSize() {
		return backupSize;
	}

	public byte[] getBackupHash() {
		return backupHash;
	}

	public boolean fill(InputStream is, LocalPeer peer, int targetSize)
			throws IOException, FilenameLengthException {
		byte[] buffer = new byte[targetSize];
		int bytesRead = is.read(buffer, 0, targetSize);
		if (bytesRead < 1) {
			return false;
		}

		// Remove zeros at the end of the buffer
		ByteArrayOutputStream baos = new ByteArrayOutputStream(targetSize);
		baos.write(buffer, 0, bytesRead);

		this.data = baos.toByteArray();

		// Add padding, or at least a byte data structure
		if (peer.hasFixedChunkSize()) {
			setPadding(targetSize - bytesRead);
		} else {
			setPadding(0);
		}

		return true;
	}

	public void write(LocalPeer peer) throws GeneralSecurityException,
			IOException {
		Cipher cipher = getCipher(true, peer, id);

		OutputStream os = getOutputStream(peer);

		DataOutputStream dos = new DataOutputStream(os);
		CipherOutputStream cos = new CipherOutputStream(dos, cipher);

		ObjectOutputStream oos = new ObjectOutputStream(cos);
		oos.writeObject(this);
		oos.close();

		cos.close();

		outputSize = dos.size();

		dos.close();
		os.close();
	}

	static public ChunkData retrieve(InputStream is, Id chunkId, LocalPeer user)
			throws IOException, GeneralSecurityException {
		Cipher cipher = getCipher(false, user, chunkId);

		int rawSize = is.available();

		CipherInputStream cis = new CipherInputStream(is, cipher);
		ObjectInputStream ois = new ObjectInputStream(cis);

		try {
			Object chunkData = ois.readObject();
			if (chunkData instanceof ChunkData) {
				ChunkData cdata = (ChunkData)chunkData;
				cdata.outputSize = rawSize;
				return (ChunkData) chunkData;
			}
		} catch (ClassNotFoundException e) {
		} finally {
			// ois.close();
			// cis.close();
		}

		throw new IOException("Incorrect object type in chunk data field");
	}

	static protected Cipher getCipher(boolean encrypting, LocalPeer user,
			Id chunkId) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
		//Cipher cipher = Cipher.getInstance("AES/CTS/SHA-256/NoPadding", "BC");


		SecretKey secretKey = user.getSecretKey();
		if (secretKey == null) {
			throw new GeneralSecurityException(
					"Service peer secret key does not exist");
		}

		int cipherMode = encrypting ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE;
		cipher.init(cipherMode, secretKey, new IvParameterSpec(chunkId
				.getBytes()));

		return cipher;
	}

	public Chunk getChunk() {
		return new Chunk(id, outputSize);
	}
}
