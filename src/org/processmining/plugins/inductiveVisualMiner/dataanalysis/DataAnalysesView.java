package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTabbedPane;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import gnu.trove.map.hash.THashMap;

public class DataAnalysesView extends SideWindow {

	private static final long serialVersionUID = -1113805892324898124L;

	private final Map<String, DataAnalysisTable<?>> tables = new THashMap<>();
	private final Map<String, OnOffPanel<?>> onOffPanels = new THashMap<>();

	public DataAnalysesView(Component parent, InductiveVisualMinerConfiguration configuration) {
		super(parent, "Data analysis - " + InductiveVisualMinerPanel.title);

		JTabbedPane tabbedPane = new JTabbedPane();

		for (DataAnalysisTableFactory<?> factory : configuration.getDataAnalysisTables()) {
			DataAnalysisTable<?> table = factory.create();
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

	private static <D> OnOffPanel<DataAnalysisView<D>> createView(DataAnalysisTable<D> table, String explanation) {
		return new OnOffPanel<>(new DataAnalysisView<>(table, explanation));
	}

	public void initialiseAttributes(AttributesInfo attributesInfo) {
		for (DataAnalysisTable<?> table : tables.values()) {
			table.setAttributesInfo(attributesInfo);
		}
	}

	@SuppressWarnings("unchecked")
	public <D> void setData(String analysisName, D data) {
		if (tables.containsKey(analysisName)) {
			DataAnalysisTable<D> table = (DataAnalysisTable<D>) tables.get(analysisName);
			table.setData(data);

			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.on();
		}
	}

	public void invalidate(String analysisName) {
		if (onOffPanels.containsKey(analysisName)) {
			OnOffPanel<?> onOffPanel = onOffPanels.get(analysisName);
			onOffPanel.off();
		}
	}

}