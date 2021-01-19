package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerSelectionColourer;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

/**
 * This class is to be instantiated once for every mode.
 * 
 * @author sander
 *
 */
public class Cl22TraceViewEventColourMapFiltered extends DataChainLinkComputationAbstract {

	private final InductiveVisualMinerConfiguration configuration;

	public Cl22TraceViewEventColourMapFiltered(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getName() {
		return "trace view event colour map";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Colouring events..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.graph_svg, IvMObject.selected_model_selection,
				IvMObject.selected_visualisation_mode, IvMObject.graph_visualisation_info,
				IvMObject.visualisation_data };
	}

	public IvMObject<?>[] createNonTriggerObjects() {
		return Mode.gatherInputsRequested(configuration);
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.trace_view_event_colour_map };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		SVGDiagram svg = inputs.get(IvMObject.graph_svg);
		IvMModel model = inputs.get(IvMObject.model);
		Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		Selection selection = inputs.get(IvMObject.selected_model_selection);
		ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);
		AlignedLogVisualisationData visualisationData = inputs.get(IvMObject.visualisation_data);

		IvMObjectValues subInputs = inputs.getIfPresent(mode.inputsRequested());
		ProcessTreeVisualisationParameters visualisationParameters = mode
				.getVisualisationParametersWithAlignments(subInputs);

		TraceViewEventColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(svg,
				visualisationInfo, model, visualisationData, visualisationParameters);
		colourMap.setSelectedNodes(selection);

		return new IvMObjectValues().//
				s(IvMObject.trace_view_event_colour_map, colourMap);
	}

}
