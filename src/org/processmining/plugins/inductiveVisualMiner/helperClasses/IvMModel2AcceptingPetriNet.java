package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import java.util.List;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel2AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class IvMModel2AcceptingPetriNet {
	public Pair<AcceptingPetriNet, TObjectIntMap<Transition>> convert(IvMModel model) {
		if (model.isDfg()) {
			return convert(model.getDfg());
		} else if (model.isTree()) {
			return convert(model.getTree());
		} else {
			throw new RuntimeException("model not supported");
		}
	}

	public Pair<AcceptingPetriNet, TObjectIntMap<Transition>> convert(DirectlyFollowsModel model) {
		TIntObjectMap<List<Transition>> map = new TIntObjectHashMap<Transition>(10, 0.5f, -1);
		AcceptingPetriNet aNet = DirectlyFollowsModel2AcceptingPetriNet.convert(model, map);
		return Pair.of(aNet, map);
	}

	public Pair<AcceptingPetriNet, TObjectIntMap<Transition>> convert(EfficientTree model) {
		return EfficientTree2AcceptingPetriNet.convert(model);
	}
}
