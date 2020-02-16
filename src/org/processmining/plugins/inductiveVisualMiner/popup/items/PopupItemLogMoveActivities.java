package org.processmining.plugins.inductiveVisualMiner.popup.items;

import java.util.Collections;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveActivities implements PopupItemLogMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLogMove> input) {
		if (state.isAlignmentReady()) {
			MultiSet<XEventClass> logMoves = input.get().getLogMoves();
			int maxNumberOfLogMoves = 10;

			String[][] result = new String[maxNumberOfLogMoves + 1][];

			//get digits of the maximum cardinality
			long max = logMoves.getCardinalityOf(logMoves.getElementWithHighestCardinality());
			int maxDigits = (int) (Math.log10(max) + 1);

			List<XEventClass> activities = logMoves.sortByCardinality();
			Collections.reverse(activities);
			for (int i = 0; i < maxNumberOfLogMoves; i++) {
				if (i < activities.size()) {
					XEventClass activity = activities.get(i);
					result[i] = new String[] { //
							activity.toString(), //
							String.format("%" + maxDigits + "d", logMoves.getCardinalityOf(activity))//
					};
				}
			}

			if (activities.size() > maxNumberOfLogMoves) {
				int left = activities.size() - maxNumberOfLogMoves;
				result[maxNumberOfLogMoves] = new String[] {
						"... and " + left + " more " + (left > 1 ? "activities" : "activity") };
			}

			return result;
		} else {
			return nothing;
		}
	}

}
