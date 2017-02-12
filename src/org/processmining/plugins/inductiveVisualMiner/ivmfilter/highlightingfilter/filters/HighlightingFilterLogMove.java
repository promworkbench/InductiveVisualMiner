package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterLogMove extends HighlightingFilterEvent {

	@Override
	public String getName() {
		return "Log move filter";
	}

	@Override
	public boolean countInColouring(IvMTrace trace) {		
		Attribute attribute = panel.getSelectedAttribute();
		if (attribute.isLiteral()) {
			for (IvMMove event : trace) {
				if (event.isLogMove() && event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
						.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
					return true;
				}
			}
		} else if (attribute.isNumeric()) {
			for (IvMMove event : trace) {
				if (event.isLogMove() && event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					double value = Attribute.parseDoubleFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
						return true;
					}
				}
			}
		} else if (attribute.isTime()) {
			for (IvMMove event : trace) {
				if (event.isLogMove() && event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					long value = Attribute.parseTimeFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void updateExplanation() {		
		if (!isEnabled()) {
			panel.getExplanationLabel().setText(
					"Include only traces that have at least one log move having an attribute as selected.");
		} else {
			panel.getExplanationLabel().setText(
					"Include only traces that have at least one log move " + panel.getExplanation() + ".");
		}
	}
}
