package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class Dfg2AcceptingPetriNet {
	public static AcceptingPetriNet convert(Dfg dfg) {
		Petrinet petriNet = new PetrinetImpl("converted from Dfg");
		Place source = petriNet.addPlace("net source");
		Place sink = petriNet.addPlace("net sink");
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		/**
		 * Activities (states)
		 */
		TIntObjectMap<Place> activity2place = new TIntObjectHashMap<>();
		for (int activity : dfg.getActivityIndices()) {
			Place place = petriNet.addPlace(dfg.getActivityOfIndex(activity).getId());
			activity2place.put(activity, place);
		}

		/**
		 * Transitions
		 */
		for (long edge : dfg.getDirectlyFollowsEdges()) {
			int sourceActivity = dfg.getDirectlyFollowsEdgeSourceIndex(edge);
			int targetActivity = dfg.getDirectlyFollowsEdgeTargetIndex(edge);
			Place sourcePlace = activity2place.get(sourceActivity);
			Place targetPlace = activity2place.get(targetActivity);

			Transition transition = petriNet.addTransition(
					dfg.getActivityOfIndex(sourceActivity).getId() + " -> " + dfg.getActivityOfIndex(targetActivity));

			petriNet.addArc(sourcePlace, transition);
			petriNet.addArc(transition, targetPlace);
		}

		/**
		 * Starts
		 */
		for (int activity : dfg.getStartActivityIndices()) {
			Transition transition = petriNet.addTransition(dfg.getActivityOfIndex(activity).getId());

			petriNet.addArc(source, transition);
			petriNet.addArc(transition, activity2place.get(activity));
		}

		/**
		 * Ends
		 */
		for (int activity : dfg.getEndActivityIndices()) {
			Transition transition = petriNet.addTransition(dfg.getActivityOfIndex(activity).getId() + " -> end");
			transition.setInvisible(true);

			petriNet.addArc(activity2place.get(activity), transition);
			petriNet.addArc(transition, sink);
		}

		return AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet, initialMarking, finalMarking);
	}

	public static Sextuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>> convertForPerformance(
			Dfg dfg) {
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
		 * Activities (states)
		 */
		TIntObjectMap<Place> activity2EnqueuePlace = new TIntObjectHashMap<>();
		TIntObjectMap<Place> activity2EndPlace = new TIntObjectHashMap<>();
		for (int activity : dfg.getActivityIndices()) {
			Place enqueuePlace = petriNet.addPlace(dfg.getActivityOfIndex(activity).getId() + " enqueue");
			activity2EnqueuePlace.put(activity, enqueuePlace);

			Place startPlace = petriNet.addPlace(dfg.getActivityOfIndex(activity).getId() + " start");
			Place completePlace = petriNet.addPlace(dfg.getActivityOfIndex(activity).getId() + " complete");

			Place endPlace = petriNet.addPlace(dfg.getActivityOfIndex(activity).getId() + " end");
			activity2EndPlace.put(activity, endPlace);

			Transition enqueue = petriNet
					.addTransition(dfg.getActivityOfIndex(activity) + "+" + ExpandProcessTree.enqueue);
			Transition skipEnqueue = petriNet.addTransition("tau");
			activity2skipEnqueue.put(skipEnqueue, activity);
			skipEnqueue.setInvisible(true);
			petriNet.addArc(enqueuePlace, enqueue);
			petriNet.addArc(enqueue, startPlace);
			petriNet.addArc(enqueuePlace, skipEnqueue);
			petriNet.addArc(skipEnqueue, startPlace);

			Transition start = petriNet.addTransition(dfg.getActivityOfIndex(activity) + "+" + ExpandProcessTree.start);
			Transition skipStart = petriNet.addTransition("tau");
			activity2skipStart.put(skipStart, activity);
			skipStart.setInvisible(true);
			petriNet.addArc(startPlace, start);
			petriNet.addArc(start, completePlace);
			petriNet.addArc(startPlace, skipStart);
			petriNet.addArc(skipStart, completePlace);

			Transition complete = petriNet
					.addTransition(dfg.getActivityOfIndex(activity) + "+" + ExpandProcessTree.complete);
			petriNet.addArc(completePlace, complete);
			petriNet.addArc(complete, endPlace);
		}

		/**
		 * Transitions
		 */
		for (long edge : dfg.getDirectlyFollowsEdges()) {
			int sourceActivity = dfg.getDirectlyFollowsEdgeSourceIndex(edge);
			int targetActivity = dfg.getDirectlyFollowsEdgeTargetIndex(edge);
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
		for (int activity : dfg.getStartActivityIndices()) {
			Transition transition = petriNet.addTransition("start -> " + dfg.getActivityOfIndex(activity).getId());
			transition.setInvisible(true);
			startTransitions.add(transition);

			petriNet.addArc(source, transition);
			petriNet.addArc(transition, activity2EnqueuePlace.get(activity));
		}

		/**
		 * Ends
		 */
		for (int activity : dfg.getEndActivityIndices()) {
			Transition transition = petriNet.addTransition(dfg.getActivityOfIndex(activity).getId() + " -> end");
			transition.setInvisible(true);
			endTransitions.add(transition);

			petriNet.addArc(activity2EndPlace.get(activity), transition);
			petriNet.addArc(transition, sink);
		}

		return Sextuple.of(AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet, initialMarking, finalMarking),
				activity2skipEnqueue, activity2skipStart, startTransitions, endTransitions, interTransitions);
	}
}
