package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.DataAnalysisTabCohorts;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.DataAnalysisTabCohorts.DataTableCohort;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.FilterCommunicator;

import gnu.trove.map.hash.THashMap;

public class DataAnalysesView<C, P> extends SideWindow {

	private static final long serialVersionUID = -1113805892324898124L;

	private final Map<String, DataAnalysisTable<?, C, P>> tables = new THashMap<>();
	private final Map<String, OnOffPanel<?>> onOffPanels = new THashMap<>();
	private final JTabbedPane tabbedPane;

	public DataAnalysesView(Component parent, List<DataAnalysisTab<?, C, P>> factories, IvMDecoratorI decorator) {
		super(parent, "Data analysis - " + InductiveVisualMinerPanel.title);

		tabbedPane = new JTabbedPane();

		for (DataAnalysisTab<?, C, P> factory : factories) {
			DataAnalysisTable<?, C, P> table = createAndFillTable(factory, this, decorator);

			String analysisName = factory.getAnalysisName();
			String explanation = factory.getExplanation();
			boolean switchable = table.isSwitchable() != null;

			OnOffPanel<DataAnalysisView> onOffPanel = createView(decorator, table, analysisName, explanation,
					switchable);
			onOffPanel.off();
			table.setDataAnalysisView(onOffPanel.getOnPanel());
			tabbedPane.addTab(analysisName, onOffPanel);

			tables.put(analysisName, table);
			onOffPanels.put(analysisName, onOffPanel);
		}

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

	public static <O, C, P> DataAnalysisTable<O, C, P> createAndFillTable(DataAnalysisTab<O, C, P> factory,
			DataAnalysesView<C, P> dataAnalysesView, IvMDecoratorI decorator) {
		DataAnalysisTable<O, C, P> table = factory.createTable(dataAnalysesView, decorator);
		List<DataRowBlock<O, C, P>> blocks = factory.createRowBlocks(table);
		for (DataRowBlockComputer<O, C, P> rowBlockComputer : factory.createRowBlockComputers()) {
			blocks.add(rowBlockComputer.createDataRowBlock(table));
		}

		table.getModel().setBlocks(blocks);

		return table;
	}

	private static OnOffPanel<DataAnalysisView> createView(IvMDecoratorI decorator, DataAnalysisTable<?, ?, ?> table,
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

	public Collection<DataAnalysisTable<?, C, P>> getAnalyses() {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setChannel(FilterCommunicator<Pair<Cohort, Boolean>, Void, Void, Void> channel) {
		DataAnalysisTable<?, C, P> table = tables.get(DataAnalysisTabCohorts.name);
		if (table != null && table instanceof DataAnalysisTabCohorts.DataTableCohort) {
			((DataTableCohort) table).setChannel(channel);
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

	public void setSwitcherEnabled2(String analysisName, boolean cohortAnalysisEnabled) {
		if (onOffPanels.containsKey(analysisName)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.getSwitch().setSelected(cohortAnalysisEnabled);
		}
	}
}