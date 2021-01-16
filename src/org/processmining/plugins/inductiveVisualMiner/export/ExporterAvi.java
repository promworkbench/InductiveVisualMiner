package org.processmining.plugins.inductiveVisualMiner.export;

import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.Selection;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationData;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.tracecolouring.TraceColourMap;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationParameters;

import com.kitfox.svg.SVGDiagram;

public class ExporterAvi extends Exporter {

	private final DataState state;

	public ExporterAvi(DataState state) {
		this.state = state;
	}

	@Override
	public String getDescription() {
		return "avi (animation)";
	};

	protected String getExtension() {
		return "avi";
	}

	public void export(NavigableSVGPanel panel, final File file) throws Exception {
		final InductiveVisualMinerAnimationPanel panel2 = (InductiveVisualMinerAnimationPanel) panel;

		//save avi asynchronously
		final SVGDiagram svg = panel2.getImage();
		final Mode colourMode = state.getMode();
		final Dot dot = panel2.getDot();
		final GraphVizTokens tokens = state.getAnimationGraphVizTokens();
		final Scaler scaler = state.getAnimationScaler();
		final ProcessTreeVisualisationInfo info = state.getVisualisationInfo();
		final IvMLogFiltered filteredLog = state.getIvMLogFiltered();
		final TraceColourMap trace2colour = state.getTraceColourMap();
		final boolean updateWithTimeStep = state.getMode().isUpdateWithTimeStep();
		final AlignedLogVisualisationData visualisationData = state.getVisualisationData().clone();
		Mode mode = state.getMode();
		final ProcessTreeVisualisationParameters visualisationParameters = mode
				.getVisualisationParametersWithAlignments(
						state.getObject(IvMObject.carte_blanche).getIfPresent(mode.inputsRequested()));
		final ProcessTreeVisualisationInfo visualisationInfo = state.getVisualisationInfo();
		final IvMModel model = state.getModel();
		final Selection selection = state.getSelection();
		new Thread(new Runnable() {
			public void run() {
				try {
					boolean success;
					if (updateWithTimeStep) {
						success = ExportAnimation.saveAVItoFileUpdating(filteredLog, trace2colour, tokens, info,
								colourMode, svg, dot, file, panel2, scaler, visualisationData, visualisationParameters,
								visualisationInfo, model, selection);
					} else {
						success = ExportAnimation.saveAVItoFile(filteredLog, trace2colour, tokens, info, colourMode,
								svg, dot, file, panel2, scaler);
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
