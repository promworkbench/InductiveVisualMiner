package org.processmining.plugins.inductiveVisualMiner.highlightingfilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class HighlightingFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;
	
	protected boolean usesVerticalSpace = false;

	public HighlightingFilterGui(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setBackground(Color.gray);
	}
	
	public boolean isUsesVerticalSpace() {
		return usesVerticalSpace;
	}
}
