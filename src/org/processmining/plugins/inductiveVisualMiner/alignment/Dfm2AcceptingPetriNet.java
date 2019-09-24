package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Septuple;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class Dfm2AcceptingPetriNet {
	public static AcceptingPetriNet convert(DirectlyFollowsModel dfg) {
		Petrinet petriNet = new PetrinetImpl("converted from Dfm");
		Place source = petriNet.addPlace("net source");
		Place sink = petriNet.addPlace("net sink");
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		/**
		 * empty traces
		 */
		if (dfg.getNumberOfNodes() > 0) {
			Transition epsilon = petriNet.addTransition("epsilon");
			epsilon.setInvisible(true);
			petriNet.addArc(source, epsilon);
			petriNet.addArc(epsilon, sink);
		}

		/**
		 * Activities (states)
		 */
		TIntObjectMap<Place> activity2place = new TIntObjectHashMap<>();
		for (int activity : dfg.getNodeIndices()) {
			Place place = petriNet.addPlace(dfg.getNodeOfIndex(activity));
			activity2place.put(activity, place);
		}

		/**
		 * Transitions
		 */
		for (long edge : dfg.getEdges()) {
			int sourceActivity = dfg.getEdgeSource(edge);
			int targetActivity = dfg.getEdgeTarget(edge);
			Place sourcePlace = activity2place.get(sourceActivity);
			Place targetPlace = activity2place.get(targetActivity);

			Transition transition = petriNet.addTransition(dfg.getNodeOfIndex(targetActivity));

			petriNet.addArc(sourcePlace, transition);
			petriNet.addArc(transition, targetPlace);
		}

		/**
		 * Starts
		 */
		for (TIntIterator it = dfg.getStartNodes().iterator(); it.hasNext();) {
			int activity = it.next();
			Transition transition = petriNet.addTransition(dfg.getNodeOfIndex(activity));

			petriNet.addArc(source, transition);
			petriNet.addArc(transition, activity2place.get(activity));
		}

		/**
		 * Ends
		 */
		for (TIntIterator it = dfg.getEndNodes().iterator(); it.hasNext();) {
			int activity = it.next();
			Transition transition = petriNet.addTransition(dfg.getNodeOfIndex(activity) + " -> end");
			transition.setInvisible(true);

			petriNet.addArc(activity2place.get(activity), transition);
			petriNet.addArc(transition, sink);
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet, initialMarking, finalMarking);
	}

	public static Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, Transition> convertForPerformance(
			DirectlyFollowsModel dfg) {
		TObjectIntMap<Transition> activity2skipEnqueue = new TObjectIntHashMap<>(10, 0.5f, -1);
		TObjectIntMap<Transition> activity2skipStart = new TObjectIntHashMap<>(10, 0.5f, -1);
		Set<Transition> startTransitions = new THashSet<>();
		Set<Transition> endTransitions = new THashSet<>();
		Set<Transition> interTransitions = new THashSet<>();

		Petrinet petriNet = new PetrinetImpl("converted from Dfg");
		Place source = petriNet.addPlace("net source");
		Place sink = petriNet.addPlace("net sink");
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		/**
		 * empty traces
		 */
		Transition emptyTraceTransition = null;
		if (dfg.isEmptyTraces()) {
			emptyTraceTransition = petriNet.addTransition("epsilon");
			emptyTraceTransition.setInvisible(true);
			petriNet.addArc(source, emptyTraceTransition);
			petriNet.addArc(emptyTraceTransition, sink);
		}

		/**
		 * Activities (states)
		 */
		TIntObjectMap<Place> activity2EnqueuePlace = new TIntObjectHashMap<>();
		TIntObjectMap<Place> activity2EndPlace = new TIntObjectHashMap<>();
		for (int activity : dfg.getNodeIndices()) {
			Place enqueuePlace = petriNet.addPlace(dfg.getNodeOfIndex(activity) + " enqueue");
			activity2EnqueuePlace.put(activity, enqueuePlace);

			Place startPlace = petriNet.addPlace(dfg.getNodeOfIndex(activity) + " start");
			Place completePlace = petriNet.addPlace(dfg.getNodeOfIndex(activity) + " complete");

			Place endPlace = petriNet.addPlace(dfg.getNodeOfIndex(activity) + " end");
			activity2EndPlace.put(activity, endPlace);

			Transition enqueue = petriNet.addTransition(dfg.getNodeOfIndex(activity) + "+" + ExpandProcessTree.enqueue);
			Transition skipEnqueue = petriNet.addTransition("tau");
			activity2skipEnqueue.put(skipEnqueue, activity);
			skipEnqueue.setInvisible(true);
			petriNet.addArc(enqueuePlace, enqueue);
			petriNet.addArc(enqueue, startPlace);
			petriNet.addArc(enqueuePlace, skipEnqueue);
			petriNet.addArc(skipEnqueue, startPlace);

			Transition start = petriNet.addTransition(dfg.getNodeOfIndex(activity) + "+" + ExpandProcessTree.start);
			Transition skipStart = petriNet.addTransition("tau");
			activity2skipStart.put(skipStart, activity);
			skipStart.setInvisible(true);
			petriNet.addArc(startPlace, start);
			petriNet.addArc(start, completePlace);
			petriNet.addArc(startPlace, skipStart);
			petriNet.addArc(skipStart, completePlace);

			Transition complete = petriNet
					.addTransition(dfg.getNodeOfIndex(activity) + "+" + ExpandProcessTree.complete);
			petriNet.addArc(completePlace, complete);
			petriNet.addArc(complete, endPlace);
		}

		/**
		 * Transitions
		 */
		for (long edge : dfg.getEdges()) {
			int sourceActivity = dfg.getEdgeSource(edge);
			int targetActivity = dfg.getEdgeTarget(edge);
			Place sourcePlace = activity2EndPlace.get(sourceActivity);
			Place targetPlace = activity2EnqueuePlace.get(targetActivity);

			Transition transition = petriNet.addTransition("tau");
			transition.setInvisible(true);
			interTransitions.add(transition);

			petriNet.addArc(sourcePlace, transition);
			petriNet.addArc(transition, targetPlace);
		}

		/**
		 * Starts
		 */
		for (TIntIterator it = dfg.getStartNodes().iterator(); it.hasNext();) {
			int activity = it.next();
			Transition transition = petriNet.addTransition("start -> " + dfg.getNodeOfIndex(activity));
			transition.setInvisible(true);
			startTransitions.add(transition);

			petriNet.addArc(source, transition);
			petriNet.addArc(transition, activity2EnqueuePlace.get(activity));
		}

		/**
		 * Ends
		 */
		for (TIntIterator it = dfg.getEndNodes().iterator(); it.hasNext();) {
			int activity = it.next();
			Transition transition = petriNet.addTransition(dfg.getNodeOfIndex(activity) + " -> end");
			transition.setInvisible(true);
			endTransitions.add(transition);

			petriNet.addArc(activity2EndPlace.get(activity), transition);
			petriNet.addArc(transition, sink);
		}

		return Septuple.of(AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet, initialMarking, finalMarking),
				activity2skipEnqueue, activity2skipStart, startTransitions, endTransitions, interTransitions,
				emptyTraceTransition);
	}
}
