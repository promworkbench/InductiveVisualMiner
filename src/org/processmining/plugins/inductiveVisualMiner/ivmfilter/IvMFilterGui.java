package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public abstract class IvMFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	protected boolean usesVerticalSpace = false;

	public IvMFilterGui(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setOpaque(false);
	}

	public boolean isUsesVerticalSpace() {
		return usesVerticalSpace;
	}

	/**
	 * We're drastically changing colours. Make sure the gui is not-opaque and
	 * recursively set the text colour.
	 */
	protected abstract void setForegroundRecursively(Color colour);

	@Override
	public void setForeground(Color colour) {
		super.setForeground(colour);
		setForegroundRecursively(colour);
		if (getBorder() != null) {
			((TitledBorder) getBorder()).setTitleColor(colour);
		}
	}
}
