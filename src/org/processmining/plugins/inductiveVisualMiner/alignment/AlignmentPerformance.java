package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;

public class AlignmentPerformance {

	public static IvMLogNotFiltered align(IvMModel model, XEventPerformanceClassifier performanceClassifier, XLog xLog,
			XEventClasses activityEventClasses, XEventClasses performanceEventClasses, ProMCanceller canceller)
			throws Exception {
		if (model.isTree()) {
			return alignTree(model, performanceClassifier, xLog, activityEventClasses, performanceEventClasses,
					canceller);
		} else {
			return alignDfg(model, performanceClassifier, xLog, activityEventClasses, performanceEventClasses,
					canceller);
		}
	}

	public static IvMLogNotFiltered alignDfg(IvMModel model, XEventPerformanceClassifier performanceClassifier,
			XLog xLog, XEventClasses activityEventClasses, XEventClasses performanceEventClasses,
			ProMCanceller canceller) throws Exception {

		//the event classes are not thread-safe; copy them
		IvMEventClasses activityEventClasses2 = new IvMEventClasses(activityEventClasses);
		IvMEventClasses performanceEventClasses2 = new IvMEventClasses(performanceEventClasses);

		//make a Petri net to align
		Sextuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>> p = Dfg2AcceptingPetriNet
				.convertForPerformance(model.getDfg());

		AcceptingPetriNetAlignment.addAllLeavesAsEventClasses(activityEventClasses2, model.getDfg());
		AcceptingPetriNetAlignment.addAllLeavesAsPerformanceEventClasses(performanceEventClasses2, p.getA());
		AcceptingPetriNetAlignmentCallback callback = new AcceptingPetriNetAlignmentCallbackImpl(xLog, model,
				activityEventClasses2, p);
		AcceptingPetriNetAlignment.align(model.getDfg(), p, xLog, performanceEventClasses2, callback, canceller);

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

	public static IvMLogNotFiltered alignTree(IvMModel model, XEventPerformanceClassifier performanceClassifier,
			XLog xLog, XEventClasses activityEventClasses, XEventClasses performanceEventClasses,
			ProMCanceller canceller) throws Exception {

		IvMEfficientTree tree = model.getTree();

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

		ETMAlignmentCallbackImpl callback = new ETMAlignmentCallbackImpl(model, performanceTree, xLog,
				activityEventClasses2, performanceNodeMapping, performanceEventClasses2, nodeId2performanceNode,
				enqueueTaus);
		ETMAlignment alignment = new ETMAlignment(performanceTree.getDTree(), xLog, performanceEventClasses2, callback,
				canceller);
		alignment.alignLog();

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}
}
