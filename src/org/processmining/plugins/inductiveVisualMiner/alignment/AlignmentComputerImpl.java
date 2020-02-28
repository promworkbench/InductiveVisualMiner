package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.AStarException;

public class AlignmentComputerImpl implements AlignmentComputer {

	public IvMLogNotFiltered computeAcceptingPetriNet(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p)
			throws InterruptedException, ExecutionException, AStarException {
		AcceptingPetriNetAlignmentCallbackImpl callback = new AcceptingPetriNetAlignmentCallbackImpl(xLog, model,
				activityEventClasses2, p);
		//AlignmentsWithAlignmentPackageQualityInformed.align(p.getA(), xLog, performanceEventClasses2, callback,
		//		canceller);
		//AlignmentsWithAlignmentPackage.align(p.getA(), xLog, performanceEventClasses2, callback, canceller);
		AcceptingPetriNetAlignment.align(p.getA(), xLog, performanceEventClasses2, callback, canceller);

		if (!canceller.isCancelled()) {
			return callback.getAlignedLog();
		} else {
			return null;
		}
	}

	public IvMLogNotFiltered computeProcessTree(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			IvMEfficientTree performanceTree, Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping,
			Set<UnfoldedNode> enqueueTaus, UnfoldedNode[] nodeId2performanceNode) throws Exception {
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
