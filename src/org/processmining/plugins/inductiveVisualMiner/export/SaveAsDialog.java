package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.processmining.plugins.InductiveMiner.Pair;

public class SaveAsDialog extends JFileChooser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6614457648530087883L;
	private static final Preferences preferences = Preferences.userRoot().node("org.processmining.graphviz");
	
	public enum FileType {
		pngImage, pdfImage, svgImage, aviMovie
	}
	
	private class IvMFileFilter extends FileFilter {

		private final String ext;
		private final String description;
		private final FileType type;
		
		public IvMFileFilter(FileType type, String extension, String description) {
			this.type = type;
			ext = extension;
			this.description = description;
		}
		
		public boolean accept(File file) {
			String extension = "";
			int i = file.getName().lastIndexOf('.');
			if (i >= 0) {
				extension = file.getName().substring(i + 1);
			}
			return file.isDirectory() || extension.toLowerCase().equals(ext);
		}

		public String getDescription() {
			return description;
		}
		
		public String getExtension() {
			return ext;
		}
		
		public FileType getType() {
			return type;
		}
	}

	public SaveAsDialog(boolean animationExportPossible) {
		super(preferences.get("lastUsedFolder", new File(".").getAbsolutePath()));
		setAcceptAllFileFilterUsed(false);
		addChoosableFileFilter(new IvMFileFilter(FileType.pngImage, "png", "png"));
		addChoosableFileFilter(new IvMFileFilter(FileType.pdfImage, "pdf", "pdf"));
		addChoosableFileFilter(new IvMFileFilter(FileType.svgImage, "svg", "svg"));
		if (animationExportPossible) {
			addChoosableFileFilter(new IvMFileFilter(FileType.aviMovie, "avi", "avi (animation)"));
		}
	}

	@Override
	public void approveSelection() {
		File f = getSelectedFile();
		if (f.exists() && getDialogType() == SAVE_DIALOG) {
			int result = JOptionPane.showConfirmDialog(this, "The file already exists, do you want to overwrite it?",
					"Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (result) {
				case JOptionPane.YES_OPTION :
					super.approveSelection();
					return;
				case JOptionPane.NO_OPTION :
					return;
				case JOptionPane.CLOSED_OPTION :
					return;
				case JOptionPane.CANCEL_OPTION :
					cancelSelection();
					return;
			}
		}
		super.approveSelection();
	}

	public Pair<File, FileType> askUser(JComponent panel) {
		int returnVal = showSaveDialog(panel);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = getSelectedFile();

			//get type and add file extension if necessary
			IvMFileFilter fileFilter = (IvMFileFilter) getFileFilter();
			if (!file.getName().endsWith(fileFilter.getExtension())) {
				file = new File(file + "." + fileFilter.getExtension());
			}

			return Pair.of(file, fileFilter.getType());

		} else {
			return null;
		}
	}
}
