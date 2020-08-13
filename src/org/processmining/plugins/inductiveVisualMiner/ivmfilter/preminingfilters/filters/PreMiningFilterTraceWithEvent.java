package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningTraceFilter;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;
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
		//TODO: update
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
						.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
					return true;
				}
			}
		} else if (attribute.isNumeric()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					double value = AttributeUtils.parseDoubleFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
						return true;
					}
				}
			}
		} else if (attribute.isTime()) {
			for (XEvent event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					long value = AttributeUtils.parseTimeFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
						return true;
					}
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
