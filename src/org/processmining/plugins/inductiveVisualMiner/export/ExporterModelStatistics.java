package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class ExporterModelStatistics extends IvMExporter {

	protected final InductiveVisualMinerConfiguration configuration;

	public ExporterModelStatistics(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getDescription() {
		return "csv (model statistics & popups)";
	}

	@Override
	protected String getExtension() {
		return "csv";
	}

	@Override
	protected IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.performance, IvMObject.graph_visualisation_info, IvMObject.model,
				IvMObject.aligned_log_info_filtered, IvMObject.popups };
	}

	@Override
	protected IvMObject<?>[] createNonTriggerObjects() {
		return new IvMObject<?>[] {};
	}

	@Override
	public void export(IvMObjectValues inputs, InductiveVisualMinerAnimationPanel panel, File file) throws Exception {
		PerformanceWrapper performance = inputs.get(IvMObject.performance);
		ProcessTreeVisualisationInfo visualisationInfo = inputs.get(IvMObject.graph_visualisation_info);
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogInfo logInfo = inputs.get(IvMObject.aligned_log_info_filtered);
		@SuppressWarnings("unchecked")
		Map<PopupItemInput<?>, List<String[]>> popups = inputs.get(IvMObject.popups);

		AlignedLogVisualisationDataImplFrequencies frequencies = new AlignedLogVisualisationDataImplFrequencies(model,
				logInfo);

		PrintWriter w = new PrintWriter(file, "UTF-8");
		char sep = '\t';

		for (LocalDotNode activityNode : visualisationInfo.getAllActivityNodes()) {
			int node = activityNode.getUnode();
			long cardinality = frequencies.getNodeLabel(node, false).getB();
			long modelMoveCardinality = frequencies.getModelMoveEdgeLabel(node).getB();
			w.print(model.getActivityName(node));
			w.print(sep + "occurrences" + sep + cardinality);
			w.print(sep + "model moves" + sep + modelMoveCardinality);

			for (TypeNode type : TypeNode.values()) {
				for (Gather gather : Gather.values()) {
					long m = performance.getNodeMeasure(type, gather, node);
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
		{
			w.println();
			MultiSet<XEventClass> logMoves = new MultiSet<XEventClass>();
			for (MultiSet<XEventClass> l : logInfo.getLogMoves().values()) {
				logMoves.addAll(l);
			}
			for (XEventClass e : logMoves.sortByCardinality()) {
				w.println(e.getId() + sep + "log moves" + sep + logMoves.getCardinalityOf(e));
			}
		}

		//popups
		{
			{
				w.println();
				w.println("-- activity pop-ups --");
				//activities
				for (LocalDotNode activityNode : visualisationInfo.getAllActivityNodes()) {
					int node = activityNode.getUnode();
					w.print(model.getActivityName(node));
					PopupItemInputActivity itemInput = new PopupItemInputActivity(node);
					printPopupItems(w, popups.get(itemInput), sep, inputs);
				}
			}

			//log
			{
				w.println();
				w.println("-- log pop-up --");
				PopupItemInputLog itemInput = new PopupItemInputLog();
				printPopupItems(w, popups.get(itemInput), sep, inputs);
			}

			//start-end pop-up
			{
				w.println();
				w.println("-- start-end pop-up --");
				PopupItemInputStartEnd itemInput = new PopupItemInputStartEnd();
				printPopupItems(w, popups.get(itemInput), sep, inputs);
			}
		}

		w.close();
	}

	private void printPopupItems(PrintWriter w, List<String[]> rows, char sep, IvMObjectValues inputs) {
		for (String[] row : rows) {
			if (row != null && row.length > 0 && row[0] != null) {
				w.print(sep);
				w.println(StringUtils.join(row, sep));
			}
		}
	}
}