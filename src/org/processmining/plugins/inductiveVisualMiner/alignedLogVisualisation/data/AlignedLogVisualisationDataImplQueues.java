package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Manual;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet.UnfoldedNode;


public class AlignedLogVisualisationDataImplQueues implements AlignedLogVisualisationData {

	private long minQueueLength;
	private long maxQueueLength;
	private TObjectDoubleMap<UnfoldedNode> computedLengths;
	
	private final ProcessTree tree;
	private final PerformanceWrapper queueLengths;
	
	private final AlignedLogVisualisationData dataForEdges;
	
	public AlignedLogVisualisationDataImplQueues(ProcessTree tree, PerformanceWrapper queueLengths, IvMLogInfo logInfo) {
		this.tree = tree;
		this.queueLengths = queueLengths;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(tree, logInfo);
		setTime(0);
	}
	
	public void setTime(long time) {
		//compute queue lengths
		computedLengths = new TObjectDoubleHashMap<UnfoldedNode>(10,
				0.5f, -1);
		minQueueLength = Long.MAX_VALUE;
		maxQueueLength = Long.MIN_VALUE;
		{
			for (UnfoldedNode unode : TreeUtils.unfoldAllNodes(new UnfoldedNode(tree.getRoot()))) {
				if (unode.getNode() instanceof Manual) {
					double l = queueLengths.getQueueLength(unode, time);
					computedLengths.put(unode, l);
					if (l > maxQueueLength) {
						maxQueueLength = (long) (l + 0.5);
					}
					if (l < minQueueLength && l > -0.1) {
						minQueueLength = (long) (l + 0.5);
					}
				}
			}
		}
	}
	
	public Pair<Long, Long> getExtremeCardinalities() {
		return Pair.of(minQueueLength, maxQueueLength);
	}

	public Pair<String, Long> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long length = Math.round(computedLengths.get(unode));
		if (length >= 0) {
			return Pair.of(String.valueOf(length), length);
		} else {
			return Pair.of("-", -1l);
		}
	}

	public Pair<String, Long> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Pair<String, Long> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Pair<String, MultiSet<XEventClass>> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}



}
