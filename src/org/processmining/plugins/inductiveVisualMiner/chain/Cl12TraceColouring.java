package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMapSettings;

public class Cl12TraceColouring extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	@Override
	public String getName() {
		return "colour traces";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Colouring traces..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log, IvMObject.trace_colour_map_settings };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.trace_colour_map };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogNotFiltered aLog = inputs.get(IvMObject.aligned_log);
		TraceColourMapSettings settings = inputs.get(IvMObject.trace_colour_map_settings);

		//		if (settings == null) {
		//			return new TraceColourMapFixed(RendererFactory.defaultTokenFillColour);
		//		} else {
		TraceColourMap result = settings.getTraceColourMap(model, aLog);
		//		}

		return new IvMObjectValues().//
				s(IvMObject.trace_colour_map, result);
	}
}