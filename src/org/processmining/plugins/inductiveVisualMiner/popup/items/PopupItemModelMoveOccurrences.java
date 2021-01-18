package org.processmining.plugins.inductiveVisualMiner.popup.items;

import org.apache.commons.lang3.StringUtils;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;

public class PopupItemModelMoveOccurrences implements PopupItemModelMove {
	public IvMObject<?>[] inputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info_filtered };
	}

	public String[][] get(IvMObjectValues inputs, PopupItemInput<PopupItemInputModelMove> input) {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo info = inputs.get(IvMObject.aligned_log_info_filtered);

		int unode = input.get().getUnode();
		long t = IvMLogMetrics.getModelMovesLocal(unode, info);
		return new String[][] { //
				{ (t > 1 ? (t + " times") : "Once") + ", activity " }, //
				{ StringUtils.abbreviate(model.getActivityName(unode), 40) }, //
				{ "was not executed." } //
		};
	}

}
