package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

public class TraceAttributeFilter extends ColouringFilter {
	TraceAttributeFilterFrame filterFrame;

	public TraceAttributeFilter(JComponent parent, XLog xLog) {
		super(parent, xLog);
	}

	public JFrame createGui(JComponent parent) {
		if (filterFrame == null) {
			filterFrame = createFrame(xLog, new Runnable() {
				public void run() {
					update();
				}
			});
		}

		return filterFrame;
	}

	public boolean countInColouring(XTrace xTrace, AlignedTrace aTrace) {
		String selectedKey = filterFrame.getSelectedKey();
		XAttribute attribute = xTrace.getAttributes().get(selectedKey);		
		if (!xTrace.getAttributes().containsKey(selectedKey)) {
			return false;
		}		
		XAttribute selectedAttribute = filterFrame.getSelectedAttribute();
		// TODO: test equals function
		return attribute.equals(selectedAttribute);
	}

	private TraceAttributeFilterFrame createFrame(XLog log, Runnable update) {
		return new TraceAttributeFilterFrame(log, update);
	}

	public boolean isEnabled() {
		return filterFrame.getSelectedKey() != null && filterFrame.getSelectedAttribute() != null && filterFrame.isEnabledChecked();
	}

}
