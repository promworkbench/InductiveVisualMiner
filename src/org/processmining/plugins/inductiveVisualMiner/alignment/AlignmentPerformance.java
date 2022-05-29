package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.TreeUtils;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;

public class AlignmentPerformance {

	public static IvMLogNotFiltered align(AlignmentComputer computer, IvMModel model,
			XEventPerformanceClassifier performanceClassifier, XLog xLog, XEventClasses activityEventClasses,
			XEventClasses performanceEventClasses, ProMCanceller canceller, IvMDecoratorI decorator) throws Exception {
		if (model.isTree()) {
			return alignTree(computer, model, performanceClassifier, xLog, activityEventClasses,
					performanceEventClasses, canceller, decorator);
		} else if (model.isDfg()) {
			return alignDfg(computer, model, performanceClassifier, xLog, activityEventClasses, performanceEventClasses,
					canceller, decorator);
		} else if (model.isNet()) {
			return alignNet(computer, model, performanceClassifier, xLog, activityEventClasses, performanceEventClasses,
					canceller, decorator);
		}
		throw new RuntimeException("model not supported");
	}

	public static IvMLogNotFiltered alignTree(AlignmentComputer computer, IvMModel model,
			XEventPerformanceClassifier performanceClassifier, XLog xLog, XEventClasses activityEventClasses,
			XEventClasses performanceEventClasses, ProMCanceller canceller, IvMDecoratorI decorator) throws Exception {

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

		if (!canceller.isCancelled()) {
			return computer.computeProcessTree(model, xLog, canceller, activityEventClasses2, performanceEventClasses2,
					performanceTree, performanceNodeMapping, enqueueTaus, nodeId2performanceNode, decorator);
		} else {
			return null;
		}
	}

	public static IvMLogNotFiltered alignDfg(AlignmentComputer computer, IvMModel model,
			XEventPerformanceClassifier performanceClassifier, XLog xLog, XEventClasses activityEventClasses,
			XEventClasses performanceEventClasses, ProMCanceller canceller, IvMDecoratorI decorator) throws Exception {

		//the event classes are not thread-safe; copy them
		IvMEventClasses activityEventClasses2 = new IvMEventClasses(activityEventClasses);
		IvMEventClasses performanceEventClasses2 = new IvMEventClasses(performanceEventClasses);

		//make a Petri net to align
		Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p = Dfm2AcceptingPetriNet
				.convertForPerformance(model.getDfg());

		AcceptingPetriNetAlignment.addAllLeavesAsEventClasses(activityEventClasses2, model.getDfg());
		AcceptingPetriNetAlignment.addAllLeavesAsPerformanceEventClasses(performanceEventClasses2, p.getA());

		if (!canceller.isCancelled()) {
			return computer.computeDfgAcceptingPetriNet(model, xLog, canceller, activityEventClasses2,
					performanceEventClasses2, p, decorator);
		} else {
			return null;
		}
	}

	public static IvMLogNotFiltered alignNet(AlignmentComputer computer, IvMModel model,
			XEventPerformanceClassifier performanceClassifier, XLog xLog, XEventClasses activityEventClasses,
			XEventClasses performanceEventClasses, ProMCanceller canceller, IvMDecoratorI decorator) throws Exception {
		//the event classes are not thread-safe; copy them
		IvMEventClasses activityEventClasses2 = new IvMEventClasses(activityEventClasses);
		IvMEventClasses performanceEventClasses2 = new IvMEventClasses(performanceEventClasses);

		//make a Petri net to align
		Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, TObjectIntMap<Transition>> p = AcceptingPetriNet2AcceptingPetriNetPerformance
				.convertForPerformance(model);

		AcceptingPetriNetAlignment.addAllLeavesAsEventClasses(activityEventClasses2, model.getNet());
		AcceptingPetriNetAlignment.addAllLeavesAsPerformanceEventClasses(performanceEventClasses2, p.getA());

		if (!canceller.isCancelled()) {
			return computer.computeAcceptingPetriNet(model, xLog, canceller, activityEventClasses2,
					performanceEventClasses2, p, decorator);
		} else {
			return null;
		}
	}
}