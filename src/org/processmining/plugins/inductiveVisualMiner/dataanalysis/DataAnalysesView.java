package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

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
			boolean switchable = factory.isSwitchable();

			OnOffPanel<?> onOffPanel = createView(configuration.getDecorator(), table, analysisName, explanation,
					switchable);
			onOffPanel.off();
			tabbedPane.addTab(analysisName, onOffPanel);

			tables.put(analysisName, table);
			onOffPanels.put(analysisName, onOffPanel);
		}

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	private static OnOffPanel<DataAnalysisView> createView(IvMDecoratorI decorator, DataAnalysisTable table,
			String analysisName, String explanation, boolean switchable) {
		OnOffPanel<DataAnalysisView> result = new OnOffPanel<>(decorator,
				new DataAnalysisView(decorator, table, explanation), switchable);
		if (switchable) {
			result.setOffMessage("Compute " + analysisName);
			result.getSwitch().setSelected(false);
		} else {
			result.setOffMessage("Computing " + analysisName);
		}
		return result;
	}

	/**
	 * Sets the data and enables the table. As this is an extensible method, the
	 * state is provided.
	 * 
	 * @param analysisName
	 * @param state
	 */
	public void setData(String analysisName, DataState state) {
		if (tables.containsKey(analysisName)) {
			DataAnalysisTable table = tables.get(analysisName);
			boolean successful = table.setData(state);

			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			if (!onOffPanel.isSwitchable() || successful) {
				onOffPanel.on();
			}
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

	public void addSwitcherListener(String analysisName, ActionListener actionListener) {
		if (onOffPanels.containsKey(analysisName)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.getSwitch().addActionListener(actionListener);
		}
	}

	public void setSwitcherMessage(String analysisName, String message) {
		if (onOffPanels.containsKey(analysisName)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.setOffMessage(message);
		}
	}

	public void setSwitcherEnabled(String analysisName, boolean cohortAnalysisEnabled) {
		if (onOffPanels.containsKey(analysisName)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.getSwitch().setSelected(cohortAnalysisEnabled);
		}
	}
}