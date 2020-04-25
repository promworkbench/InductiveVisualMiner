package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningEventFilter;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class PreMiningFilterEvent extends PreMiningEventFilter {

	AttributeFilterGui panel = null;

	@Override
	public String getName() {
		return "Event filter";
	}

	@Override
	public IvMFilterGui createGui(final AttributesInfo attributesInfo) throws Exception {
		panel = new AttributeFilterGui(getName(), attributesInfo.getEventAttributes(), new Runnable() {
			public void run() {
				update();
				updateExplanation();
			}
		});

		updateExplanation();
		return panel;
	}

	@Override
	public boolean staysInLog(XEvent event) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
					.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
				return true;
			}
		} else if (attribute.isNumeric()) {
			if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
				double value = Attribute.parseDoubleFast(event.getAttributes().get(attribute.getName()));
				if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
					return true;
				}
			}
		} else if (attribute.isTime()) {
			if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
				long value = Attribute.parseTimeFast(event.getAttributes().get(attribute.getName()));
				if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean fillGuiWithLog(IMLog log) throws Exception {
		return false;
	}

	@Override
	protected boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel().setText("Include only events having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText("Include only events " + panel.getExplanation() + ".");
		}
	}

}
