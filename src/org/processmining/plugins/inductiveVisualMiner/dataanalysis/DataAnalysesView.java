package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import gnu.trove.map.hash.THashMap;

public class DataAnalysesView extends SideWindow {

	private static final long serialVersionUID = -1113805892324898124L;

	private final Map<String, DataAnalysisTable> tables = new THashMap<>();
	private final Map<String, OnOffPanel<?>> onOffPanels = new THashMap<>();
	private final JTabbedPane tabbedPane;

	public DataAnalysesView(Component parent, InductiveVisualMinerConfiguration configuration) {
		super(parent, "Data analysis - " + InductiveVisualMinerPanel.title);

		tabbedPane = new JTabbedPane();

		for (DataAnalysisTableFactory factory : configuration.getDataAnalysisTables()) {
			DataAnalysisTable table = factory.create();
			String analysisName = factory.getAnalysisName();
			String explanation = factory.getExplanation();

			OnOffPanel<?> onOffPanel = createView(table, explanation);
			onOffPanel.setOffMessage("Computing..");
			onOffPanel.off();
			tabbedPane.addTab(analysisName, onOffPanel);

			tables.put(analysisName, table);
			onOffPanels.put(analysisName, onOffPanel);
		}

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	private static OnOffPanel<DataAnalysisView> createView(DataAnalysisTable table, String explanation) {
		return new OnOffPanel<>(new DataAnalysisView(table, explanation));
	}

	/**
	 * Sets the data and enables the table.
	 * 
	 * @param analysisName
	 * @param state
	 */
	public void setData(String analysisName, InductiveVisualMinerState state) {
		if (tables.containsKey(analysisName)) {
			DataAnalysisTable table = tables.get(analysisName);
			table.setData(state);

			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.on();
		}
	}

	public void invalidate(String analysisName) {
		if (onOffPanels.containsKey(analysisName)) {
			onOffPanels.get(analysisName).off();
			tables.get(analysisName).invalidateData();
		}
	}

	public void showAnalysis(String name) {
		if (onOffPanels.containsKey(name)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(name);
			tabbedPane.setSelectedComponent(onOffPanel);
		}
	}

	public void setCohortAnalysis2HighlightingFilterHandler(
			CohortAnalysis2HighlightingFilterHandler showCohortHighlightingFilterHandler) {
		for (DataAnalysisTable table : tables.values()) {
			if (table instanceof CohortAnalysisTable) {
				((CohortAnalysisTable) table)
						.setCohortAnalysis2HighlightingFilterHandler(showCohortHighlightingFilterHandler);
			}
		}
	}
}