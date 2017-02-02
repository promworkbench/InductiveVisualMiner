package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogMetrics;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class ExporterStatistics extends Exporter {

	private final InductiveVisualMinerState state;

	public ExporterStatistics(InductiveVisualMinerState state) {
		this.state = state;
	}

	@Override
	public String getDescription() {
		return "csv (statistics)";
	}

	protected String getExtension() {
		return "csv";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		ProcessTreeVisualisationInfo visualisationInfo = state.getVisualisationInfo();
		IvMEfficientTree tree = state.getTree();
		IvMLogInfo logInfo = state.getIvMLogInfoFiltered();

		PrintWriter w = new PrintWriter(file, "UTF-8");

		for (LocalDotNode activityNode : visualisationInfo.getAllActivityNodes()) {

			int node = activityNode.getUnode();
			long cardinality = IvMLogMetrics.getNumberOfTracesRepresented(tree, node, false, logInfo);
			long modelMoveCardinality = IvMLogMetrics.getModelMovesLocal(node, logInfo);
			w.println("activity\t" + state.getTree().getActivityName(node) + "\toccurrences\t" + cardinality
					+ "\tmodel moves\t" + modelMoveCardinality);
		}

		w.close();
	}

}
