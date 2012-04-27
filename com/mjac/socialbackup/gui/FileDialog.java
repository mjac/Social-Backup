package com.mjac.socialbackup.gui;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import com.mjac.socialbackup.state.Backup;
import com.mjac.socialbackup.state.LocalPeer;

@SuppressWarnings("serial")
public class FileDialog extends MutableDialog {
	private static final Logger logger = Logger.getLogger(FileDialog.class);

	LocalPeer servicePeer;

	public FileDialog(LocalPeer servicePeer) {
		super((JFrame) null, "Backed up files");
		this.servicePeer = servicePeer;
		logger.trace("Updating");
		createGui();
		updateGui();
	}

	public void test() {
	}

	protected void restore(int idx) {
		Backup toRestore = servicePeer.getBackups().get(idx);
		JFileChooser fc = new JFileChooser();

		File restoreFile = toRestore.getFile();
		fc.setSelectedFile(restoreFile);

		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if (toRestore.read(servicePeer, file)) {
				JOptionPane.showMessageDialog(FileDialog.this, "Your backup "
						+ restoreFile.getAbsolutePath()
						+ "\nhas been restored to " + file.getAbsolutePath(),
						"File restore successful",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(FileDialog.this, "Your backup "
						+ restoreFile.getAbsolutePath()
						+ "\ncould not be restored.", "File restore failed",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	@Override
	protected void createGui() {
		GroupLayout layout = new GroupLayout(this.getContentPane());

		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		// treeRoot = getBackupTree();
		// treeModel = new BackupTreeModel(treeRoot);
		//
		// JTree tree = new JTree(getBackupTree());
		// tree.setRootVisible(false);
		// tree.setPreferredSize(new Dimension(400, 400));
		// JScrollPane treeView = new JScrollPane(tree);

		ListModel bigData = new AbstractListModel() {
			public int getSize() {
				return servicePeer.getBackups().size();
			}

			public Object getElementAt(int index) {
				return servicePeer.getBackups().get(index).getFile()
						.getAbsolutePath();
			}
		};

		final JList list = new JList(bigData);
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					int index = list.getSelectedIndex();
					list.ensureIndexIsVisible(index);
					restore(index);
				}
			}
		});
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = list.locationToIndex(e.getPoint());
					ListModel dlm = list.getModel();
					Object item = dlm.getElementAt(index);
					;
					list.ensureIndexIsVisible(index);
					System.out.println("Double clicked on " + item);
					restore(index);
				}
			}
		});
		JScrollPane treeView = new JScrollPane(list);
		treeView.setPreferredSize(new Dimension(400, 300));

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(
				treeView));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(
				treeView));

		setLayout(layout);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
	}

	@Override
	public void updateGui() {
		logger.trace("Updating");
		// ArrayList<Backup> backups = servicePeer.getBackups();
		// for (Backup backup : backups) {
		// // logger.trace(StringUtils.split(backup.getFile().getAbsolutePath(),
		// // File.separatorChar).length);
		// // MutableTreeNode mtn = new DefaultMutableTreeNode(backup.getFile()
		// // .getAbsolutePath());
		// //
		// // treeRoot.add(mtn);
		// }
		// treeModel.nodeStructureChanged(treeRoot);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
	}

	@Override
	public boolean holdsObject(Object obj) {
		return servicePeer == obj;
	}

	@Override
	public Object getHeldObject() {
		return servicePeer;
	}
}
