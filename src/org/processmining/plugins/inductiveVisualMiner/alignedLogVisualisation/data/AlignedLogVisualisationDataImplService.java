package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;

public class AlignedLogVisualisationDataImplService extends AlignedLogVisualisationDataImplSojourn {

	public AlignedLogVisualisationDataImplService(ProcessTree tree, PerformanceWrapper queueLengths,
			IvMLogInfo logInfo) {
		super(tree, queueLengths, logInfo);
	}

	@Override
	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long length = Math.round(queueLengths.getServiceTime(unode));
		if (length >= 0) {
			return Pair.of(Performance.timeToString(length), length);
		} else {
			return Pair.of("-", -1l);
		}
	}

}
