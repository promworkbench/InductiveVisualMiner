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

public class AlignedLogVisualisationDataImplRelativeFrequencies implements AlignedLogVisualisationData {

	private IvMModel model;
	private IvMLogInfo logInfo;

	public AlignedLogVisualisationDataImplRelativeFrequencies(IvMModel model, IvMLogInfo logInfo) {
		this.model = model;
		this.logInfo = logInfo;
	}

	public Pair<Long, Long> getExtremeCardinalities() throws UnknownTreeNodeException {
		return IvMLogMetrics.getExtremes(model, logInfo);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, unode, includeModelMoves, logInfo);
		
		double ratio = cardinality / (logInfo.getNumberOfTraces() * 1.0);
		return Triple.of(format(ratio), cardinality, 0l);
	}

	public Pair<String, Long> getEdgeLabel(int unode, boolean includeModelMoves) throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, unode, includeModelMoves, logInfo);

		double ratio = cardinality / (logInfo.getNumberOfTraces() * 1.0);
		return Pair.of(format(ratio), cardinality);
	}

	public Pair<String, Long> getEdgeLabel(int from, int to, boolean includeModelMoves)
			throws UnknownTreeNodeException {
		long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(model, from, to, includeModelMoves, logInfo);
		
		double ratio = cardinality / (logInfo.getNumberOfTraces() * 1.0);
		return Pair.of(format(ratio), cardinality);
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

	public AlignedLogVisualisationDataImplRelativeFrequencies clone() throws CloneNotSupportedException {
		AlignedLogVisualisationDataImplRelativeFrequencies c = (AlignedLogVisualisationDataImplRelativeFrequencies) super.clone();

		c.logInfo = this.logInfo;
		c.model = this.model;

		return c;
	}
	
	public static String format(double value) {
		return String.format("%.3f", value);
	}
}
