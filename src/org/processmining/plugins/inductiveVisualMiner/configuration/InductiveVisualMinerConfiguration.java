package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentComputer;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.chain.DataChain;
import org.processmining.plugins.inductiveVisualMiner.chain.DataState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.PreMiningFilter;
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
public interface InductiveVisualMinerConfiguration {

	/**
	 * The list of available discovery techniques.
	 * 
	 * @return
	 */
	public List<VisualMinerWrapper> getDiscoveryTechniques();

	public VisualMinerWrapper[] getDiscoveryTechniquesArray();

	/**
	 * The list of available pre-mining filters.
	 * 
	 * @return
	 */
	public List<PreMiningFilter> getPreMiningFilters();

	/**
	 * The list of available highlighting filters.
	 * 
	 * @return
	 */
	public List<HighlightingFilter> getHighlightingFilters();

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

	public List<DataAnalysisTableFactory> getDataAnalysisTables();

	public IvMVirtualAttributeFactory getVirtualAttributes();

	/**
	 * Set up the state that keeps the data related to the visual Miner.
	 * 
	 * @param log
	 * @return
	 */
	public DataState getState();

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
	public DataChain getChain();

	public AlignmentComputer getAlignmentComputer();

	public IvMDecoratorI getDecorator();

}