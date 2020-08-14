package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFilterTraceWithEvent extends PreMiningTraceFilter {

	protected AttributeFilterGui panel = null;

	public String getName() {
		return "Trace with event filter";
	}

	public IvMFilterGui createGui(final AttributesInfo attributesInfo) {
		panel = new AttributeFilterGui(getName(), attributesInfo.getEventAttributes(), new Runnable() {
			public void run() {
				update();
				updateExplanation();
			}
		});

		updateExplanation();
		return panel;
	}

	public boolean staysInLog(IMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (XEvent event : trace) {
				String value = attribute.getLiteral(event);
				if (value != null && panel.getSelectedLiterals().contains(value)) {
					return true;
				}
			}
		} else if (attribute.isNumeric()) {
			for (XEvent event : trace) {
				double value = attribute.getNumeric(event);
				if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
						&& value <= panel.getSelectedNumericMax()) {
					return true;
				}
			}
		} else if (attribute.isTime()) {
			for (XEvent event : trace) {
				long value = attribute.getTime(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		} else if (attribute.isDuration()) {
			for (XEvent event : trace) {
				long value = attribute.getDuration(event);
				if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
						&& value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least one event having an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces that have at least one event " + panel.getExplanation() + ".");
		}
	}

	public boolean fillGuiWithLog(IMLog log, XLog xLog) throws Exception {
		return false;
	}

}
