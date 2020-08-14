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

public class HighlightingFilterTraceEndsWithEvent extends HighlightingFilter {

	AttributeFilterGui panel = null;
	boolean block = true;

	public String getName() {
		return "Trace ends with event filter";
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
		for (int i = trace.size() - 1; i > 0; i--) {
			IvMMove event = trace.get(i);
			if (event.isComplete()) {
				if (attribute.isLiteral()) {
					String value = attribute.getLiteral(event);
					if (value != null && panel.getSelectedLiterals().contains(value)) {
						return true;
					}
				} else if (attribute.isNumeric()) {
					double value = attribute.getNumeric(event);
					if (value != -Double.MAX_VALUE && value >= panel.getSelectedNumericMin()
							&& value <= panel.getSelectedNumericMax()) {
						return true;
					}
				} else if (attribute.isTime()) {
					long value = attribute.getTime(event);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
						return true;
					}
				} else if (attribute.isDuration()) {
					long value = attribute.getDuration(event);
					if (value != Long.MIN_VALUE && value >= panel.getSelectedTimeMin()
							&& value <= panel.getSelectedTimeMax()) {
						return true;
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
					.setText("Include only traces whose last completion event has an attribute as selected.");
		} else {
			panel.getExplanationLabel()
					.setText("Include only traces whose last completion event " + panel.getExplanation() + ".");
		}
	}

}
