package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Type;
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
		assert (state.isPerformanceReady() && state.isAlignmentReady());
		PerformanceWrapper performance = state.getPerformance();
		ProcessTreeVisualisationInfo visualisationInfo = state.getVisualisationInfo();
		IvMModel model = state.getModel();
		IvMLogInfo logInfo = state.getIvMLogInfoFiltered();

		AlignedLogVisualisationDataImplFrequencies frequencies = new AlignedLogVisualisationDataImplFrequencies(model,
				logInfo);

		PrintWriter w = new PrintWriter(file, "UTF-8");
		char sep = '\t';

		for (LocalDotNode activityNode : visualisationInfo.getAllActivityNodes()) {
			int node = activityNode.getUnode();
			long cardinality = frequencies.getNodeLabel(node, false).getB();
			long modelMoveCardinality = frequencies.getModelMoveEdgeLabel(node).getB();
			w.print(state.getModel().getActivityName(node));
			w.print(sep + "occurrences" + sep + cardinality);
			w.print(sep + "model moves" + sep + modelMoveCardinality);

			for (Type type : Type.values()) {
				for (Gather gather : Gather.values()) {
					long m = performance.getMeasure(type, gather, node);
					if (m > -1) {
						w.print(sep + gather.toString() + " " + type.toString() + " time" + sep
								+ Performance.timeToString(m));
					} else {
						w.print(sep + gather.toString() + " " + type.toString() + " time" + sep);
					}
				}
			}

			w.println();
		}

		//log moves
		w.println();
		MultiSet<XEventClass> logMoves = new MultiSet<XEventClass>();
		for (MultiSet<XEventClass> l : logInfo.getLogMoves().values()) {
			logMoves.addAll(l);
		}
		for (XEventClass e : logMoves.sortByCardinality()) {
			w.println(e.getId() + sep + "log moves" + sep + logMoves.getCardinalityOf(e));
		}

		w.close();
	}

}
