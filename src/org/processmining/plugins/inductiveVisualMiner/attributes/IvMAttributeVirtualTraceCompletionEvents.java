package org.processmining.plugins.inductiveVisualMiner.attributes;

import java.util.Collection;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

public class IvMAttributeVirtualTraceCompletionEvents extends AttributeVirtualTraceNumericAbstract {

	@Override
	public String getName() {
		return "number of completion events";
	}

	public double getNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			int result = 0;
			for (IvMMove move : (IvMTrace) x) {
				if (move.hasAttributes() && move.isComplete()) {
					result++;
				}
			}
			return result;
		}
		if (x instanceof Collection<?>) {
			return ((Collection<?>) x).size();
		}
		return -Double.MAX_VALUE;
	}

}
