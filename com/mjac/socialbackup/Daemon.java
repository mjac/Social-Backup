package com.mjac.socialbackup;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.mjac.socialbackup.crypto.KeystoreManager;
import com.mjac.socialbackup.gui.KeystoreDialog;
import com.mjac.socialbackup.gui.ServiceDialog;
import com.mjac.socialbackup.gui.TrayInterface;
import com.mjac.socialbackup.state.LocalPeer;

public class Daemon {
	static Logger logger = Logger.getLogger(Daemon.class);

	protected char[] keystorePassword = new char[] {};
	protected KeystoreManager keystoreManager;

	final static protected String configFilename = "socialbackup.properties";

	protected File directory;
	final static protected String defaultDirectory = "./";

	protected File keystore;
	final static protected String defaultKeystore = "./socialbackup.bks";

	// protected Queue<ChangeEvent> changeEvents = new
	// ArrayBlockingQueue<ChangeEvent>();

	protected TrayInterface tray;

	private LocalPeer servicePeer;
	private ChangeDispatcher changeDispatcher;

	public Daemon(File directory, File keystore) {
		this.directory = directory;
		this.keystore = keystore;
	}

	public void exit() {
		servicePeer.stopService();
		System.exit(0);
	}

	private KeystoreManager keystoreCreate() {
		try {
			KeystoreManager km = new KeystoreManager(keystore, keystorePassword);
			km.blank();
			km.store();
			return km;
		} catch (Exception e) {
			logger.error("Error creating keystore", e);
		}
		return null;
	}

	public KeystoreManager getKeystoreManager() {
		ArrayList<String> keystoreErrors = new ArrayList<String>();

		KeystoreManager openKM;
		try {
			openKM = new KeystoreManager(keystore, keystorePassword);
			try {
				openKM.load();
			} catch (FileNotFoundException e) {
				// Create empty keystore as file missing.
				return keystoreCreate();
			} catch (IOException e) {
				logger.warn("Keystore IO exception", e);
				keystoreErrors.add("io");
			} catch (NoSuchAlgorithmException e) {
				logger.warn("Integrity algorithm missing", e);
				keystoreErrors.add("integrity");
			} catch (CertificateException e) {
				logger.warn("Certificates could not be loaded", e);
				keystoreErrors.add("certificates");
			}
		} catch (KeyStoreException e) {
			logger.fatal("Bouncy castle cannot be used", e);
			return null;
		}

		if (keystoreErrors.size() < 1) {
			// Loaded without error.
			return openKM;
		}

		// Manual interaction required.
		changeKeystore();

		return null;
	}

	public KeystoreManager changeKeystore() {
		KeystoreDialog kd = new KeystoreDialog(keystore);
		kd.setModal(true);
		kd.setVisible(true);

		return null;
	}

	/** Entry for the application */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		logger.info("Application started");

		String directory = defaultDirectory;
		String keystore = defaultKeystore;
		if (args.length > 0) {
			directory = args[0];
			logger.info("Using custom directory " + directory);
			if (args.length > 1) {
				keystore = args[1];
				logger.info("Using custom keystore " + keystore);
			}
		}

		// logger.trace("Setting up Java environment");
		// System.setProperty("javax.net.debug", "all");
		//
		// try {
		// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		// } catch (Exception e) {
		// logger.debug("Could not set look and feel", e);
		// }

		logger.trace("Adding BouncyCastle to Java");
		Security.addProvider(new BouncyCastleProvider());

		logger.trace("Starting daemon");
		Daemon d = new Daemon(new File(directory), new File(keystore));

		d.start();
	}

	/**
	 * Ensure that the configuration file is present and contains valid entries.
	 */
	private boolean start() {
		if (!directory.isDirectory()) {
			logger.error("Social Backup directory does not exist");
			return false;
		}

		keystoreManager = getKeystoreManager();
		if (keystoreManager == null) {
			return false;
		}

		ArrayList<LocalPeer> servicePeers = new ArrayList<LocalPeer>();

		for (File checkFile : directory.listFiles()) {
			Id serviceId = LocalPeer.fileId(checkFile);
			if (serviceId == null) {
				continue;
			}

			logger.trace("Found service " + serviceId);

			LocalPeer sp = new LocalPeer(serviceId, directory);

			try {
				LocalPeer spRestored = sp.restore();
				spRestored.setKeystoreManager(keystoreManager);
				spRestored.restoreClients();
				servicePeers.add(spRestored);
				spRestored.persist();
			} catch (IOException e) {
				logger.warn("Could not restore local " + serviceId, e);
			} catch (ClassNotFoundException e) {
				logger.warn("Could not restore local " + serviceId, e);
			}
		}

		int peerNo = servicePeers.size();
		if (peerNo < 1) {
			LocalPeer newService = new LocalPeer(new RandomisedId(), directory);
			newService.setKeystoreManager(keystoreManager);

			if (!editService(newService)) {
				JOptionPane.showMessageDialog(null,
						"Missing a valid service configuration, exiting.",
						"Configuration issues", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			servicePeers.add(newService);
		}
		
		servicePeer = servicePeers.get(0);
		
		if (!servicePeer.hasKeys()) {
			try {
				servicePeer.createKeys();
			} catch (Exception e) {
				logger.fatal("Could not generate keys", e);
				return false;
			}
		}

		try {
			servicePeer.updateCertificate();
			keystoreManager.store();
		} catch (Exception e) {
			logger.error("Could not update keystore", e);
			return false;
		}

		if (!SystemTray.isSupported()) {
			JOptionPane
					.showMessageDialog(
							null,
							"Social Backup requires the system tray and this feature is not supported on your system",
							"Startup error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		logger.debug(keystoreManager);
		
		changeDispatcher = new ChangeDispatcher();
		changeDispatcher.createThread();

		servicePeer.setChangeDispatcher(changeDispatcher);
		changeDispatcher.add(servicePeer);

		try {
			tray = new TrayInterface(servicePeer, changeDispatcher);
			SystemTray systemTray = SystemTray.getSystemTray();
			systemTray.add(tray);
			changeDispatcher.add(tray);
		} catch (AWTException e) {
			logger.error("System tray not initialised", e);
		}

		servicePeer.startService();

		return true;
	}
	
	static public void editRunningService(LocalPeer sp)
	{
		if (editService(sp)) {
			sp.stopService();
			sp.startService();
		}
	}

	static public boolean editService(LocalPeer sp) {
		ServiceDialog sdChanger = new ServiceDialog(sp);
		sdChanger.setModal(true);
		sdChanger.setAlwaysOnTop(true);
		sdChanger.setVisible(true);

		if (sdChanger.changed()) {
			return sp.persist();
		}

		return false;
	}
}
