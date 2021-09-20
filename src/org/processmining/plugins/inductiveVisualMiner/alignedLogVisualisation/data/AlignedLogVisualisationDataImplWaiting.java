package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Aggregate;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceUtils;

public class AlignedLogVisualisationDataImplWaiting extends AlignedLogVisualisationDataImplSojourn {

	public AlignedLogVisualisationDataImplWaiting(IvMModel model, Performance performance, IvMLogInfo logInfo) {
		super(model, performance, logInfo);
	}

	@Override
	protected void computeExtremes(Performance performance) {
		//compute extreme average times
		minMeasure = Long.MAX_VALUE;
		maxMeasure = Long.MIN_VALUE;
		for (long d : performance.getNodeMeasures(DurationType.waiting, Aggregate.average)) {
			if (d >= 0 && d > maxMeasure) {
				maxMeasure = d;
			}
			if (d >= 0 && d < minMeasure) {
				minMeasure = d;
			}
		}
	}

	@Override
	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		long length = performance.getNodeMeasure(DurationType.waiting, Aggregate.average, unode);
		if (length > -1) {
			return Triple.of(PerformanceUtils.timeToString(length), length, length);
		} else {
			return Triple.of("-", -1l, -1l);
		}
	}

}
