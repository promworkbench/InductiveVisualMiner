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
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.AStarException;

public interface AlignmentComputer {

	public IvMLogNotFiltered computeAcceptingPetriNet(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> p,
			IvMDecoratorI decorator) throws InterruptedException, ExecutionException, AStarException;

	public IvMLogNotFiltered computeProcessTree(IvMModel model, XLog xLog, ProMCanceller canceller,
			IvMEventClasses activityEventClasses2, IvMEventClasses performanceEventClasses2,
			IvMEfficientTree performanceTree, Map<UnfoldedNode, UnfoldedNode> performanceNodeMapping,
			Set<UnfoldedNode> enqueueTaus, UnfoldedNode[] nodeId2performanceNode, IvMDecoratorI decorator)
			throws Exception;

	/**
	 * 
	 * @return A string that uniquely represent this alignment computer. Used
	 *         for caching purposes.
	 */
	public String getUniqueIdentifier();
}