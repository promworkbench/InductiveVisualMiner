package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.HashSet;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerParameters;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

public class Cl03Mine extends
		ChainLink<Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters>, ProcessTree> {

	public Cl03Mine(ProMCanceller globalCanceller) {
		super(globalCanceller);
	}

	protected Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters> generateInput(InductiveVisualMinerState state) {
		VisualMinerParameters minerParameters = new VisualMinerParameters(state.getPaths());
		return Quadruple.of(state.getPreMinedTree(), state.getActivityFilteredIMLog(), state.getMiner(),
				minerParameters);
	}

	protected ProcessTree executeLink(Quadruple<ProcessTree, IMLog, VisualMinerWrapper, VisualMinerParameters> input) {
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
		state.setSelectedNodes(new HashSet<UnfoldedNode>());
		state.setSelectedLogMoves(new HashSet<LogMovePosition>());
		state.resetAlignment();
	}
}
