package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;

public class Cl03Mine extends
		ChainLink<Quadruple<IvMEfficientTree, IMLog, VisualMinerWrapper, VisualMinerParameters>, IvMEfficientTree> {

	protected Quadruple<IvMEfficientTree, IMLog, VisualMinerWrapper, VisualMinerParameters> generateInput(
			InductiveVisualMinerState state) {
		VisualMinerParameters minerParameters = new VisualMinerParameters(state.getPaths());
		return Quadruple.of(state.getPreMinedTree(), state.getActivityFilteredIMLog(), state.getMiner(),
				minerParameters);
	}

	protected IvMEfficientTree executeLink(
			Quadruple<IvMEfficientTree, IMLog, VisualMinerWrapper, VisualMinerParameters> input, IvMCanceller canceller)
			throws UnknownTreeNodeException {
		if (input.getA() == null) {
			//mine a new tree
			ProcessTree tree = input.getC().mine(input.getB(), input.getD(), canceller);
			if (tree != null) {
				return new IvMEfficientTree(tree);
			} else {
				assert(canceller.isCancelled());
				return null;
			}
		} else {
			//use the existing tree
			return input.getA();
		}
	}

	protected void processResult(IvMEfficientTree result, InductiveVisualMinerState state) {
		state.setTree(result);
		state.setSelection(new Selection());
		state.resetAlignment();
	}
}
