package org.processmining.plugins.inductiveVisualMiner.popup.items;

import java.util.Collections;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveActivities implements PopupItemLogMove {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_info_filtered };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLogMove> input) {
		IvMLogInfo info = inputs.get(IvMObject.aligned_log_info_filtered);

		LogMovePosition position = input.get().getPosition();
		MultiSet<XEventClass> logMoves = info.getLogMoves().get(position);
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
	}

}
