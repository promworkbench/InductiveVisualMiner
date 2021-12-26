package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFrequentTracesFilter extends PreMiningTraceFilter {

	private SliderGui panel = null;
	private FrequencyLog fLog = null;

	@Override
	public boolean staysInLog(IMTrace trace) {
		return fLog.isFrequentEnough(trace, panel.getSlider().getValue() / 1000.0);
	}

	@Override
	public String getName() {
		return "Frequent traces filter";
	}

	@Override
	public IvMFilterGui createGui(IvMDecoratorI decorator) {
		panel = new SliderGui(getName(), decorator);

		panel.getSlider().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateExplanation();
				update();
			}
		});
		panel.getSlider().setEnabled(false);

		updateExplanation();
		return panel;
	}

	@Override
	protected boolean isEnabled() {
		return getThreshold() < 1.0 && panel.getSlider().isEnabled() && fLog != null;
	}

	private double getThreshold() {
		return panel.getSlider().getValue() / 1000.0;
	}

	private void updateExplanation() {
		panel.getExplanation()
				.setText("Include " + String.format("%.1f", getThreshold() * 100) + "% of the most occurring traces.");
	}

	@Override
	public void setAttributesInfo(AttributesInfo attributesInfo) {
		//TODO: fill in using attributes
	}

	public boolean fillGuiWithLog(IMLog log, XLog xLog) throws Exception {
		fLog = null;
		panel.getSlider().setEnabled(false);
		panel.getExplanation().setText("Initialising...");

		fLog = new FrequencyLog(xLog, log.getClassifier());

		updateExplanation();
		panel.getSlider().setEnabled(true);
		return isEnabled();
	}

}
