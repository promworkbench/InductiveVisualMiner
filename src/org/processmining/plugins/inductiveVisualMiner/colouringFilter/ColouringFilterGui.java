package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Dimension;

import javax.swing.JPanel;

public abstract class ColouringFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	public ColouringFilterGui() {
		setMaximumSize(new Dimension(100, 500));
	}
}
