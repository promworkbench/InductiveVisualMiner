package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class ColouringFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	public ColouringFilterGui(String title) {
//		setMaximumSize(new Dimension(500, 500));
		setBorder(BorderFactory.createTitledBorder(title));
	}
}
