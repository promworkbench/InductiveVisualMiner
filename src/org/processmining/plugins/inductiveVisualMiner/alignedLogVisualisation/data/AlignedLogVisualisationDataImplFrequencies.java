package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;

public class AlignedLogVisualisationDataImplFrequencies implements AlignedLogVisualisationData {

	private IvMModel model;
	private IvMLogInfo logInfo;

	public AlignedLogVisualisationDataImplFrequencies(IvMModel model, IvMLogInfo logInfo) {
		this.model = model;
		this.logInfo = logInfo;
	}

	public Pair<Long, Long> getExtremeCardinalities() throws UnknownTreeNodeException {
		return IvMLogMetrics.getExtremes(model, logInfo);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, unode, includeModelMoves, logInfo);
		return Triple.of(String.valueOf(cardinality), cardinality, 0l);
	}

	public Pair<String, Long> getEdgeLabel(int unode, boolean includeModelMoves) throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, unode, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, Long> getEdgeLabel(int from, int to, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, from, to, includeModelMoves, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(int unode) {
		long cardinality = IvMLogMetrics.getModelMovesLocal(unode, logInfo);
		return Pair.of(String.valueOf(cardinality), cardinality);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		MultiSet<XEventClass> cardinality = IvMLogMetrics.getLogMoves(logMovePosition, logInfo);
		return Pair.of(String.valueOf(cardinality.size()), cardinality);
	}

	public void setTime(long time) {

	}

	public AlignedLogVisualisationDataImplFrequencies clone() throws CloneNotSupportedException {
		AlignedLogVisualisationDataImplFrequencies c = (AlignedLogVisualisationDataImplFrequencies) super.clone();

		c.logInfo = this.logInfo;
		c.model = this.model;

		return c;
	}
}
