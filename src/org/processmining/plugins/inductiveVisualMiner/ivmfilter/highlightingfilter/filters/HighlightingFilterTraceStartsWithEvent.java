package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterTraceStartsWithEvent extends HighlightingFilter {

	AttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Trace starts with event filter";
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
		if (trace.size() == 0) {
			return false;
		}
		for (IvMMove event : trace) {
			if (event.isComplete()) {
				if (attribute.isLiteral()) {
					if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())
							&& panel.getSelectedLiterals()
									.contains(event.getAttributes().get(attribute.getName()).toString())) {
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
		}
		return false;
	}

	public boolean isEnabled() {
		return panel.isFiltering();
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces whose first completion event has an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces whose first completion event " + panel.getExplanation() + ".");
		}
	}

}
