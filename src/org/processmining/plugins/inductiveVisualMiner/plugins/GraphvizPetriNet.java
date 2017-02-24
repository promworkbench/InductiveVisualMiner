package org.processmining.plugins.inductiveVisualMiner.plugins;

import java.util.HashMap;

import javax.swing.JComponent;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.dot.Dot.GraphDirection;
import org.processmining.plugins.graphviz.dot.DotNode;
import org.processmining.plugins.graphviz.visualisation.DotPanel;

public class GraphvizPetriNet {

	@Plugin(name = "Graphviz Petri net visualisation", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Petri net" }, userAccessible = true, level = PluginLevel.PeerReviewed)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Petrinet petrinet) {
		Dot dot = convert(petrinet, null, null);
		return new DotPanel(dot);
	}
	
	@Plugin(name = "Graphviz Accepting Petri net visualisation", returnLabels = { "Dot visualization" }, returnTypes = { JComponent.class }, parameterLabels = { "Petri net" }, userAccessible = true, level = PluginLevel.PeerReviewed)
	@Visualizer
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Convert Process tree", requiredParameterLabels = { 0 })
	public JComponent visualize2(PluginContext context, AcceptingPetriNet petrinet) {
		Dot dot = convert(petrinet.getNet(), petrinet.getInitialMarking(), petrinet.getFinalMarkings());
		return new DotPanel(dot);
	}

	public static Dot convert(PetrinetGraph petrinet, Marking initialMarking, Iterable<Marking> finalMarkings) {
		Dot dot = new Dot();
		dot.setDirection(GraphDirection.leftRight);
		convert(dot, petrinet, initialMarking, finalMarkings);
		return dot;
	}

	private static class LocalDotPlace extends DotNode {
		public LocalDotPlace() {
			super("", null);
			setOption("shape", "circle");
		}
	}

	private static class LocalDotTransition extends DotNode {
		//transition
		public LocalDotTransition(String label) {
			super(label, null);
			setOption("shape", "box");
		}

		//tau transition
		public LocalDotTransition() {
			super("", null);
			setOption("style", "filled");
			setOption("fillcolor", "#EEEEEE");
			setOption("width", "0.15");
			setOption("shape", "box");
		}
	}

	private static void convert(Dot dot, PetrinetGraph petrinet, Marking initialMarking, Iterable<Marking> finalMarkings) {
		HashMap<PetrinetNode, DotNode> mapPetrinet2Dot = new HashMap<PetrinetNode, DotNode>();

		//add places
		for (Place p : petrinet.getPlaces()) {
			DotNode place;

			//find final marking
			boolean inFinalMarking = false;
			if (finalMarkings != null) {
				for (Marking finalMarking : finalMarkings) {
					inFinalMarking |= finalMarking.contains(p);
				}
			}

			place = new LocalDotPlace();
			if (initialMarking != null && initialMarking.contains(p) && finalMarkings != null && inFinalMarking) {
				place.setOption("style", "filled");
				place.setOption("fillcolor", "#80ff00;0.5:#E40000");
			} else if (initialMarking != null && initialMarking.contains(p)) {
				place.setOption("style", "filled");
				place.setOption("fillcolor", "#80ff00");
			} else if (finalMarkings != null && inFinalMarking) {
				place.setOption("style", "filled");
				place.setOption("fillcolor", "#E40000");
			}
			dot.addNode(place);
			mapPetrinet2Dot.put(p, place);
		}

		//add transitions
		for (Transition t : petrinet.getTransitions()) {
			DotNode transition;
			if (t.isInvisible()) {
				transition = new LocalDotTransition();
			} else {
				transition = new LocalDotTransition(t.getLabel());
			}
			dot.addNode(transition);
			mapPetrinet2Dot.put(t, transition);
		}

		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : petrinet.getEdges()) {
			if (mapPetrinet2Dot.get(edge.getSource()) != null && mapPetrinet2Dot.get(edge.getTarget()) != null) {
				dot.addEdge(mapPetrinet2Dot.get(edge.getSource()), mapPetrinet2Dot.get(edge.getTarget()));
			}
		}
	}
}
