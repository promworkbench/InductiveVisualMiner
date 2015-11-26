package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tue.astar.AStarThread.Canceller;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.ExpandProcessTreeForQueues;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignmentPerformance {

	public static IvMLogNotFiltered align(ProcessTree tree, XEventPerformanceClassifier performanceClassifier,
			XLog xLog, IMLog log, XEventClasses activityEventClasses, XEventClasses performanceEventClasses,
			Canceller canceller) throws Exception {

		//transform tree for performance measurement
		Triple<ProcessTree, Map<UnfoldedNode, UnfoldedNode>, Set<UnfoldedNode>> t = ExpandProcessTreeForQueues
				.expand(tree);
		ProcessTree performanceTree = t.getA();
		Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping = t.getB(); //mapping performance node -> original node
		Set<UnfoldedNode> enqueueTaus = t.getC(); //set of taus involved in enqueueing

		//create mapping int->(performance)unfoldedNode
		List<UnfoldedNode> l = TreeUtils.unfoldAllNodes(new UnfoldedNode(performanceTree.getRoot()));
		UnfoldedNode[] nodeId2performanceNode = l.toArray(new UnfoldedNode[l.size()]);

		Alignment.addAllLeavesAsEventClasses(performanceEventClasses, performanceTree);

		AlignmentResultImplPerformance callback = new AlignmentResultImplPerformance(xLog, activityEventClasses,
				performanceNodeMapping, performanceEventClasses, nodeId2performanceNode, enqueueTaus);
		Alignment alignment = new Alignment(performanceTree, xLog, performanceEventClasses, callback, canceller);
		alignment.alignLog();

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

}
