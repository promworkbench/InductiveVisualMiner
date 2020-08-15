package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

/**
 * Some (virtual) attributes might use information from the IvMLog (=aligned
 * log). Therefore, their minimum and maximum need to be updated after the
 * alignment finishes.
 * 
 * @author sander
 *
 */
public class Cl08UpdateIvMAttributes extends
		ChainLink<InductiveVisualMinerState, Triple<IvMLogNotFiltered, AttributesInfo, IvMVirtualAttributeFactory>, IvMAttributesInfo> {

	public String getName() {
		return "update IvM attributes";
	}

	public String getStatusBusyMessage() {
		return "updating attributes";
	}

	protected Triple<IvMLogNotFiltered, AttributesInfo, IvMVirtualAttributeFactory> generateInput(
			InductiveVisualMinerState state) {
		return Triple.of(state.getIvMLog(), state.getAttributesInfo(), state.getConfiguration().getVirtualAttributes());
	}

	protected IvMAttributesInfo executeLink(Triple<IvMLogNotFiltered, AttributesInfo, IvMVirtualAttributeFactory> input,
			IvMCanceller canceller) throws Exception {
		return new IvMAttributesInfo(input.getA(), input.getB(), input.getC());
	}

	protected void processResult(IvMAttributesInfo result, InductiveVisualMinerState state) {
		state.setAttributesInfoIvM(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setAttributesInfoIvM(null);
	}

}