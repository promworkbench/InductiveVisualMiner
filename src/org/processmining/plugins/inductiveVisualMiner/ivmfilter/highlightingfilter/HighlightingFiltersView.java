package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersView;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class HighlightingFiltersView extends IvMFiltersView {

	private static final long serialVersionUID = -3014513707228444822L;

	public HighlightingFiltersView(Component parent) {
		super(parent, "highlighting filters - Inductive visual Miner");
	}

	/**
	 * Tell the user which traces are being coloured/selected
	 * 
	 * @param panel
	 * @param selectedNodes
	 * @param selectedLogMoves
	 * @param list
	 * @param maxAnimatedTraces
	 * @param numberOfTraces
	 */
	public static void updateSelectionDescription(InductiveVisualMinerPanel panel, Selection selection,
			List<IvMFilter> list) {
		//show the user which traces are shown

		StringBuilder result = new StringBuilder();

		//selected nodes
		if (selection.isAnActivitySelected()) {
			result.append("include ");
			Iterator<UnfoldedNode> it = selection.getSelectedActivities().iterator();
			result.append("'" + it.next().getNode() + "'");

			while (it.hasNext()) {
				String p = it.next().getNode().toString();
				if (it.hasNext()) {
					result.append(", `" + p + "'");
				} else {
					result.append(" or `" + p + "'");
				}
			}
			result.append(" in sync with the model");
		}

		//selected log moves
		if (selection.isALogMoveSelected()) {
			if (result.length() != 0) {
				result.append(" or ");
			}
			result.append("have an only-in-log event as selected");
		}

		//selected model moves
		if (selection.isAModelMoveSelected()) {
			if (result.length() != 0) {
				result.append(" or ");
			}
			result.append("have an only-in-model event as selected");
		}

		//selected edges
		if (selection.isAModelEdgeSelected()) {
			if (result.length() != 0) {
				result.append(" or ");
			}
			result.append("pass an edge as selected");
		}

		//colouring filters
		{
			int enabledColouringFilters = 0;
			for (IvMFilter colouringFilter : list) {
				if (colouringFilter.isEnabledFilter()) {
					enabledColouringFilters++;
				}
			}
			if (enabledColouringFilters >= 1) {
				if (result.length() != 0) {
					result.append("; and ");
				}

				if (enabledColouringFilters == 1) {
					result.append("pass the highlighting filter");
				} else {
					result.append("pass the highlighting filters");
				}
			}
		}

		//construct a sentence
		String s;
		if (result.length() == 0) {
			//no criteria active
			s = "Highlighting all traces.";
		} else {
			//all criteria active
			s = "Highlighting traces that " + result.toString() + ".";
		}

		panel.getSelectionLabel().setText(s);
	}
}
