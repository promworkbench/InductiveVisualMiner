package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

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
		ExecutorService executor = Executors.newFixedThreadPool(
				Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
				new ThreadFactoryBuilder().setNameFormat("ivm-thread-ivmattributes-%d").build());
		IvMAttributesInfo result;
		try {
			result = new IvMAttributesInfo(input.getA(), input.getB(), input.getC(), executor);
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} finally {
			executor.shutdownNow();
		}
		return result;
	}

	protected void processResult(IvMAttributesInfo result, InductiveVisualMinerState state) {
		state.setAttributesInfoIvM(result);
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setAttributesInfoIvM(null);
	}

}