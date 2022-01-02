package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceBooleanAbstract;

public class VirtualAttributeTraceHasDeviations extends AttributeVirtualTraceBooleanAbstract {

	public String getName() {
		return "has deviations";
	}

	public Boolean getBoolean(XAttributable x) {
		if (x instanceof IvMTrace) {
			for (IvMMove move : (IvMTrace) x) {
				if (!move.isSyncMove() && !move.isIgnoredLogMove() && !move.isIgnoredModelMove()) {
					return true;
				}
			}
			return false;
		} else {
			return null;
		}
	}
}