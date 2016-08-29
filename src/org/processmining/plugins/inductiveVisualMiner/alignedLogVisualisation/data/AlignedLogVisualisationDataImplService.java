package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;

public class AlignedLogVisualisationDataImplService extends AlignedLogVisualisationDataImplSojourn {

	public AlignedLogVisualisationDataImplService(IvMEfficientTree tree, PerformanceWrapper queueLengths,
			IvMLogInfo logInfo) {
		super(tree, queueLengths, logInfo);
	}

	@Override
	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		long length = Math.round(queueLengths.getServiceTime(unode));
		if (length >= 0) {
			return Triple.of(Performance.timeToString(length), length, length);
		} else {
			return Triple.of("-", -1l, -1l);
		}
	}

}
