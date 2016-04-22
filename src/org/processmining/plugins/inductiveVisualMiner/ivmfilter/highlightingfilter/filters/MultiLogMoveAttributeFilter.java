package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class MultiLogMoveAttributeFilter extends MultiEventAttributeFilter {

	@Override
	public String getName() {
		return "Log move attribute filter";
	}

	@Override
	public boolean countInColouring(IvMTrace trace) {
		String key = panel.getSelectedKey();
		for (IvMMove event : trace) {
			if (event.isLogMove() && event.getAttributes() != null && event.getAttributes().containsKey(key)
					&& panel.getSelectedAttributes().contains(event.getAttributes().get(key))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void updateExplanation() {
		if (panel.getSelectedAttributes().isEmpty()) {
			panel.getExplanation().setText(
					"<html>Include only traces that have at least one log move having an attribute as selected.</html>");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("<html>Include only traces that have at least one log move having attribute `");
			s.append(panel.getSelectedKey());
			s.append("' being ");
			List<XAttribute> attributes = panel.getSelectedAttributes();
			if (attributes.size() > 1) {
				s.append("either ");
			}
			for (int i = 0; i < attributes.size(); i++) {
				s.append("`");
				s.append(attributes.get(i));
				s.append("'");
				if (i == attributes.size() - 2) {
					s.append(" or ");
				} else if (i < attributes.size() - 1) {
					s.append(", ");
				}
			}
			s.append("</html>");
			panel.getExplanation().setText(s.toString());
		}
	}
}
