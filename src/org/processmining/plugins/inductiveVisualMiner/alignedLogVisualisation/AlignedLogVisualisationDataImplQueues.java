package org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedLogInfo;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.performance.QueueLengthsWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task.Manual;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;


public class AlignedLogVisualisationDataImplQueues implements AlignedLogVisualisationData {

	private long time;
	private long minQueueLength;
	private long maxQueueLength;
	private TObjectDoubleMap<UnfoldedNode> computedLengths;
	
	private final ProcessTree tree;
	private final QueueLengthsWrapper queueLengths;
	
	private final AlignedLogVisualisationData dataForEdges;
	
	public AlignedLogVisualisationDataImplQueues(ProcessTree tree, QueueLengthsWrapper queueLengths, AlignedLogInfo logInfo) {
		this.tree = tree;
		this.queueLengths = queueLengths;
		dataForEdges = new AlignedLogVisualisationDataImplFrequencies(tree, logInfo);
		setTime(0);
	}
	
	public void setTime(long time) {
		//compute queue lengths
		computedLengths = new TObjectDoubleHashMap<ProcessTree2Petrinet.UnfoldedNode>(10,
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

	public Triple<String, Long, String> getNodeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		long length = Math.round(computedLengths.get(unode));
		if (length >= 0) {
			return Triple.of("queue length ", length, "");
		} else {
			return Triple.of("-", -1l, "");
		}
	}

	public Triple<String, Long, String> getEdgeLabel(UnfoldedNode unode, boolean includeModelMoves) {
		return dataForEdges.getEdgeLabel(unode, includeModelMoves);
	}

	public Triple<String, Long, String> getModelMoveEdgeLabel(UnfoldedNode unode) {
		return dataForEdges.getModelMoveEdgeLabel(unode);
	}

	public Triple<String, MultiSet<XEventClass>, String> getLogMoveEdgeLabel(LogMovePosition logMovePosition) {
		return dataForEdges.getLogMoveEdgeLabel(logMovePosition);
	}



}
