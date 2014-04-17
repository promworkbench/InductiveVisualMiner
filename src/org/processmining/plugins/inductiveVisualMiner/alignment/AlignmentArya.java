package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.tue.astar.AStarException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.InvalidProcessTreeException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.NotYetImplementedException;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class AlignmentArya {
	public static AlignmentResult alignTree(ProcessTree tree, XEventClassifier classifier, IMLogInfo logInfo,
			PluginContext context, XLog log, Set<XEventClass> skipActivities) {

		long start = System.nanoTime();

		//convert tree to Petri net
		PetrinetWithMarkings pnwm;
		try {
			pnwm = ProcessTree2Petrinet.convertKeepStructure(tree);
		} catch (NotYetImplementedException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidProcessTreeException e) {
			e.printStackTrace();
			return null;
		}
		Petrinet petrinet = pnwm.petrinet;
		Marking initialMarking = pnwm.initialMarking;
		Marking finalMarking = pnwm.finalMarking;
		Map<Transition, UnfoldedNode> mapTransition2Path = pnwm.mapTransition2Path;

		//create mapping transition -> eventclass
		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping = new TransEvClassMapping(classifier, dummy);
		for (Transition t : petrinet.getTransitions()) {
			if (t.isInvisible()) {
				mapping.put(t, dummy);
			} else {
				mapping.put(t, mapTransitionToEventClass(t, logInfo, mapTransition2Path));
			}
		}

		PNLogReplayer replayer = new PNLogReplayer();
		Collection<XEventClass> activities = logInfo.getActivities().toSet();
		CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(activities, dummy,
				petrinet.getTransitions(), 1, 1);
		replayParameters.setInitialMarking(initialMarking);
		replayParameters.setMaxNumOfStates(Integer.MAX_VALUE);
		IPNReplayAlgorithm algorithm = new PetrinetReplayerWithILP();
		replayParameters.setFinalMarkings(new Marking[] { finalMarking });
		replayParameters.setCreateConn(false);
		replayParameters.setGUIMode(false);

		PNRepResult replayed;
		try {
			replayed = replayer.replayLog(context, petrinet, log, mapping, algorithm, replayParameters);
		} catch (AStarException e) {
			e.printStackTrace();
			return null;
		}

		//construct log and loginfo
		AlignedLog alignedLog = constructLogFromAlignment(replayed, mapTransition2Path, skipActivities);
		AlignedLogInfo alignedLogInfo = new AlignedLogInfo(alignedLog);

		System.out.println("Arya alignment done,  took " + String.format("%15d", System.nanoTime() - start) + ", fitness "
				+ replayed.getInfo().get(PNRepResult.TRACEFITNESS));

		return new AlignmentResult(alignedLog, alignedLogInfo);
	}

	private static XEventClass mapTransitionToEventClass(Transition t, IMLogInfo logInfo,
			Map<Transition, UnfoldedNode> mapTransition2Path) {
		//find the event class with the same label as the transition
		for (XEventClass activity : logInfo.getActivities()) {
			UnfoldedNode unode = mapTransition2Path.get(t);
			if (unode.getNode().getName().equals(activity.toString())) {
				return activity;
			}
		}
		return null;
	}

	public static AlignedLog constructLogFromAlignment(PNRepResult replayed,
			Map<Transition, UnfoldedNode> mapTransition2Path, Set<XEventClass> skipActivities) {

		//construct aligned log
		AlignedLog alignedLog = new AlignedLog();
		for (SyncReplayResult r : replayed) {
			IMTraceG<Move> trace = new IMTraceG<Move>();
			Iterator<Object> itEvent = r.getNodeInstance().iterator();
			Iterator<StepTypes> itMoveType = r.getStepTypes().iterator();
			long cardinality = r.getTraceIndex().size();
			while (itEvent.hasNext()) {
				Object event = itEvent.next();
				StepTypes moveType = itMoveType.next();
				if (!skipActivities.contains(event)) {
					trace.add(new Move(moveType, event, mapTransition2Path));
				}
			}
			alignedLog.add(trace, cardinality);
		}

		return alignedLog;
	}
}
