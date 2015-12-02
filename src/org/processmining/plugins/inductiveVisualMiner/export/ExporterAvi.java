package org.processmining.plugins.inductiveVisualMiner.export;

import java.awt.geom.NoninvertibleTransformException;
import java.io.File;
import java.io.IOException;

import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.animation.GraphVizTokens;
import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

import com.kitfox.svg.SVGDiagram;

public class ExporterAvi extends Exporter {

	private final InductiveVisualMinerState state;

	public ExporterAvi(InductiveVisualMinerState state) {
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
		new Thread(new Runnable() {
			public void run() {
				try {
					if (!ExportAnimation.saveAVItoFile(filteredLog, tokens, info, colourMode, svg, dot, file, panel2,
							scaler)) {
						System.out.println("deleted");
						file.delete();
					}
				} catch (IOException | NoninvertibleTransformException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

}
