package com.mjac.socialbackup.actors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.mjac.socialbackup.Id;
import com.mjac.socialbackup.PeerTracker;
import com.mjac.socialbackup.state.Chunk;
import com.mjac.socialbackup.state.ChunkList;

public class User extends Peer implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(User.class);

	private static final String fileExtension = ".peer";

	protected PeerTracker tracker = new PeerTracker();

	/** Used in filesystem and keystore */
	protected Id id;

	public Id getId() {
		return id;
	}

	// Chunk store!!!!
	/** List of chunks associated with the peer. */
	protected ChunkList chunks = new ChunkList();

	protected transient File localStore;

	public User(Id id, File localStore) {
		this.id = id;
		this.localStore = localStore;
	}

	public PeerTracker getTracker() {
		return tracker;
	}

	// CHUNKS CORRESPONDING TO THE PEER

	public ChunkList getChunkList() {
		return chunks;
	}

	public void setChunkList(ChunkList newChunkList) {
		chunks = newChunkList;
	}

	// PEER LOCATION ON DISK

	public File getFile() {
		return getFile(fileExtension);
	}

	protected File getFile(String ext) {
		return new File(localStore.getPath() + File.separatorChar + id + ext);
	}

	static public Id fileId(File checkFile) {
		return fileId(checkFile, fileExtension);
	}

	protected static Id fileId(File checkFile, String ext) {
		String name = checkFile.getName();
		if (name.endsWith(ext)) {
			String idStr = name.substring(0, name.length() - ext.length());
			return new Id(idStr);
		}
		return null;
	}

	public File getChunkDirectory() {

		return new File(localStore.getPath() + File.separatorChar + id);
	}

	// SERIALIZATION

	public boolean persist() {
		try {
			FileOutputStream fos = new FileOutputStream(getFile());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this);
			oos.close();
			fos.close();
			return true;
		} catch (Exception e) {
			logger.warn("Could not persist peer", e);
			return false;
		}
	}

	public User restore() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(getFile());
		ObjectInputStream ois = new ObjectInputStream(fis);
		Object loaded = ois.readObject();
		ois.close();
		fis.close();
		return (User) loaded;
	}

	public boolean writeChunkData(Chunk chunk, byte[] data, ChunkList toUpdate) {
		try {
			chunk.writeBytes(this, data);
		} catch (Exception e) {
			logger.warn("Raw chunk data could not be written.", e);
			return false;
		}

		toUpdate.set(chunk);

		persist();

		return true;
	}
}
