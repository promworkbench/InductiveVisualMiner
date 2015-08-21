package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class QueueLengthsImplUPStartComplete extends QueueLengths {

	public double getQueueProbability(UnfoldedNode unode, QueueActivityLog l, long time, int traceIndex) {
		if (l.getInitiate(traceIndex) > 0 && l.getStart(traceIndex) > 0 && l.getInitiate(traceIndex) <= time
				&& time <= l.getStart(traceIndex)) {
			return 1/2.0;
		}
		return 0;
	}
	
	public String getName() {
		return "UP start complete";
	}

}
