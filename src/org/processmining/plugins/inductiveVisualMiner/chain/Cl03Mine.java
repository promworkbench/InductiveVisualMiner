package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class Cl03Mine extends
		ChainLink<Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters>, ProcessTree> {

	protected Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters> generateInput(
			InductiveVisualMinerState state) {
		VisualMinerParameters minerParameters = new VisualMinerParameters(state.getPaths());
		return Quadruple.of(state.getPreMinedTree(), state.getActivityFilteredIMLog(), state.getMiner(),
				minerParameters);
	}

	protected ProcessTree executeLink(Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters> input,
			IvMCanceller canceller) {
		if (input.getA() == null) {
			//mine a new tree
			return input.getC().mine(input.getB(), input.getD(), canceller);
		} else {
			//use the existing tree
			return input.getA();
		}
	}

	protected void processResult(ProcessTree result, InductiveVisualMinerState state) {
		state.setTree(result);
		state.setSelection(new Selection());
		state.resetAlignment();
	}
}
