package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTab;
import org.processmining.plugins.inductiveVisualMiner.export.IvMExporter;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderFactory;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

public class InductiveVisualMinerConfigurationFake implements InductiveVisualMinerConfiguration {

	private InductiveVisualMinerConfigurationDefault phantom;
	private DataChain<InductiveVisualMinerConfiguration> chain;
	private InductiveVisualMinerPanel panel;
	private ProMCanceller canceller;
	private Executor executor;
	private List<VisualMinerWrapper> discoveryTechniques;

	public InductiveVisualMinerConfigurationFake(DataState state, InductiveVisualMinerPanel panel,
			ProMCanceller canceller, Executor executor) {
		this.executor = executor;
		this.canceller = canceller;
		this.panel = panel;
		phantom = new InductiveVisualMinerConfigurationDefault(canceller, executor);
		this.discoveryTechniques = phantom.getDiscoveryTechniques();
	}

	public List<VisualMinerWrapper> getDiscoveryTechniques() {
		return discoveryTechniques;
	}

	public VisualMinerWrapper[] getDiscoveryTechniquesArray() {
		VisualMinerWrapper[] result = new VisualMinerWrapper[discoveryTechniques.size()];
		return discoveryTechniques.toArray(result);
	}

	public IvMFilterBuilderFactory getFilters() {
		return phantom.getFilters();
	}

	public List<Mode> getModes() {
		return phantom.getModes();
	}

	public Mode[] getModesArray() {
		return phantom.getModesArray();
	}

	public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	public DataChain<InductiveVisualMinerConfiguration> getChain() {
		if (chain == null) {
			chain = phantom.createChain(panel, canceller, executor);
		}
		return chain;
	}

	public void setDiscoveryTechniques(List<VisualMinerWrapper> discoveryTechniques) {
		this.discoveryTechniques = discoveryTechniques;
	}

	public void setDiscoveryTechniques(VisualMinerWrapper[] miners) {
		this.discoveryTechniques = new ArrayList<>(Arrays.asList(miners));
	}

	public List<PopupItemActivity> getPopupItemsActivity() {
		return phantom.getPopupItemsActivity();
	}

	public List<PopupItemStartEnd> getPopupItemsStartEnd() {
		return phantom.getPopupItemsStartEnd();
	}

	public List<PopupItemLogMove> getPopupItemsLogMove() {
		return phantom.getPopupItemsLogMove();
	}

	public List<PopupItemModelMove> getPopupItemsModelMove() {
		return phantom.getPopupItemsModelMove();
	}

	public List<PopupItemLog> getPopupItemsLog() {
		return phantom.getPopupItemsLog();
	}

	public AlignmentComputer getAlignmentComputer() {
		return phantom.getAlignmentComputer();
	}

	public List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> getDataAnalysisTables() {
		return phantom.getDataAnalysisTables();
	}

	public List<CostModelFactory> getCostModelFactories() {
		return phantom.getCostModelFactories();
	}

	public List<IvMExporter> getExporters() {
		return phantom.getExporters();
	}

	public IvMVirtualAttributeFactory getVirtualAttributes() {
		return phantom.getVirtualAttributes();
	}

	public IvMDecoratorI getDecorator() {
		return phantom.getDecorator();
	}

}