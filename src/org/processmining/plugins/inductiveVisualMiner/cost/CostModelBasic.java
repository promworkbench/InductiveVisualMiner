package org.processmining.plugins.inductiveVisualMiner.cost;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;

public class CostModelBasic extends CostModelAbstract {

	public CostModelBasic(IvMModel model, double logMove, double modelMove, double syncMove) {
		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				parameter_logMove.put(node, logMove);
				parameter_modelMove.put(node, modelMove);
				parameter_syncMove.put(node, syncMove);
			}
		}
	}

	public double getCost(int node, IvMMove initiate, IvMMove enqueue, IvMMove start, IvMMove complete) {

		double result = 0;

		if (complete.isLogMove()) {
			//log move
			result += parameter_logMove.get(node);
		} else if (complete.isModelMove()) {
			//model move
			result += parameter_modelMove.get(node);
		} else {
			//sync move
			result += parameter_syncMove.get(node);

			//TODO: time-based costing
		}

		return result;
	}

	public Pair<Long, String> getNodeRepresentationModel(int node) {
		return Pair.of( //
				(long) (parameter_syncMove.get(node)), //
				"sync move cost " + parameter_syncMove.get(node));
	}

	public String[][] getNodeRepresentationPopup(int node) {
		return new String[][] { //
				{ "cost                   " + parameter_syncMove.get(node) }, //
				{ "cost skip model        " + parameter_modelMove.get(node) }, //
				{ "cost extra log event   " + parameter_logMove.get(node) }, //
		};
	}
}