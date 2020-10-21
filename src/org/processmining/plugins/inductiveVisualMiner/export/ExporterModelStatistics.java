package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.deckfour.xes.classification.XEventClass;
import org.processmining.plugins.InductiveMiner.MultiSet;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.alignedLogVisualisation.data.AlignedLogVisualisationDataImplFrequencies;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.Gather;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeNode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItem;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInput;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemInputStartEnd;
import org.processmining.plugins.inductiveVisualMiner.visualisation.LocalDotNode;
import org.processmining.plugins.inductiveVisualMiner.visualisation.ProcessTreeVisualisationInfo;

public class ExporterModelStatistics extends Exporter {

	private final InductiveVisualMinerConfiguration configuration;

	public ExporterModelStatistics(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getDescription() {
		return "csv (model statistics & popups)";
	}

	protected String getExtension() {
		return "csv";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		assert (configuration.getState().isPerformanceReady() && configuration.getState().isAlignmentReady());
		PerformanceWrapper performance = configuration.getState().getPerformance();
		ProcessTreeVisualisationInfo visualisationInfo = configuration.getState().getVisualisationInfo();
		IvMModel model = configuration.getState().getModel();
		IvMLogInfo logInfo = configuration.getState().getIvMLogInfoFiltered();

		AlignedLogVisualisationDataImplFrequencies frequencies = new AlignedLogVisualisationDataImplFrequencies(model,
				logInfo);

		PrintWriter w = new PrintWriter(file, "UTF-8");
		char sep = '\t';

		for (LocalDotNode activityNode : visualisationInfo.getAllActivityNodes()) {
			int node = activityNode.getUnode();
			long cardinality = frequencies.getNodeLabel(node, false).getB();
			long modelMoveCardinality = frequencies.getModelMoveEdgeLabel(node).getB();
			w.print(configuration.getState().getModel().getActivityName(node));
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
					w.print(configuration.getState().getModel().getActivityName(node));
					PopupItemInputActivity input = new PopupItemInputActivity(node);
					printPopupItems(w, configuration.getPopupItemsActivity(), input, sep);
				}
			}

			//log
			{
				w.println();
				w.println("-- log pop-up --");
				PopupItemInputLog input = new PopupItemInputLog();
				printPopupItems(w, configuration.getPopupItemsLog(), input, sep);
			}

			//start-end pop-up
			{
				w.println();
				w.println("-- start-end pop-up --");
				PopupItemInputStartEnd input = new PopupItemInputStartEnd();
				printPopupItems(w, configuration.getPopupItemsStartEnd(), input, sep);
			}
		}

		w.close();
	}

	public <T> void printPopupItems(PrintWriter w, List<? extends PopupItem<T>> popupItems, PopupItemInput<T> input,
			char sep) {
		for (PopupItem<T> popupItem : popupItems) {
			String[][] rows = popupItem.get(configuration.getState(), input);
			for (String[] row : rows) {
				if (row != null && row.length > 0 && row[0] != null) {
					w.print(sep);
					w.println(StringUtils.join(row, sep));
				}
			}
		}
	}

}
