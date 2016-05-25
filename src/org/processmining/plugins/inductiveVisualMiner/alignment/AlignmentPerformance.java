package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignmentPerformance {

	public static IvMLogNotFiltered align(IvMEfficientTree tree, XEventPerformanceClassifier performanceClassifier,
			XLog xLog, XEventClasses activityEventClasses, XEventClasses performanceEventClasses,
			IvMCanceller canceller) throws Exception {

		//the event classes are not thread-safe; copy them
		IvMEventClasses activityEventClasses2 = new IvMEventClasses(activityEventClasses);
		IvMEventClasses performanceEventClasses2 = new IvMEventClasses(performanceEventClasses);
		
		//transform tree for performance measurement
		Triple<ProcessTree, Map<UnfoldedNode, UnfoldedNode>, Set<UnfoldedNode>> t = ExpandProcessTree
				.expand(tree.getDTree());
		IvMEfficientTree performanceTree = new IvMEfficientTree(t.getA());
		Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping = t.getB(); //mapping performance node -> original node
		Set<UnfoldedNode> enqueueTaus = t.getC(); //set of taus involved in enqueueing

		//create mapping int->(performance)unfoldedNode
		List<UnfoldedNode> l = TreeUtils.unfoldAllNodes(new UnfoldedNode(performanceTree.getDTree().getRoot()));
		UnfoldedNode[] nodeId2performanceNode = l.toArray(new UnfoldedNode[l.size()]);

		ETMAlignment.addAllLeavesAsEventClasses(performanceEventClasses2, performanceTree.getDTree());

		AlignmentCallbackImpl callback = new AlignmentCallbackImpl(tree, performanceTree, xLog,
				activityEventClasses2, performanceNodeMapping, performanceEventClasses2, nodeId2performanceNode,
				enqueueTaus);
		ETMAlignment alignment = new ETMAlignment(performanceTree.getDTree(), xLog, performanceEventClasses2, callback, canceller);
		alignment.alignLog();

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

}
