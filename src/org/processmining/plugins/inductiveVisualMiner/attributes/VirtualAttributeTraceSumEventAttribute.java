package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;
import org.processmining.plugins.InductiveMiner.mining.logs.IMTrace;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeImpl;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

public class VirtualAttributeTraceSumEventAttribute extends AttributeVirtualTraceNumericAbstract {

	private final AttributeImpl attribute;

	public VirtualAttributeTraceSumEventAttribute(AttributeImpl attribute) {
		this.attribute = attribute;
	}

	@Override
	public String getName() {
		return "trace sum of event attribute " + attribute.getName();
	}

	@Override
	public double getNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			double result = 0;
			for (IvMMove move : (IvMTrace) x) {
				double value = attribute.getNumeric(move);
				if (value != -Double.MAX_VALUE) {
					result += value;
				}
			}
			return result;
		}
		if (x instanceof IMTrace) {
			double result = 0;
			for (XEvent event : (IMTrace) x) {
				double value = attribute.getNumeric(event);
				if (value != -Double.MAX_VALUE) {
					result += value;
				}
			}
			return result;
		}
		return -Double.MAX_VALUE;
	}

}
