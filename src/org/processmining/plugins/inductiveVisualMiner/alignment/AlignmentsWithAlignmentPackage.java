package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.concurrent.ExecutionException;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import nl.tue.alignment.Progress;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class AlignmentsWithAlignmentPackage {
	private static final boolean moveSort = true;
	private static final boolean useInt = false;
	private static final boolean partialOrder = false;
	private static final boolean preferExact = true;
	private static final boolean queueSort = true;
	private static final int threads = Math.max(Runtime.getRuntime().availableProcessors() - 1, 1);
	private static final int costUpperBound = Integer.MAX_VALUE;
	private static final int maxNumberOfStates = Integer.MAX_VALUE;
	private static final int timeout = Integer.MAX_VALUE;
	private static final Debug debug = Debug.NONE;

	public static void align(AcceptingPetriNet aNet, XLog log, IvMEventClasses eventClasses,
			AcceptingPetriNetAlignmentCallback callback, final ProMCanceller canceller)
			throws InterruptedException, ExecutionException {

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

		ReplayerParameters parameters = new ReplayerParameters.AStar(moveSort, queueSort, preferExact, threads, useInt,
				debug, timeout, maxNumberOfStates, costUpperBound, partialOrder);

		Petrinet net = aNet.getNet();
		Marking initialMarking = aNet.getInitialMarking();
		Marking finalMarking = aNet.getFinalMarkings().iterator().next();
		Replayer replayer = new Replayer(parameters, net, initialMarking, finalMarking, eventClasses, mapping, true);

		PNRepResult result = replayer.computePNRepResult(Progress.INVISIBLE, log);

		for (SyncReplayResult aTrace : result) {
			callback.traceAlignmentComplete(aTrace, aTrace.getTraceIndex(), eventClasses);
		}
	}
}