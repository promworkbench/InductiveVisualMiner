package org.processmining.plugins.inductiveVisualMiner.chain;

import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.DotPanel;
import org.processmining.plugins.graphviz.visualisation.DotPanelUserSettings;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.traceview.TraceViewEventColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.DfmVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisation;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

public class Cl09LayoutAlignment implements DataChainLinkComputation {

	@Override
	public String getName() {
		return "layout alignment";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Layouting aligned model..";
	}

	@Override
	public IvMObject<?>[] getInputNames() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_info, IvMObject.selected_visualisation_mode,
				IvMObject.selected_graph_user_settings, IvMObject.carte_blanche };
	}

	@Override
	public IvMObject<?>[] getOutputNames() {
		return new IvMObject<?>[] { IvMObject.graph_dot, IvMObject.graph_svg, IvMObject.graph_visualisation_info,
				IvMObject.trace_view_event_colour_map };
	}

	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info);
		Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		DotPanelUserSettings settings = inputs.get(IvMObject.selected_graph_user_settings);
		IvMObjectCarteBlanche carteBlanche = inputs.get(IvMObject.carte_blanche);

		IvMObjectValues modeInputs = carteBlanche.getIfPresent(mode.inputsRequested());
		ProcessTreeVisualisationParameters visualisationParameters = mode
				.getVisualisationParametersWithAlignments(modeInputs);

		//compute dot
		AlignedLogVisualisationData data = new AlignedLogVisualisationDataImplFrequencies(model, logInfo);
		Triple<Dot, ProcessTreeVisualisationInfo, TraceViewEventColourMap> p;
		if (model.isTree()) {
			ProcessTreeVisualisation visualiser = new ProcessTreeVisualisation();
			p = visualiser.fancy(model, data, visualisationParameters);
		} else {
			DfmVisualisation visualiser = new DfmVisualisation();
			p = visualiser.fancy(model, data, visualisationParameters);
		}

		//keep the user settings of the dot panel
		settings.applyToDot(p.getA());

		//compute svg from dot
		SVGDiagram diagram = DotPanel.dot2svg(p.getA());

		return new IvMObjectValues().//
				s(IvMObject.graph_dot, p.getA()).//
				s(IvMObject.graph_svg, diagram).//
				s(IvMObject.graph_visualisation_info, p.getB()).//
				s(IvMObject.trace_view_event_colour_map, p.getC());
	}
}