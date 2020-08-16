package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

public class VirtualAttributeTraceNumberOfLogMoves extends AttributeVirtualTraceNumericAbstract {

	@Override
	public String getName() {
		return "number of log moves";
	}

	public double getNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			int result = 0;
			for (IvMMove move : (IvMTrace) x) {
				if (move.isLogMove() && !move.isIgnoredLogMove()) {
					result++;
				}
			}
			return result;
		}
		return -Double.MAX_VALUE;
	}

}
