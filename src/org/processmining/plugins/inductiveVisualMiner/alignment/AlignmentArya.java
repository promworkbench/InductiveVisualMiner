package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import nl.tue.astar.AStarException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.inductiveVisualMiner.alignment.Move.Type;
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
	public static AlignmentResult alignTree(ProcessTree tree, XEventClassifier classifier, Collection<XEventClass> activities,
			PluginContext context, XLog log) {

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
				mapping.put(t, mapTransitionToEventClass(t, activities, mapTransition2Path));
			}
		}

		PNLogReplayer replayer = new PNLogReplayer();
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
		AlignedLog alignedLog = constructLogFromAlignment(replayed, mapTransition2Path);
		AlignedLogInfo alignedLogInfo = new AlignedLogInfo(alignedLog);

		System.out.println("Arya alignment done,  took " + String.format("%15d", System.nanoTime() - start) + ", fitness "
				+ replayed.getInfo().get(PNRepResult.TRACEFITNESS));

		return new AlignmentResult(alignedLog, alignedLogInfo);
	}

	private static XEventClass mapTransitionToEventClass(Transition t, Iterable<XEventClass> activities,
			Map<Transition, UnfoldedNode> mapTransition2Path) {
		//find the event class with the same label as the transition
		for (XEventClass activity : activities) {
			UnfoldedNode unode = mapTransition2Path.get(t);
			if (unode.getNode().getName().equals(activity.toString())) {
				return activity;
			}
		}
		return null;
	}

	public static AlignedLog constructLogFromAlignment(PNRepResult replayed,
			Map<Transition, UnfoldedNode> mapTransition2Path) {

		//construct aligned log
		AlignedLog alignedLog = new AlignedLog();
		for (SyncReplayResult r : replayed) {
			AlignedTrace trace = new AlignedTrace();
			Iterator<Object> itEvent = r.getNodeInstance().iterator();
			Iterator<StepTypes> itMoveType = r.getStepTypes().iterator();
			long cardinality = r.getTraceIndex().size();
			while (itEvent.hasNext()) {
				Object event = itEvent.next();
				StepTypes moveType = itMoveType.next();
				if ((moveType == StepTypes.MINVI || moveType == StepTypes.LMGOOD) && event instanceof Transition) {
					//synchronous move
					trace.add(new Move(Type.synchronous, mapTransition2Path.get(event), null));
				} else if (moveType == StepTypes.L && event instanceof XEventClass) {
					//log move
					trace.add(new Move(Type.log, null, (XEventClass) event));
				} else if (moveType == StepTypes.MREAL && event instanceof Transition) {
					//model move
					trace.add(new Move(Type.model, mapTransition2Path.get(event), null));
				} else {
					System.out.println("unknown move " + moveType + " " + event);
					trace.add(new Move(null, null, null));
				}
			}
			alignedLog.add(trace, cardinality);
		}

		return alignedLog;
	}
}
