package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class IvMFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;
	
	protected boolean usesVerticalSpace = false;

	public IvMFilterGui(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setBackground(Color.gray);
	}
	
	public boolean isUsesVerticalSpace() {
		return usesVerticalSpace;
	}
}
