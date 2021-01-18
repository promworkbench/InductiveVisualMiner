package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;

public class PopupItemLogMoveTitle implements PopupItemLogMove {

	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log_info_filtered };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputLogMove> input) {
		IvMLogInfo info = inputs.get(IvMObject.aligned_log_info_filtered);

		LogMovePosition position = input.get().getPosition();
		MultiSet<XEventClass> logMoves = info.getLogMoves().get(position);

		return new String[][] {
				{ logMoves.size() + (logMoves.size() <= 1 ? " event" : " events") + " additional to the model:" } };
	}

}