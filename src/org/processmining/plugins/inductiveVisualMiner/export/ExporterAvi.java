package org.processmining.plugins.inductiveVisualMiner.export;

import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

public class ExporterAvi extends IvMExporter {

	private final InductiveVisualMinerConfiguration configuration;

	public ExporterAvi(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.graph_svg_aligned, IvMObject.selected_visualisation_mode,
				IvMObject.graph_dot_aligned, IvMObject.animation, IvMObject.animation_scaler,
				IvMObject.graph_visualisation_info_aligned, IvMObject.aligned_log_filtered, IvMObject.trace_colour_map,
				IvMObject.visualisation_data, IvMObject.model, IvMObject.selected_model_selection };
	}

	@Override
	protected IvMObject<?>[] createNonTriggerObjects() {
		return Mode.gatherInputsRequested(configuration);
	}

	@Override
	public String getDescription() {
		return "avi (animation)";
	};

	protected String getExtension() {
		return "avi";
	}

	@Override
	public void export(IvMObjectValues inputs, final InductiveVisualMinerAnimationPanel panel, final File file)
			throws Exception {

		//save avi asynchronously
		final SVGDiagram svg = inputs.get(IvMObject.graph_svg_aligned);
		final Mode mode = inputs.get(IvMObject.selected_visualisation_mode);
		final Dot dot = inputs.get(IvMObject.graph_dot_aligned);
		final GraphVizTokens tokens = inputs.get(IvMObject.animation);
		final Scaler scaler = inputs.get(IvMObject.animation_scaler);
		final ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info_aligned);
		final IvMLogFiltered filteredLog = inputs.get(IvMObject.aligned_log_filtered);
		final TraceColourMap trace2colour = inputs.get(IvMObject.trace_colour_map);
		final AlignedLogVisualisationData visualisationData = inputs.get(IvMObject.visualisation_data);

		final boolean updateWithTimeStep = mode.isVisualisationDataUpdateWithTimeStep();

		IvMObjectValues subInputs = inputs.getIfPresent(mode.getOptionalObjects());
		final ProcessTreeVisualisationParameters visualisationParameters = mode
				.getVisualisationParametersWithAlignments(subInputs);
		final IvMModel model = inputs.get(IvMObject.model);
		final Selection selection = inputs.get(IvMObject.selected_model_selection);

		new Thread(new Runnable() {
			public void run() {
				try {
					boolean success;
					if (updateWithTimeStep) {
						success = ExportAnimation.saveAVItoFileUpdating(filteredLog, trace2colour, tokens,
								visualisationInfo, mode, svg, dot, file, panel, scaler, visualisationData,
								visualisationParameters, model, selection);
					} else {
						success = ExportAnimation.saveAVItoFile(filteredLog, trace2colour, tokens, visualisationInfo,
								mode, svg, dot, file, panel, scaler);
					}
					if (!success) {
						System.out.println("animation failed; file deleted");
						file.delete();
					}

				} catch (IOException | NoninvertibleTransformException e) {
					e.printStackTrace();
				}
			}
		}, "IvM animation exporter thread").start();
	}
}