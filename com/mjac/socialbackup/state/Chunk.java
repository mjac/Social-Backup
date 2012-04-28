package com.mjac.socialbackup.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;

import org.apache.log4j.Logger;

import com.mjac.socialbackup.Id;

/** Just a data store. */
public class Chunk implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Chunk.class);

	protected Id id;

	/** Size of serialized chunk on disk */
	protected int outputSize;

	public Chunk(Id id, int outputSize) {
		this.id = id;
		this.outputSize = outputSize;
	}

	public Id getId() {
		return id;
	}

	public int getOutputSize() {
		return outputSize;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final Chunk other = (Chunk) obj;
		return other.id == id;
	}

	public File getFile(Peer peer) {
		File chunkDir = peer.getChunkDirectory();
		return new File(chunkDir.getPath() + File.separatorChar + id.toString());
	}

	public OutputStream getOutputStream(Peer peer) {
		File peerFile = getFile(peer);
		try {
			return new FileOutputStream(peerFile);
		} catch (FileNotFoundException e) {
			if (peerFile.getParentFile().mkdirs()) {
				try {
					return new FileOutputStream(peerFile);
				} catch (FileNotFoundException e2) {
					logger.warn(
							"Made all directories required but still failed to get output stream for "
									+ peerFile, e2);
					return null;
				}
			}

			logger.warn("Could not make directories for chunk " + peerFile);
			return null;
		}
	}

	public InputStream getInputStream(Peer peer) throws IOException {
		File chunkFile = getFile(peer);
		if (chunkFile == null) {
			return null;
		}

		return new FileInputStream(chunkFile);
	}

	/** Write a raw chunk to a peer. */
	public void writeBytes(Peer peer, byte[] data)
			throws GeneralSecurityException, IOException {
		OutputStream os = getOutputStream(peer);
		os.write(data);
		os.close();
	}

	/**
	 * Return encrypted chunk data.
	 * 
	 * @throws IOException
	 */
	public byte[] getEncryptedData(Peer peer) throws IOException {
		InputStream fis = getInputStream(peer);
		byte[] allBytes = new byte[fis.available()];
		fis.read(allBytes);
		fis.close();
		return allBytes;
	}
}
