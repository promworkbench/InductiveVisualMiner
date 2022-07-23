package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.AStarException;

@SuppressWarnings("deprecation")
public class AlignmentComputerImpl implements AlignmentComputer {

	public IvMLogNotFiltered computeProcessTree(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			IvMEfficientTree performanceTree, Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping,
			Set<UnfoldedNode> enqueueTaus, UnfoldedNode[] nodeId2performanceNode, IvMDecoratorI decorator)
			throws Exception {
//		ETMAlignmentCallbackImpl callback = new ETMAlignmentCallbackImpl(model, performanceTree, xLog,
//				activityEventClasses2, performanceNodeMapping, performanceEventClasses2, nodeId2performanceNode,
//				enqueueTaus, decorator);
//		ETMAlignment alignment = new ETMAlignment(performanceTree.getDTree(), xLog, performanceEventClasses2, callback,
//				canceller);
//		alignment.alignLog();

		//alternative: alignment via accepting Petri nets
		Triple<AcceptingPetriNet, TObjectIntMap<Transition>, Set<Transition>> p = EfficientTree2AcceptingPetriNetPerformance
				.convert(model.getTree());
		AcceptingPetriNetAlignmentCallbackImplEfficientTree callback = new AcceptingPetriNetAlignmentCallbackImplEfficientTree(
				xLog, model, activityEventClasses2, p, decorator);
		AcceptingPetriNetAlignment.align(p.getA(), xLog, performanceEventClasses2, callback, canceller);

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

	public IvMLogNotFiltered computeDfgAcceptingPetriNet(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p,
			IvMDecoratorI decorator) throws InterruptedException, ExecutionException, AStarException {
		AcceptingPetriNetAlignmentCallbackImplDfg callback = new AcceptingPetriNetAlignmentCallbackImplDfg(xLog, model,
				activityEventClasses2, p, decorator);
		AcceptingPetriNetAlignment.align(p.getA(), xLog, performanceEventClasses2, callback, canceller);

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

	public IvMLogNotFiltered computeAcceptingPetriNet(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			Quadruple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, TObjectIntMap<Transition>> p,
			IvMDecoratorI decorator) throws InterruptedException, ExecutionException, AStarException {
		AcceptingPetriNetAlignmentCallbackImplNet callback = new AcceptingPetriNetAlignmentCallbackImplNet(xLog, model,
				activityEventClasses2, p, decorator);
		AcceptingPetriNetAlignment.align(p.getA(), xLog, performanceEventClasses2, callback, canceller);

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

	public String getUniqueIdentifier() {
		return "default alignment";
	}

}
