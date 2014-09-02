package org.processmining.plugins.inductiveVisualMiner.ColouringFilter;

import javax.swing.JFrame;

import org.deckfour.xes.model.XTrace;

public interface ColouringFilter {
	
	public boolean countInColouring(XTrace trace);
	
	public JFrame createGui(Runnable update);

}
