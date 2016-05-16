package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class MultiLogMoveAttributeFilter extends MultiEventAttributeFilter {

	@Override
	public String getName() {
		return "Log move filter";
	}

	@Override
	public boolean countInColouring(IvMTrace trace) {
		String key = panel.getSelectedKey();
		for (IvMMove event : trace) {
			if (event.isLogMove() && event.getAttributes() != null && event.getAttributes().containsKey(key)
					&& panel.getSelectedAttributes().contains(event.getAttributes().get(key).toString())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation()
					.setText(
							"<html>Include only traces that have at least one log move having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only traces that have at least one log move ");
			printKey(s);
			s.append("</html>");
			panel.getExplanation().setText(s.toString());
		}
	}
}
