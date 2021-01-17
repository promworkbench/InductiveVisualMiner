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

public class Cl22TraceViewEventColourMapFiltered extends DataChainLinkAbstract implements DataChainLinkComputation {

	public String getName() {
		return "trace view event colour map";
	}

	public String getStatusBusyMessage() {
		return "Colouring events..";
	}

	public IvMObject<?>[] getInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.graph_svg, IvMObject.selected_model_selection,
				IvMObject.selected_visualisation_mode, IvMObject.graph_visualisation_info, IvMObject.carte_blanche,
				IvMObject.visualisation_data };
	}

	public IvMObject<?>[] getOutputNames() {
		return new IvMObject<?>[] { IvMObject.trace_view_event_colour_map };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		SVGDiagram svg = inputs.get(IvMObject.graph_svg);
		IvMModel model = inputs.get(IvMObject.model);
		Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		Selection selection = inputs.get(IvMObject.selected_model_selection);
		IvMObjectCarteBlanche carteBlanche = inputs.get(IvMObject.carte_blanche);
		ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);
		AlignedLogVisualisationData visualisationData = inputs.get(IvMObject.visualisation_data);

		ProcessTreeVisualisationParameters visualisationParameters = mode
				.getVisualisationParametersWithAlignments(carteBlanche.getIfPresent(mode.inputsRequested()));

		TraceViewEventColourMap colourMap = InductiveVisualMinerSelectionColourer.colourHighlighting(svg,
				visualisationInfo, model, visualisationData, visualisationParameters);
		colourMap.setSelectedNodes(selection);

		return new IvMObjectValues().//
				s(IvMObject.trace_view_event_colour_map, colourMap);
	}

}
