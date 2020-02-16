package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveActivities implements PopupItemLogMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputLogMove> input) {
		if (state.isAlignmentReady() && state.getIvMLogFiltered() != null) {
			return nothing;
		} else {
			return nothing;
		}
	}

	//int maxNumberOfLogMoves = 10;
	//	//get digits of the maximum cardinality
	//	long max = logMoves.getCardinalityOf(logMoves.getElementWithHighestCardinality());
	//	int maxDigits = (int) (Math.log10(max) + 1);
	//
	//	if (max == 0) {
	//		panel.getGraph().setShowPopup(false, 10);
	//	}
	//
	//	List<XEventClass> activities = logMoves.sortByCardinality();
	//	Collections.reverse(activities);
	//	for (XEventClass activity : activities) {
	//		if (maxNumberOfLogMoves > 0) {
	//			popup.add(String.format("%" + maxDigits + "d", logMoves.getCardinalityOf(activity)) + " "
	//					+ StringUtils.abbreviate(activity.toString(), 40 - maxDigits));
	//		}
	//		maxNumberOfLogMoves--;
	//	}
	//	if (maxNumberOfLogMoves < 0) {
	//		popup.add("... and " + Math.abs(maxNumberOfLogMoves) + " more "
	//				+ (Math.abs(maxNumberOfLogMoves) > 1 ? "activities" : "activity") + " ");
	//	}

}
