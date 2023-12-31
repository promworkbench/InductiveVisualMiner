package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.cost.CostModelFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTab;
import org.processmining.plugins.inductiveVisualMiner.export.IvMExporter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.IvMFilterBuilderFactory;
import org.processmining.plugins.inductiveVisualMiner.mode.Mode;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemActivity;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLog;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemLogMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemModelMove;
import org.processmining.plugins.inductiveVisualMiner.popup.PopupItemStartEnd;
import org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.VisualMinerWrapper;

/**
 * Use this class to extend the visual Miner:
 * 
 * - discovery techniques;
 * 
 * - chain of computation steps, done out of the gui thread;
 * 
 * - state keeps track of computation results
 * 
 * - the gui panel
 * 
 * @author sander
 *
 */
public interface InductiveVisualMinerConfiguration extends ConfigurationWithDecorator {

	/**
	 * The list of available discovery techniques.
	 * 
	 * @return
	 */
	public List<VisualMinerWrapper> getDiscoveryTechniques();

	public VisualMinerWrapper[] getDiscoveryTechniquesArray();

	/**
	 * The list of available filters.
	 * 
	 * @return
	 */
	public IvMFilterBuilderFactory getFilters();

	/**
	 * The list of available modes (arc colouring, which numbers to show on the
	 * model nodes, etc.)
	 * 
	 * @return
	 */
	public List<Mode> getModes();

	public Mode[] getModesArray();

	/**
	 * The list of items that are shown in the pop-ups of activities.
	 * 
	 * @return
	 */
	public List<PopupItemActivity> getPopupItemsActivity();

	/**
	 * The list of items that are shown in the pop-ups of the start and end
	 * node.
	 * 
	 * @return
	 */
	public List<PopupItemStartEnd> getPopupItemsStartEnd();

	public List<PopupItemLogMove> getPopupItemsLogMove();

	public List<PopupItemModelMove> getPopupItemsModelMove();

	public List<PopupItemLog> getPopupItemsLog();

	public List<DataAnalysisTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel>> getDataAnalysisTables();

	public IvMVirtualAttributeFactory getVirtualAttributes();

	public List<IvMExporter> getExporters();

	/**
	 * Set up the JComponent panel.
	 * 
	 * @param context
	 * @param state
	 * @param discoveryTechniques
	 * @param canceller
	 * @return
	 */
	public InductiveVisualMinerPanel getPanel();

	/**
	 * Set up the chain (DAG) of steps (chain links) that should be executed in
	 * the background and to update the gui.
	 * 
	 * @return
	 */
	public DataChain<InductiveVisualMinerConfiguration> getChain();

	public AlignmentComputer getAlignmentComputer();

	public List<CostModelFactory> getCostModelFactories();

}