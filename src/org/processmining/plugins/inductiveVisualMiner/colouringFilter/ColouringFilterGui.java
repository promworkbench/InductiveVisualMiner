package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public abstract class ColouringFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	public ColouringFilterGui(String title) {
		setBorder(BorderFactory.createTitledBorder(title));
		setBackground(Color.gray);
	}
}
