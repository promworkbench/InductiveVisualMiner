package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.Set;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class AcceptingPetriNet2AcceptingPetriNetPerformance {
	public static Septuple<AcceptingPetriNet, TObjectIntMap<Transition>, TObjectIntMap<Transition>, Set<Transition>, Set<Transition>, Set<Transition>, TObjectIntMap<Transition>> convertForPerformance(
			IvMModel model) {
		assert model.isNet();
		AcceptingPetriNet oldNet = model.getNet();

		TObjectIntMap<Transition> activity2skipEnqueue = new TObjectIntHashMap<>(10, 0.5f, -1);
		TObjectIntMap<Transition> activity2skipStart = new TObjectIntHashMap<>(10, 0.5f, -1);
		Set<Transition> startTransitions = new THashSet<>();
		Set<Transition> endTransitions = new THashSet<>();
		Set<Transition> interTransitions = new THashSet<>();
		TObjectIntMap<Transition> activity2node = new TObjectIntHashMap<>(10, 0.5f, -1);

		Petrinet newNet = new PetrinetImpl("converted from net");

		THashMap<Place, Place> oldPlace2newPlace = new THashMap<>();

		//places
		for (Place oldPlace : oldNet.getNet().getPlaces()) {
			Place newPlace = newNet.addPlace(oldPlace.getLabel());
			oldPlace2newPlace.put(oldPlace, newPlace);
		}

		//transitions
		for (int node = 0; node < model.getMaxNumberOfNodes(); node++) {
			Transition oldTransition = model.getNetTransition(node);
			if (model.isTau(node)) {
				Transition newTransition = newNet.addTransition(oldTransition.getLabel());
				newTransition.setInvisible(true);
				activity2node.put(newTransition, node);

				//edges
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> oldEdge : oldNet.getNet()
						.getInEdges(oldTransition)) {
					Place oldSource = (Place) oldEdge.getSource();
					newNet.addArc(oldPlace2newPlace.get(oldSource), newTransition);
				}
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> oldEdge : oldNet.getNet()
						.getOutEdges(oldTransition)) {
					Place oldTarget = (Place) oldEdge.getTarget();
					newNet.addArc(newTransition, oldPlace2newPlace.get(oldTarget));
				}
			} else {
				String activity = oldTransition.getLabel();

				Place startPlace = newNet.addPlace(activity + " start");
				Place completePlace = newNet.addPlace(activity + " complete");

				Transition enqueue = newNet.addTransition(activity + "+" + ExpandProcessTree.enqueue);
				activity2node.put(enqueue, node);
				Transition skipEnqueue = newNet.addTransition("tau");
				activity2skipEnqueue.put(skipEnqueue, node);
				activity2node.put(skipEnqueue, node);
				skipEnqueue.setInvisible(true);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> oldEdge : oldNet.getNet()
						.getInEdges(oldTransition)) {
					Place oldSource = (Place) oldEdge.getSource();
					newNet.addArc(oldPlace2newPlace.get(oldSource), enqueue);
					newNet.addArc(oldPlace2newPlace.get(oldSource), skipEnqueue);
				}
				newNet.addArc(enqueue, startPlace);
				newNet.addArc(skipEnqueue, startPlace);

				Transition start = newNet.addTransition(activity + "+" + ExpandProcessTree.start);
				activity2node.put(start, node);
				Transition skipStart = newNet.addTransition("tau");
				activity2node.put(skipStart, node);
				activity2skipStart.put(skipStart, node);
				skipStart.setInvisible(true);
				newNet.addArc(startPlace, start);
				newNet.addArc(start, completePlace);
				newNet.addArc(startPlace, skipStart);
				newNet.addArc(skipStart, completePlace);

				Transition complete = newNet.addTransition(activity + "+" + ExpandProcessTree.complete);
				activity2node.put(complete, node);
				newNet.addArc(completePlace, complete);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> oldEdge : oldNet.getNet()
						.getOutEdges(oldTransition)) {
					Place oldTarget = (Place) oldEdge.getTarget();
					newNet.addArc(complete, oldPlace2newPlace.get(oldTarget));
				}
			}
		}

		//initial marking
		Marking newInitialMarking = new Marking();
		for (Place oldPlace : oldNet.getInitialMarking()) {
			newInitialMarking.add(oldPlace2newPlace.get(oldPlace), oldNet.getInitialMarking().occurrences(oldPlace));
		}

		//final markings
		Marking[] newFinalMarkings = new Marking[oldNet.getFinalMarkings().size()];
		{
			int i = 0;
			for (Marking finalMarking : oldNet.getFinalMarkings()) {
				Marking newFinalMarking = new Marking();
				for (Place place : finalMarking) {
					newFinalMarking.add(oldPlace2newPlace.get(place), finalMarking.occurrences(place));
				}
				newFinalMarkings[i] = newFinalMarking;
				i++;
			}
		}

		return Septuple.of(
				AcceptingPetriNetFactory.createAcceptingPetriNet(newNet, newInitialMarking, newFinalMarkings),
				activity2skipEnqueue, activity2skipStart, startTransitions, endTransitions, interTransitions,
				activity2node);
	}
}