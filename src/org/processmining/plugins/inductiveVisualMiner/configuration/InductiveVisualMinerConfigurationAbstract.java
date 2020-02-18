package org.processmining.plugins.inductiveVisualMiner.configuration;

import java.util.List;
import java.util.concurrent.Executor;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.ProMCanceller;
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
 * IvM configuration that contains the chainlink. To extend, please use the
 * InductiveVisualMinerConfigurationDefault class. This one is not guaranteed to
 * be stable.
 * 
 * @author sander
 *
 */
public abstract class InductiveVisualMinerConfigurationAbstract implements InductiveVisualMinerConfiguration {

	private final Chain chain;
	private final InductiveVisualMinerState state;
	private final InductiveVisualMinerPanel panel;
	private final List<VisualMinerWrapper> discoveryTechniques;
	private final List<Mode> modes;
	private final List<PopupItemActivity> popupItemsActivity;
	private final List<PopupItemStartEnd> popupItemsStartEnd;
	private final List<PopupItemLogMove> popupItemsLogMove;
	private final List<PopupItemModelMove> popupItemsModelMove;
	private final List<PopupItemLog> popupItemsLog;
	private final List<IvMFilter> preMiningFilters;
	private final List<IvMFilter> highlightingFilters;

	public InductiveVisualMinerConfigurationAbstract(XLog log, ProMCanceller canceller, Executor executor) {
		discoveryTechniques = createDiscoveryTechniques();
		preMiningFilters = createPreMiningFilters();
		highlightingFilters = createHighlightingFilters();
		modes = createModes();
		popupItemsActivity = createPopupItemsActivity();
		popupItemsStartEnd = createPopupItemsStartEnd();
		popupItemsLogMove = createPopupItemsLogMove();
		popupItemsModelMove = createPopupItemsModelMove();
		popupItemsLog = createPopupItemsLog();

		state = createState(log);
		panel = createPanel(canceller);
		chain = createChain(state, panel, canceller, executor, preMiningFilters, highlightingFilters);
	}

	protected abstract List<IvMFilter> createPreMiningFilters();

	protected abstract List<IvMFilter> createHighlightingFilters();

	protected abstract List<VisualMinerWrapper> createDiscoveryTechniques();

	protected abstract List<Mode> createModes();

	protected abstract List<PopupItemActivity> createPopupItemsActivity();

	protected abstract List<PopupItemStartEnd> createPopupItemsStartEnd();

	protected abstract List<PopupItemLogMove> createPopupItemsLogMove();

	protected abstract List<PopupItemModelMove> createPopupItemsModelMove();

	protected abstract List<PopupItemLog> createPopupItemsLog();

	protected abstract InductiveVisualMinerState createState(XLog log);

	protected abstract InductiveVisualMinerPanel createPanel(ProMCanceller canceller);

	protected abstract Chain createChain(InductiveVisualMinerState state, InductiveVisualMinerPanel panel,
			ProMCanceller canceller, Executor executor, List<IvMFilter> preMiningFilters,
			List<IvMFilter> highlightingFilters);

	@Override
	public final Chain getChain() {
		return chain;
	}

	@Override
	public final InductiveVisualMinerState getState() {
		return state;
	}

	@Override
	public final InductiveVisualMinerPanel getPanel() {
		return panel;
	}

	@Override
	public final List<VisualMinerWrapper> getDiscoveryTechniques() {
		return discoveryTechniques;
	}

	@Override
	public VisualMinerWrapper[] getDiscoveryTechniquesArray() {
		VisualMinerWrapper[] result = new VisualMinerWrapper[discoveryTechniques.size()];
		return discoveryTechniques.toArray(result);
	}

	@Override
	public final List<Mode> getModes() {
		return modes;
	}

	@Override
	public Mode[] getModesArray() {
		Mode[] result = new Mode[modes.size()];
		return modes.toArray(result);
	}

	@Override
	public final List<IvMFilter> getPreMiningFilters() {
		return preMiningFilters;
	}

	@Override
	public final List<IvMFilter> getHighlightingFilters() {
		return highlightingFilters;
	}

	@Override
	public List<PopupItemActivity> getPopupItemsActivity() {
		return popupItemsActivity;
	}

	@Override
	public List<PopupItemStartEnd> getPopupItemsStartEnd() {
		return popupItemsStartEnd;
	}

	@Override
	public List<PopupItemLogMove> getPopupItemsLogMove() {
		return popupItemsLogMove;
	}

	@Override
	public List<PopupItemModelMove> getPopupItemsModelMove() {
		return popupItemsModelMove;
	}

	@Override
	public List<PopupItemLog> getPopupItemsLog() {
		return popupItemsLog;
	}
}