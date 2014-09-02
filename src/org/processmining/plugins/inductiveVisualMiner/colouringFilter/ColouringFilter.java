package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public interface ColouringFilter {
	
	public JFrame createGui(JComponent parent, XLog log, Runnable update);
	
	public boolean countInColouring(XTrace xTrace, AlignedTrace aTrace);

}
