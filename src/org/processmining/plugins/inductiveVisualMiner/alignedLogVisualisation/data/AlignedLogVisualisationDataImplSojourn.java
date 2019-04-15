package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;

public class AlignedLogVisualisationDataImplSojourn implements AlignedLogVisualisationData {

	protected long minMeasure;
	protected long maxMeasure;

	protected PerformanceWrapper performance;

	protected AlignedLogVisualisationData dataForEdges;

	public AlignedLogVisualisationDataImplSojourn(IvMModel model, PerformanceWrapper queueLengths, IvMLogInfo logInfo) {
		this.performance = queueLengths;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(model, logInfo);

		computeExtremes(queueLengths);
	}

	protected void computeExtremes(PerformanceWrapper performance) {
		//compute extreme sojourn times
		minMeasure = Long.MAX_VALUE;
		maxMeasure = Long.MIN_VALUE;
		for (long d : performance.getNodeMeasures(TypeNode.sojourn, Gather.average)) {
			if (d >= 0 && d > maxMeasure) {
				maxMeasure = d;
			}
			if (d >= 0 && d < minMeasure) {
				minMeasure = d;
			}
		}
	}

	public void setTime(long time) {

	}

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(minMeasure, maxMeasure);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		long length = performance.getNodeMeasure(TypeNode.sojourn, Gather.average, unode);
		if (length >= 0) {
			return Triple.of(Performance.timeToString(length), length, length);
		} else {
			return Triple.of("-", -1l, -1l);
		}
	}

	public Pair<String, Long> getEdgeLabel(int unode, boolean includeModelMoves) throws UnknownTreeNodeException {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Pair<String, Long> getEdgeLabel(int from, int to, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		return dataForEdges.getEdgeLabel(from, to, includeModelMoves);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(int unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}

	public AlignedLogVisualisationDataImplSojourn clone() throws CloneNotSupportedException {
		AlignedLogVisualisationDataImplSojourn c = (AlignedLogVisualisationDataImplSojourn) super.clone();

		c.dataForEdges = this.dataForEdges;
		c.maxMeasure = this.maxMeasure;
		c.minMeasure = this.minMeasure;
		c.performance = this.performance;

		return c;
	}

}
