package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;

public class PopupItemModelMoveOccurrences implements PopupItemModelMove {

	public String[][] get(InductiveVisualMinerState state, PopupItemInput<PopupItemInputModelMove> input) {
		if (state.isAlignmentReady()) {
			int unode = input.get().getUnode();
			long t = IvMLogMetrics.getModelMovesLocal(unode, state.getIvMLogInfoFiltered());
			return new String[][] { //
					{ (t > 1 ? (t + " times") : "once") + ", activity " }, //
					{ StringUtils.abbreviate(state.getModel().getActivityName(unode), 40) }, //
					{ "was not executed." } //
			};
		} else {
			return nothing;
		}
	}

}
