package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
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
public class Cl08UpdateIvMAttributes extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	public String getName() {
		return "update IvM attributes";
	}

	public String getStatusBusyMessage() {
		return "updating attributes";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log, IvMObject.attributes_info };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);
		AttributesInfo attributesInfo = inputs.get(IvMObject.attributes_info);
		IvMVirtualAttributeFactory virtualAttributes = configuration.getVirtualAttributes();

		ExecutorService executor = Executors.newFixedThreadPool(
				Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
				new ThreadFactoryBuilder().setNameFormat("ivm-thread-ivmattributes-%d").build());
		IvMAttributesInfo result;
		try {
			result = new IvMAttributesInfo(aLog, attributesInfo, virtualAttributes, executor);
			executor.shutdown();
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} finally {
			executor.shutdownNow();
		}

		return new IvMObjectValues().//
				s(IvMObject.ivm_attributes_info, result);
	}
}