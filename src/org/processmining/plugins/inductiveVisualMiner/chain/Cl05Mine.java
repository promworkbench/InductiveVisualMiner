package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.InductiveMiner.mining.IMLogInfo;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

public class Cl05Mine
		extends ChainLink<Quintuple<IvMModel, IMLog, IMLogInfo, VisualMinerWrapper, VisualMinerParameters>, IvMModel> {

	protected Quintuple<IvMModel, IMLog, IMLogInfo, VisualMinerWrapper, VisualMinerParameters> generateInput(
			InductiveVisualMinerState state) {
		VisualMinerParameters minerParameters = new VisualMinerParameters(state.getPaths());
		return Quintuple.of(state.getPreMinedTree(), state.getActivityFilteredIMLog(),
				state.getActivityFilteredIMLogInfo(), state.getMiner(), minerParameters);
	}

	protected IvMModel executeLink(
			Quintuple<IvMModel, IMLog, IMLogInfo, VisualMinerWrapper, VisualMinerParameters> input,
			IvMCanceller canceller) throws UnknownTreeNodeException {
		if (input.getA() == null) {
			//mine a new tree
			IvMModel tree = input.getD().mine(input.getB(), input.getC(), input.getE(), canceller);
			if (tree != null) {
				return tree;
			} else {
				assert (canceller.isCancelled());
				return null;
			}
		} else {
			//use the existing tree
			return input.getA();
		}
	}

	protected void processResult(IvMModel result, InductiveVisualMinerState state) {
		state.setModel(result);
		state.setSelection(new Selection());
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setModel(null);
		state.setSelection(new Selection());
	}

	public String getName() {
		return "mine";
	}
}
