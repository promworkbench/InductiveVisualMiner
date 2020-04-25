package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class HighlightingFilterWithoutEvent extends HighlightingFilter {

	AttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Without event filter";
	}

	public IvMFilterGui createGui(final AttributesInfo attributesInfo) {
		panel = new AttributeFilterGui(getName(), attributesInfo.getEventAttributes(), new Runnable() {
			public void run() {
				updateExplanation();
				update();
			}
		});
		updateExplanation();
		return panel;
	}

	protected boolean fillGuiWithLog(IMLog log, XLog xLog, IvMLog ivmLog) throws Exception {
		return false;
	}

	public boolean countInColouring(IvMTrace trace) {
		return isTraceIncluded(trace, panel);
	}

	public static boolean isTraceIncluded(IvMTrace trace, AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (IvMMove event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
						.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
					return false;
				}
			}
		} else if (attribute.isNumeric()) {
			for (IvMMove event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					double value = Attribute.parseDoubleFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
						return false;
					}
				}
			}
		} else if (attribute.isTime()) {
			for (IvMMove event : trace) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					long value = Attribute.parseTimeFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces that have no event having an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces that have no event " + panel.getExplanation() + ".");
		}
	}

}
