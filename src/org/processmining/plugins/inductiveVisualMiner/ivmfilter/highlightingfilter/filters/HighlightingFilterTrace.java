package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapPropertyDuration;

public class HighlightingFilterTrace extends HighlightingFilterEvent {

	@Override
	public IvMFilterGui createGui(final AttributesInfo attributesInfo) {
		panel = new AttributeFilterGui(getName(), attributesInfo.getTraceAttributes(), new Runnable() {
			public void run() {
				updateExplanation();
				update();
			}
		});
		updateExplanation();
		return panel;
	}

	@Override
	public String getName() {
		return "Trace filter";
	}

	@Override
	public boolean countInColouring(IvMTrace trace) {
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			if (trace.getAttributes() != null && trace.getAttributes().containsKey(attribute.getName()) && panel
					.getSelectedLiterals().contains(trace.getAttributes().get(attribute.getName()).toString())) {
				return true;
			}
		} else if (attribute.isNumeric()) {
			if (trace.getAttributes() != null && trace.getAttributes().containsKey(attribute.getName())) {
				double value = Attribute.parseDoubleFast(trace.getAttributes().get(attribute.getName()));
				if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
					return true;
				}
			}
		} else if (attribute.isTime()) {
			if (trace.getAttributes() != null && trace.getAttributes().containsKey(attribute.getName())) {
				long value = Attribute.parseTimeFast(trace.getAttributes().get(attribute.getName()));
				if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
					return true;
				}
			}
		} else if (attribute.isTraceNumberofEvents()) {
			int count = trace.getNumberOfEvents();
			return count >= panel.getSelectedNumericMin() && count <= panel.getSelectedNumericMax();
		} else if (attribute.isTraceDuration()) {
			long duration = TraceColourMapPropertyDuration.getTraceDuration(trace);
			return duration >= panel.getSelectedNumericMin() && duration <= panel.getSelectedNumericMax();
		}
		return false;
	}

	@Override
	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel().setText("Include only traces having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText("Include only traces " + panel.getExplanation() + ".");
		}
	}
}
