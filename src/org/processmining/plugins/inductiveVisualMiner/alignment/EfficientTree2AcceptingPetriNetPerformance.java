package org.processmining.plugins.inductiveVisualMiner.alignment;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.PetrinetImplExt;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.set.hash.THashSet;

public class EfficientTree2AcceptingPetriNetPerformance {

	public static AtomicInteger placeCounter = new AtomicInteger();

	private static class PN {
		PetrinetImplExt pn = new PetrinetImplExt("converted from efficient tree");
		ArrayList<Arc> arcs = new ArrayList<>();

		TObjectIntMap<Transition> performanceTransition2node = new TObjectIntHashMap<>(10, 0.5f, -1);
		Set<Transition> skipEnqueueTransitions = new THashSet<>();
	}

	public static Triple<AcceptingPetriNet, TObjectIntMap<Transition>, Set<Transition>> convert(EfficientTree tree) {
		PN petriNet = new PN();
		Place source = petriNet.pn.addPlace("net source");
		Place sink = petriNet.pn.addPlace("net sink");
		Marking initialMarking = new Marking();
		initialMarking.add(source);
		Marking finalMarking = new Marking();
		finalMarking.add(sink);

		int root = tree.getRoot();

		convertNode(petriNet, tree, root, source, sink);

		petriNet.pn.addArcsWithoutCheck(petriNet.arcs);
		AcceptingPetriNet apn = AcceptingPetriNetFactory.createAcceptingPetriNet(petriNet.pn, initialMarking,
				finalMarking);
		return Triple.of(apn, petriNet.performanceTransition2node, petriNet.skipEnqueueTransitions);
	}

	private static void convertNode(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		if (tree.isTau(node)) {
			convertTau(petriNet, tree, node, source, sink);
		} else if (tree.isActivity(node)) {
			convertTask(petriNet, tree, node, source, sink);
		} else if (tree.isConcurrent(node)) {
			convertAnd(petriNet, tree, node, source, sink);
		} else if (tree.isSequence(node)) {
			convertSeq(petriNet, tree, node, source, sink);
		} else if (tree.isXor(node)) {
			convertXor(petriNet, tree, node, source, sink);
		} else if (tree.isLoop(node)) {
			convertLoop(petriNet, tree, node, source, sink);
		} else if (tree.isOr(node)) {
			convertOr(petriNet, tree, node, source, sink);
		} else if (tree.isInterleaved(node)) {
			convertInterleaved(petriNet, tree, node, source, sink);
		} else {
			throw new RuntimeException("not implemented");
		}
	}

	private static void convertTau(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		Transition t = petriNet.pn.addTransition("tau from tree");
		t.setInvisible(true);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, t));
		petriNet.arcs.add(PetrinetImplExt.createArc(t, sink));
		petriNet.performanceTransition2node.put(t, node);
	}

	private static void convertTask(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		String activity = tree.getActivityName(node);

		Transition enqueue = petriNet.pn.addTransition(activity + "+enqueue");
		petriNet.performanceTransition2node.put(enqueue, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, enqueue));

		Transition skipEnqueue = petriNet.pn.addTransition(activity + "+enqueue skip");
		skipEnqueue.setInvisible(true);
		petriNet.performanceTransition2node.put(skipEnqueue, node);
		petriNet.skipEnqueueTransitions.add(skipEnqueue);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, skipEnqueue));

		Place p1 = petriNet.pn.addPlace(activity + "+enqueued" + placeCounter.incrementAndGet());
		petriNet.arcs.add(PetrinetImplExt.createArc(enqueue, p1));
		petriNet.arcs.add(PetrinetImplExt.createArc(skipEnqueue, p1));

		Transition start = petriNet.pn.addTransition(activity + "+start");
		petriNet.performanceTransition2node.put(start, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(p1, start));

		Transition skipStart = petriNet.pn.addTransition(activity + "+start skip");
		skipStart.setInvisible(true);
		petriNet.performanceTransition2node.put(skipStart, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(p1, skipStart));

		Place p2 = petriNet.pn.addPlace(activity + "+started" + placeCounter.incrementAndGet());
		petriNet.arcs.add(PetrinetImplExt.createArc(start, p2));
		petriNet.arcs.add(PetrinetImplExt.createArc(skipStart, p2));

		Transition complete = petriNet.pn.addTransition(activity + "+complete");
		petriNet.performanceTransition2node.put(complete, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(p2, complete));
		petriNet.arcs.add(PetrinetImplExt.createArc(complete, sink));
	}

	private static void convertXor(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		for (int child : tree.getChildren(node)) {
			convertNode(petriNet, tree, child, source, sink);
		}
	}

	private static void convertSeq(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		int last = tree.getNumberOfChildren(node);
		int i = 0;
		Place lastSink = source;
		for (int child : tree.getChildren(node)) {
			Place childSink;
			if (i == last - 1) {
				childSink = sink;
			} else {
				childSink = petriNet.pn.addPlace("sink " + placeCounter.incrementAndGet());
			}

			convertNode(petriNet, tree, child, lastSink, childSink);
			lastSink = childSink;
			i++;
		}
	}

	private static void convertAnd(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		//add split tau
		Transition t1 = petriNet.pn.addTransition("tau split");
		t1.setInvisible(true);
		petriNet.performanceTransition2node.put(t1, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, t1));

		//add join tau
		Transition t2 = petriNet.pn.addTransition("tau join");
		petriNet.performanceTransition2node.put(t2, node);
		t2.setInvisible(true);
		petriNet.arcs.add(PetrinetImplExt.createArc(t2, sink));

		//add for each child a source and sink place
		for (int child : tree.getChildren(node)) {
			Place childSource = petriNet.pn.addPlace("source " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(t1, childSource));

			Place childSink = petriNet.pn.addPlace("sink " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(childSink, t2));

			convertNode(petriNet, tree, child, childSource, childSink);
		}
	}

	private static void convertLoop(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		if (tree.getNumberOfChildren(node) != 3) {
			//a loop must have precisely three children: body, redo and exit
			throw new RuntimeException("A loop should have precisely three children");
		}

		Place middlePlace = petriNet.pn.addPlace("middle " + placeCounter.incrementAndGet());

		//add an extra tau
		Transition t = petriNet.pn.addTransition("tau start");
		t.setInvisible(true);
		petriNet.performanceTransition2node.put(t, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, t));
		//replace the source
		source = petriNet.pn.addPlace("replacement source " + placeCounter.incrementAndGet());
		petriNet.arcs.add(PetrinetImplExt.createArc(t, source));

		//body
		convertNode(petriNet, tree, tree.getChild(node, 0), source, middlePlace);
		//redo
		convertNode(petriNet, tree, tree.getChild(node, 1), middlePlace, source);
		//exit
		convertNode(petriNet, tree, tree.getChild(node, 2), middlePlace, sink);
	}

	private static void convertOr(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {

		Transition start = petriNet.pn.addTransition("tau start");
		petriNet.performanceTransition2node.put(start, node);
		start.setInvisible(true);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, start));

		Place notDoneFirst = petriNet.pn.addPlace("notDoneFirst " + placeCounter.incrementAndGet());
		petriNet.arcs.add(PetrinetImplExt.createArc(start, notDoneFirst));

		Place doneFirst = petriNet.pn.addPlace("doneFirst " + placeCounter.incrementAndGet());
		Transition end = petriNet.pn.addTransition("tau finish");
		end.setInvisible(true);
		petriNet.performanceTransition2node.put(end, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(doneFirst, end));
		petriNet.arcs.add(PetrinetImplExt.createArc(end, sink));

		for (int child : tree.getChildren(node)) {
			Place childSource = petriNet.pn.addPlace("childSource " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(start, childSource));
			Place childSink = petriNet.pn.addPlace("childSink " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(childSink, end));
			Place doChild = petriNet.pn.addPlace("doChild " + placeCounter.incrementAndGet());

			//skip
			Transition skipChild = petriNet.pn.addTransition("tau skipChild");
			skipChild.setInvisible(true);
			petriNet.performanceTransition2node.put(skipChild, node);
			petriNet.arcs.add(PetrinetImplExt.createArc(childSource, skipChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(skipChild, childSink));
			petriNet.arcs.add(PetrinetImplExt.createArc(skipChild, doneFirst));
			petriNet.arcs.add(PetrinetImplExt.createArc(doneFirst, skipChild));

			//first do
			Transition firstDoChild = petriNet.pn.addTransition("tau firstDoChild");
			firstDoChild.setInvisible(true);
			petriNet.performanceTransition2node.put(firstDoChild, node);
			petriNet.arcs.add(PetrinetImplExt.createArc(childSource, firstDoChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(notDoneFirst, firstDoChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(firstDoChild, doneFirst));
			petriNet.arcs.add(PetrinetImplExt.createArc(firstDoChild, doChild));

			//later do
			Transition laterDoChild = petriNet.pn.addTransition("tau laterDoChild");
			laterDoChild.setInvisible(true);
			petriNet.performanceTransition2node.put(laterDoChild, node);
			petriNet.arcs.add(PetrinetImplExt.createArc(childSource, laterDoChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(laterDoChild, doChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(laterDoChild, doneFirst));
			petriNet.arcs.add(PetrinetImplExt.createArc(doneFirst, laterDoChild));

			convertNode(petriNet, tree, child, doChild, childSink);
		}
	}

	private static void convertInterleaved(PN petriNet, EfficientTree tree, int node, Place source, Place sink) {
		Transition start = petriNet.pn.addTransition("tau start");
		start.setInvisible(true);
		petriNet.performanceTransition2node.put(start, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(source, start));

		Place mileStone = petriNet.pn.addPlace("milestone place " + placeCounter.incrementAndGet());
		petriNet.arcs.add(PetrinetImplExt.createArc(start, mileStone));

		Transition end = petriNet.pn.addTransition("tau end");
		end.setInvisible(true);
		petriNet.performanceTransition2node.put(end, node);
		petriNet.arcs.add(PetrinetImplExt.createArc(mileStone, end));
		petriNet.arcs.add(PetrinetImplExt.createArc(end, sink));

		for (int child : tree.getChildren(node)) {
			Place childTodo = petriNet.pn.addPlace("child todo " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(start, childTodo));

			Transition startChild = petriNet.pn.addTransition("tau start child");
			startChild.setInvisible(true);
			petriNet.performanceTransition2node.put(startChild, node);
			petriNet.arcs.add(PetrinetImplExt.createArc(childTodo, startChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(mileStone, startChild));

			Place childSource = petriNet.pn.addPlace("child source " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(startChild, childSource));

			Place childSink = petriNet.pn.addPlace("child sink " + placeCounter.incrementAndGet());

			Transition endChild = petriNet.pn.addTransition("tau end child");
			endChild.setInvisible(true);
			petriNet.performanceTransition2node.put(endChild, node);
			petriNet.arcs.add(PetrinetImplExt.createArc(childSink, endChild));
			petriNet.arcs.add(PetrinetImplExt.createArc(endChild, mileStone));

			Place childDone = petriNet.pn.addPlace("child done " + placeCounter.incrementAndGet());
			petriNet.arcs.add(PetrinetImplExt.createArc(endChild, childDone));
			petriNet.arcs.add(PetrinetImplExt.createArc(childDone, end));

			convertNode(petriNet, tree, child, childSource, childSink);
		}
	}

}
