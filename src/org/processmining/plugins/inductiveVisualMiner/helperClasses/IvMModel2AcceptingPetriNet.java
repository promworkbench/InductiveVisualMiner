package org.processmining.plugins.inductiveVisualMiner.helperClasses;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel;
import org.processmining.directlyfollowsmodelminer.model.DirectlyFollowsModel2AcceptingPetriNet;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree2AcceptingPetriNet;

public class IvMModel2AcceptingPetriNet {
	public AcceptingPetriNet convert(IvMModel model) {
		if (model.isDfg()) {
			return convert(model.getDfg());
		} else if (model.isTree()) {
			return convert(model.getTree());
		} else {
			throw new RuntimeException("model not supported");
		}
	}

	public AcceptingPetriNet convert(DirectlyFollowsModel model) {
		return DirectlyFollowsModel2AcceptingPetriNet.convert(model);
	}

	public AcceptingPetriNet convert(EfficientTree model) {
		return EfficientTree2AcceptingPetriNet.convert(model);
	}
}
