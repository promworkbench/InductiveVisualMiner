package org.processmining.plugins.inductiveVisualMiner.highlightingfilter;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

@HighlightingFilterAnnotation
public abstract class HighlightingFilter {

	/**
	 * Constructor. User is waiting when this function is called.
	 */
	public HighlightingFilter() {

	}

	/**
	 * Returns the name of this filter.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Initialises the JPanel containing the filter settings. Called
	 * asynchronously, but it is guaranteed to be the first abstract function
	 * that will be called (after constructor). No other function will be called
	 * until this one is finished.
	 * 
	 * @param log
	 * @return
	 */
	public abstract HighlightingFilterGui createGui(XLog log);

	/**
	 * Returns whether this filter is actually filtering something. If this
	 * method returns false, no other filtering function will be called and the
	 * entire log will be used.
	 * 
	 * @return
	 */
	protected abstract boolean isEnabled();

	/**
	 * Main function of the filter. Returns whether the given IvMTrace trace
	 * should be counted towards the result.
	 * 
	 * @param trace
	 * @return
	 */
	public abstract boolean countInColouring(IvMTrace trace);

	/**
	 * This function is called when the user updates a filter and the filtering
	 * has to be recomputed.
	 */
	protected void update() {
		if (onUpdate != null) {
			onUpdate.run();
		}
	}

	//private methods
	private HighlightingFilterGui panel = null;
	private Runnable onUpdate = null;
	private boolean enabledFilter = false;

	public void initialiseFilter(XLog log, Runnable onUpdate) {
		try {
			//this might be foreign code, so be careful
			panel = createGui(log);
		} catch (Exception e) {
			panel = null;
		}
		this.onUpdate = onUpdate;
	}

	public boolean swapEnabledFilter() {
		enabledFilter = !enabledFilter;
		return enabledFilter;
	}

	public boolean isEnabledFilter() {
		return enabledFilter && (panel != null) && isEnabled();
	}

	public HighlightingFilterGui getPanel() {
		return panel;
	}
}
