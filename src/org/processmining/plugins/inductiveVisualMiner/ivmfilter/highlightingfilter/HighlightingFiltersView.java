package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter;

import java.awt.Component;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersController;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFiltersView;

import gnu.trove.iterator.TIntIterator;

public class HighlightingFiltersView extends IvMFiltersView {

	private static final long serialVersionUID = -3014513707228444822L;

	public HighlightingFiltersView(Component parent) {
		super(parent, "highlighting filters - " + InductiveVisualMinerPanel.title,
				"These filters alter the occurrence counts, performance measurements and animation.");
	}

	/**
	 * Tell the user which traces are being coloured/selected
	 * 
	 * @param panel
	 * @param selectedNodes
	 * @param selectedLogMoves
	 * @param filters
	 * @param maxAnimatedTraces
	 * @param numberOfTraces
	 */
	public static void updateSelectionDescription(InductiveVisualMinerPanel panel, Selection selection,
			IvMFiltersController filters, IvMModel model) {
		//show the user which traces are shown

		StringBuilder result = new StringBuilder();

		//selected nodes
		if (selection.isAnActivitySelected()) {
			result.append("include ");
			TIntIterator it = selection.getSelectedActivities().iterator();
			result.append("'" + model.getActivityName(it.next()) + "'");

			while (it.hasNext()) {
				String p = model.getActivityName(it.next());
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
			if (filters.isAHighlightingFilterEnabled()) {
				if (result.length() != 0) {
					result.append("; and ");
				}

				result.append("pass the highlighting filters");
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
