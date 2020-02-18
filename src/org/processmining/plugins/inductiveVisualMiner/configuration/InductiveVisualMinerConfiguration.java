package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.Chain;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilter;
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
	public List<IvMFilter> getPreMiningFilters();

	/**
	 * The list of available highlighting filters.
	 * 
	 * @return
	 */
	public List<IvMFilter> getHighlightingFilters();

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

	/**
	 * Set up the state that keeps the data related to the visual Miner.
	 * 
	 * @param log
	 * @return
	 */
	public InductiveVisualMinerState getState();

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
	 * Set up the chain (DAG) of steps that should be executed in the
	 * background, concurrently with one another and with the gui.
	 * 
	 * - every path in the DAG should lead to the CLxxDone chainlink.
	 * 
	 * - the typical way to extend the visual Miner is to connect the input(s)
	 * of your chainlink to your chainlink, and your chainlink to CLxxDone.
	 * 
	 * @return
	 */
	public Chain getChain();

}