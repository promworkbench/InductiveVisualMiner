package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;
import java.util.concurrent.Executor;

import org.processmining.framework.plugin.ProMCanceller;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTab;
import org.processmining.plugins.inductiveVisualMiner.export.IvMExporter;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFilter;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

/**
 * IvM configuration that contains the chainlink. To extend, please use the
 * InductiveVisualMinerConfigurationDefault class. This one is not guaranteed to
 * be stable.
 * 
 * @author sander
 *
 */
public abstract class InductiveVisualMinerConfigurationAbstract implements InductiveVisualMinerConfiguration {

	private final DataChain<InductiveVisualMinerConfiguration> chain;
	private final InductiveVisualMinerPanel panel;
	private final List<VisualMinerWrapper> discoveryTechniques;
	private final AlignmentComputer alignmentComputer;
	private final List<Mode> modes;
	private final List<PopupItemActivity> popupItemsActivity;
	private final List<PopupItemStartEnd> popupItemsStartEnd;
	private final List<PopupItemLogMove> popupItemsLogMove;
	private final List<PopupItemModelMove> popupItemsModelMove;
	private final List<PopupItemLog> popupItemsLog;
	private final List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> dataAnalyses;
	private final List<CostModelFactory> costModelFactories;
	private final List<PreMiningFilter> preMiningFilters;
	private final List<IvMExporter> exporters;
	private final IvMVirtualAttributeFactory virtualAttributeFactory;
	private final IvMDecoratorI decorator;

	public InductiveVisualMinerConfigurationAbstract(ProMCanceller canceller, Executor executor) {
		discoveryTechniques = createDiscoveryTechniques();
		preMiningFilters = createPreMiningFilters();
		alignmentComputer = createAlignmentComputer();
		modes = createModes();
		popupItemsActivity = createPopupItemsActivity();
		popupItemsStartEnd = createPopupItemsStartEnd();
		popupItemsLogMove = createPopupItemsLogMove();
		popupItemsModelMove = createPopupItemsModelMove();
		popupItemsLog = createPopupItemsLog();
		dataAnalyses = createDataAnalysisTables();
		costModelFactories = createCostModelFactories();
		virtualAttributeFactory = createVirtualAttributes();
		exporters = createExporters();
		decorator = createDecorator();

		panel = createPanel(canceller);
		chain = createChain(panel, canceller, executor, preMiningFilters);
	}

	protected abstract List<PreMiningFilter> createPreMiningFilters();

	protected abstract List<VisualMinerWrapper> createDiscoveryTechniques();

	protected abstract AlignmentComputer createAlignmentComputer();

	protected abstract List<Mode> createModes();

	protected abstract List<PopupItemActivity> createPopupItemsActivity();

	protected abstract List<PopupItemStartEnd> createPopupItemsStartEnd();

	protected abstract List<PopupItemLogMove> createPopupItemsLogMove();

	protected abstract List<PopupItemModelMove> createPopupItemsModelMove();

	protected abstract List<PopupItemLog> createPopupItemsLog();

	protected abstract List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> createDataAnalysisTables();

	protected abstract List<CostModelFactory> createCostModelFactories();

	protected abstract List<IvMExporter> createExporters();

	protected abstract InductiveVisualMinerPanel createPanel(ProMCanceller canceller);

	protected abstract IvMDecoratorI createDecorator();

	protected abstract DataChain<InductiveVisualMinerConfiguration> createChain(InductiveVisualMinerPanel panel,
			ProMCanceller canceller, Executor executor, List<PreMiningFilter> preMiningFilters);

	protected abstract IvMVirtualAttributeFactory createVirtualAttributes();

	@Override
	final public DataChain<InductiveVisualMinerConfiguration> getChain() {
		return chain;
	}

	@Override
	final public InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	@Override
	final public List<VisualMinerWrapper> getDiscoveryTechniques() {
		return discoveryTechniques;
	}

	@Override
	final public VisualMinerWrapper[] getDiscoveryTechniquesArray() {
		VisualMinerWrapper[] result = new VisualMinerWrapper[discoveryTechniques.size()];
		return discoveryTechniques.toArray(result);
	}

	@Override
	final public AlignmentComputer getAlignmentComputer() {
		return alignmentComputer;
	}

	@Override
	final public List<Mode> getModes() {
		return modes;
	}

	@Override
	final public Mode[] getModesArray() {
		Mode[] result = new Mode[modes.size()];
		return modes.toArray(result);
	}

	@Override
	final public List<PreMiningFilter> getPreMiningFilters() {
		return preMiningFilters;
	}

	@Override
	final public List<PopupItemActivity> getPopupItemsActivity() {
		return popupItemsActivity;
	}

	@Override
	final public List<PopupItemStartEnd> getPopupItemsStartEnd() {
		return popupItemsStartEnd;
	}

	@Override
	final public List<PopupItemLogMove> getPopupItemsLogMove() {
		return popupItemsLogMove;
	}

	@Override
	final public List<PopupItemModelMove> getPopupItemsModelMove() {
		return popupItemsModelMove;
	}

	@Override
	final public List<PopupItemLog> getPopupItemsLog() {
		return popupItemsLog;
	}

	@Override
	final public List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> getDataAnalysisTables() {
		return dataAnalyses;
	}

	@Override
	public List<CostModelFactory> getCostModelFactories() {
		return costModelFactories;
	}

	@Override
	final public List<IvMExporter> getExporters() {
		return exporters;
	}

	@Override
	final public IvMVirtualAttributeFactory getVirtualAttributes() {
		return virtualAttributeFactory;
	}

	@Override
	final public IvMDecoratorI getDecorator() {
		return decorator;
	}
}