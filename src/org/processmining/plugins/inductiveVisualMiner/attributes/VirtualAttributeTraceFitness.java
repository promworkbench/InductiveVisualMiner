package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.deckfour.xes.model.XAttributable;
import org.processmining.plugins.inductiveVisualMiner.alignment.Fitness;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualTraceNumericAbstract;

public class VirtualAttributeTraceFitness extends AttributeVirtualTraceNumericAbstract {

	public String getName() {
		return "fitness";
	}

	public double getNumeric(XAttributable x) {
		if (x instanceof IvMTrace) {
			return Fitness.compute((IvMTrace) x);
		} else {
			return -Double.MAX_VALUE;
		}
	}

}
