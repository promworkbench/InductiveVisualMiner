package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;

public class PreMiningFrequentTracesFilter extends PreMiningTraceFilter {

	SliderGui panel = null;
	
	public boolean staysInLog(IMTrace trace) {
		trace.getXTraceIndex();
		// TODO Auto-generated method stub
		return false;
	}

	public String getName() {
		return "Frequent traces filter";
	}

	public IvMFilterGui createGui(AttributesInfo attributeInfo) {
		panel = new SliderGui(getName());
		
		panel.getSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateExplanation();
				update();
			}
		});
		
		updateExplanation();
		return panel;
	}

	protected boolean isEnabled() {
		return getThreshold() < 1.0;
	}
	
	private double getThreshold() {
		return panel.getSlider().getValue() / 1000.0;
	}

	private void updateExplanation() {
		panel.getExplanation().setText("Include " + String.format("%.2f", getThreshold() * 100) + "% of the most occurring traces.");
	}

	public boolean fillGuiWithLog(IMLog log) throws Exception {
		return false;
	}

}
