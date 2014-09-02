package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import javax.swing.JFrame;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public interface ColouringFilter {
	
	public boolean countInColouring(XLog xLog, XTrace xTrace, AlignedTrace aTrace);
	
	public JFrame createGui(Runnable update);

}
