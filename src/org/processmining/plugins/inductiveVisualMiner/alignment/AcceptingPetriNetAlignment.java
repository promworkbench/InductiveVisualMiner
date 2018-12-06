package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import gnu.trove.map.TObjectIntMap;
import nl.tue.astar.AStarException;

public class AcceptingPetriNetAlignment {
	public static void align(Dfg dfg,
			Sextuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>> p,
			XLog log, IvMEventClasses eventClasses, AcceptingPetriNetAlignmentCallback callback,
			final ProMCanceller canceller) throws AStarException {

		AcceptingPetriNet aNet = p.getA();

		//create mapping transition -> eventclass
		XEventClass dummy = new XEventClass("", 1);
		TransEvClassMapping mapping;
		{
			mapping = new TransEvClassMapping(eventClasses.getClassifier(), dummy);

			for (Transition t : aNet.getNet().getTransitions()) {
				if (t.isInvisible()) {
					mapping.put(t, dummy);
				} else {
					mapping.put(t, eventClasses.getByIdentity(t.getLabel()));
				}
			}
		}

		PNLogReplayer replayer = new PNLogReplayer();
		CostBasedCompleteParam replayParameters = new CostBasedCompleteParam(eventClasses.getClasses(), dummy,
				aNet.getNet().getTransitions(), 1, 1);
		replayParameters.setInitialMarking(aNet.getInitialMarking());
		replayParameters.setMaxNumOfStates(Integer.MAX_VALUE);
		IPNReplayAlgorithm algorithm = new PetrinetReplayerWithILP();
		Marking[] finalMarkings = new Marking[aNet.getFinalMarkings().size()];
		replayParameters.setFinalMarkings(aNet.getFinalMarkings().toArray(finalMarkings));
		replayParameters.setCreateConn(false);
		replayParameters.setGUIMode(false);

		PNRepResult result = replayer.replayLog(null, aNet.getNet(), log, mapping, algorithm, replayParameters);

		for (SyncReplayResult aTrace : result) {
			callback.traceAlignmentComplete(aTrace, aTrace.getTraceIndex(), eventClasses);
		}
	}

	public static void addAllLeavesAsPerformanceEventClasses(IvMEventClasses eventClasses, AcceptingPetriNet net) {
		for (Transition t : net.getNet().getTransitions()) {
			if (!t.isInvisible()) {
				eventClasses.register(t.getLabel());
			}
		}
		eventClasses.harmonizeIndices();
	}

	public static void addAllLeavesAsEventClasses(IvMEventClasses eventClasses, Dfg dfg) {
		for (XEventClass a : dfg.getActivities()) {
			eventClasses.register(a.getId());
		}
		eventClasses.harmonizeIndices();
	}
}