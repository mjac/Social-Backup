package com.mjac.socialbackup.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.RandomisedId;
import com.mjac.socialbackup.crypto.Hasher;

public class Backup implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(Backup.class);

	static protected int defaultChunkSize = 64 << 10;

	protected Id[] chunkIds;

	protected File file;
	protected long size;
	protected byte[] hash;

	protected DateTime written;

	BackupStrategy strategy;

	public BackupStrategy getStrategy() {
		return strategy;
	}

	public File getFile() {
		return file;
	}

	public long getSize() {
		return size;
	}

	public DateTime getWritten() {
		return written;
	}

	/**
	 * Normal way to specific a backup
	 * 
	 * In recover mode, use a single chunk to specify the file, then look at the
	 * gaps
	 */
	public Backup(File file) {
		this.file = file;
	}

	/**
	 * Recover backup from chunk? Perhaps make this a static call.
	 */
	public Backup(ChunkData cdata) {
		file = new File(cdata.getFilename());
		hash = cdata.getBackupHash();
		size = cdata.getBackupSize();
		chunkIds = new Id[cdata.getMaxIndex() + 1];
		chunkIds[cdata.getIndex()] = cdata.getId();
	}

	public boolean isAvailable(LocalPeer localPeer) {
		ChunkList peerStore = localPeer.getChunkList();
		for (Id chunkId : chunkIds) {
			if (!peerStore.has(chunkId)) {
				return false;
			}
		}
		return true;
	}

	public boolean verify(InputStream is) {
		try {
			byte[] isHash = Hasher.hash(is);
			return Arrays.equals(isHash, hash);
		} catch (Exception e) {
			logger.warn("Hashing input stream failed", e);
			return false;
		}
	}

	public boolean readToStream(LocalPeer user, OutputStream os)
			throws IOException {
		ChunkList localStore = user.getChunkList();
		for (Id chunkId : chunkIds) {
			Chunk chunk = localStore.get(chunkId);
			if (chunk == null) {
				return false;
			}

			InputStream chunkInput = null;
			try {
				chunkInput = chunk.getInputStream(user, user);
				ChunkData cdata = ChunkData.retrieve(chunkInput, chunk.getId(),
						user);
				os.write(cdata.getData());
			} catch (Exception e) {
				logger.warn("Error reading chunk data", e);
				return false;
			} finally {
				if (chunkInput != null) {
					chunkInput.close();
				}
			}
		}

		return true;
	}

	public boolean read(LocalPeer user, File outputFile) {
		try {
			FileOutputStream fos = new FileOutputStream(outputFile);
			readToStream(user, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		return true;
	}

	public byte[] hash() throws IOException, NoSuchAlgorithmException {
		FileInputStream fis = new FileInputStream(file);
		byte[] hashBytes = Hasher.hash(fis);
		fis.close();
		return hashBytes;
	}

	public boolean write(LocalPeer user) {
		try {
			byte[] hash = hash();
			logger.trace("Hashed to " + Hex.encodeHexString(hash));

			FileInputStream fis = new FileInputStream(file);
			boolean result = write(fis, user, hash);
			fis.close();
			return result;
		} catch (FileNotFoundException e) {
			logger.warn("File not found while adding backup", e);
		} catch (Exception e) {
			logger.warn("Error writing backup", e);
		}
		return false;
	}

	public boolean write(InputStream is, LocalPeer user, byte[] hash) {
		try {
			DateTime written = new DateTime();

			ArrayList<Chunk> newChunks = new ArrayList<Chunk>();

			int totalBytes = is.available();
			int maxIndex = totalBytes / defaultChunkSize;

			int index = 0;
			do {
				ChunkData cdata = new ChunkData(new RandomisedId(), file,
						index, maxIndex, written, totalBytes, hash);
				boolean dataFilled = cdata.fill(is, user, defaultChunkSize);
				if (!dataFilled) {
					break;
				}

				cdata.write(user);

				Chunk chunk = cdata.getChunk();
				newChunks.add(chunk);
				++index;
			} while (true);

			if (!addChunksToPeer(user, newChunks)) {
				throw new Exception("Chunk ID is already in use by peer");
			}

			writeChunkArray(newChunks);
			this.hash = hash;
			this.written = written;

			return true;
		} catch (Exception e) {
			logger.warn("Backup write failed", e);
			return false;
		}
	}

	private boolean addChunksToPeer(Peer peer, ArrayList<Chunk> newChunks) {
		ChunkList peerStore = peer.getChunkList();
		for (Chunk chunk : newChunks) {
			if (peerStore.has(chunk)) {
				logger.warn("Chunk already exists for peer, not random enough ID!");
				return false;
			}
		}

		for (Chunk chunk : newChunks) {
			peer.getChunkList().set(chunk);
		}

		return true;
	}

	private void writeChunkArray(ArrayList<Chunk> newChunks) {
		chunkIds = new Id[newChunks.size()];
		int chunkIdx = 0;
		for (Chunk chunk : newChunks) {
			chunkIds[chunkIdx] = chunk.getId();
			++chunkIdx;
		}
	}

	public Id[] getChunkIds() {
		return chunkIds;
	}

	/** Merge chunk ids from foreign backup. */
	public boolean mergeBackup(Backup backup) {
		Id[] newIds = backup.getChunkIds();

		if (newIds.length != chunkIds.length) {
			return false;
		}

		// Check first
		for (int idx = 0; idx < newIds.length; ++idx) {
			if (chunkIds[idx] != null && newIds[idx] != null
					&& !chunkIds[idx].equals(newIds[idx])) {
				return false;
			}
		}

		// It is fine so modify
		for (int idx = 0; idx < newIds.length; ++idx) {
			if (newIds[idx] != null) {
				chunkIds[idx] = newIds[idx];
			}
		}

		return true;
	}
}
