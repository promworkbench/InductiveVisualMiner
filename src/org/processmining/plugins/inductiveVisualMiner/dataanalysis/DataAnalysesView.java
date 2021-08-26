package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.CohortAnalysis2HighlightingFilterHandler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

import gnu.trove.map.hash.THashMap;

public class DataAnalysesView<C, P> extends SideWindow {

	private static final long serialVersionUID = -1113805892324898124L;

	private final Map<String, DataTable<C, P>> tables = new THashMap<>();
	private final Map<String, OnOffPanel<?>> onOffPanels = new THashMap<>();
	private final JTabbedPane tabbedPane;

	public DataAnalysesView(Component parent, List<DataTab<C, P>> factories, IvMDecoratorI decorator) {
		super(parent, "Data analysis - " + InductiveVisualMinerPanel.title);

		tabbedPane = new JTabbedPane();

		for (DataTab<C, P> factory : factories) {
			DataTable<C, P> table = createTable(factory);

			String analysisName = factory.getAnalysisName();
			String explanation = factory.getExplanation();
			boolean switchable = factory.isSwitchable();

			OnOffPanel<?> onOffPanel = createView(decorator, table, analysisName, explanation, switchable);
			onOffPanel.off();
			tabbedPane.addTab(analysisName, onOffPanel);

			tables.put(analysisName, table);
			onOffPanels.put(analysisName, onOffPanel);
		}

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	private DataTable<C, P> createTable(DataTab<C, P> factory) {
		DataTable<C, P> table = factory.createTable(this);
		List<DataRowBlock<C, P>> blocks = factory.createRowBlocks(table);
		for (DataRowBlockComputer<C, P> rowBlockComputer : factory.createRowBlockComputers()) {
			blocks.add(rowBlockComputer.createDataRowBlock(table));
		}

		table.getModel().setBlocks(blocks);

		return table;
	}

	private static OnOffPanel<DataAnalysisView> createView(IvMDecoratorI decorator, DataTable<?, ?> table,
			String analysisName, String explanation, boolean switchable) {
		OnOffPanel<DataAnalysisView> result = new OnOffPanel<>(decorator,
				new DataAnalysisView(decorator, table, explanation), switchable);
		if (switchable) {
			result.setOffMessage("Compute " + analysisName);
			result.getSwitch().setSelected(false);
		} else {
			result.setOffMessage("Computing " + analysisName + "..");
		}
		return result;
	}

	public Collection<DataTable<C, P>> getAnalyses() {
		return tables.values();
	}

	public void showAnalysis(String name) {
		if (onOffPanels.containsKey(name)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(name);
			tabbedPane.setSelectedComponent(onOffPanel);
		}
	}

	public OnOffPanel<?> getOnOffPanel(String name) {
		return onOffPanels.get(name);
	}

	public void setCohortAnalysis2HighlightingFilterHandler(
			CohortAnalysis2HighlightingFilterHandler showCohortHighlightingFilterHandler) {
		for (DataTable<C, P> table : tables.values()) {
			//			if (table instanceof CohortAnalysisTable) {
			//				((CohortAnalysisTable) table)
			//						.setCohortAnalysis2HighlightingFilterHandler(showCohortHighlightingFilterHandler);
			//			}
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