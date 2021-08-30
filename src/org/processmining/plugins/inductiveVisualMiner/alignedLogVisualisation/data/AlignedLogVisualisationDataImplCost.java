package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;

public class AlignedLogVisualisationDataImplCost implements AlignedLogVisualisationData {

	protected long minMeasure;
	protected long maxMeasure;

	protected CostModel costModel;

	protected AlignedLogVisualisationData dataForEdges;

	public AlignedLogVisualisationDataImplCost(IvMModel model, CostModel costModel, IvMLogInfo logInfo) {
		this.costModel = costModel;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(model, logInfo);

		computeExtremes(model, costModel);
	}

	protected void computeExtremes(IvMModel model, CostModel costModel) {
		//compute extreme sojourn times
		minMeasure = Long.MAX_VALUE;
		maxMeasure = Long.MIN_VALUE;
		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				long parameter = costModel.getNodeRepresentationModel(node).getA();
				minMeasure = Math.min(minMeasure, parameter);
				maxMeasure = Math.max(maxMeasure, parameter);
			}
		}
	}

	public void setTime(long time) {

	}

	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(minMeasure, maxMeasure);
	}

	public Triple<String, Long, Long> getNodeLabel(int unode, boolean includeModelMoves) {
		Pair<Long, String> p = costModel.getNodeRepresentationModel(unode);
		return Triple.of(p.getB(), p.getA(), p.getA());
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

	public AlignedLogVisualisationDataImplCost clone() throws CloneNotSupportedException {
		AlignedLogVisualisationDataImplCost c = (AlignedLogVisualisationDataImplCost) super.clone();

		c.dataForEdges = this.dataForEdges;
		c.maxMeasure = this.maxMeasure;
		c.minMeasure = this.minMeasure;
		c.costModel = this.costModel;

		return c;
	}
}
