package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class HighlightingFilterTraceEndsWithEvent extends HighlightingFilter {

	AttributeFilterGui panel = null;
	boolean block = true;

	@Override
	public String getName() {
		return "Trace ends with event filter";
	}

	@Override
	public IvMFilterGui createGui() {
		panel = new AttributeFilterGui(getName(), new Runnable() {
			public void run() {
				updateExplanation();
				update();
			}
		});
		return panel;
	}

	@Override
	public void setAttributesInfo(IvMAttributesInfo attributesInfo) {
		panel.setAttributes(attributesInfo.getEventAttributes());
		updateExplanation();
	}

	@Override
	public boolean staysInLog(IvMTrace trace) {
		return isTraceIncluded(trace, panel);
	}

	public static boolean isTraceIncluded(IvMTrace trace, AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		if (trace.size() == 0) {
			return false;
		}
		for (int i = trace.size() - 1; i > 0; i--) {
			IvMMove event = trace.get(i);
			if (event.isComplete() && !event.isModelMove()) {
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

	@Override
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
