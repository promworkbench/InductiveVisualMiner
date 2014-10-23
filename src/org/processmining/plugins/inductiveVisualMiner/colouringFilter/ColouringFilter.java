package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

@ColouringFilterAnnotation
public abstract class ColouringFilter {

	/**
	 * Constructor. User is waiting when this function is called.
	 */
	public ColouringFilter() {

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
	public abstract ColouringFilterGui createGui(XLog log);

	/**
	 * Returns whether this filter is actually filtering something. If this
	 * method returns false, no other filtering function will be called and the
	 * entire log will be used.
	 * 
	 * @return
	 */
	protected abstract boolean isEnabled();

	/**
	 * Main function of the filter. Returns whether the given xTrace/aligned
	 * trace should be counted towards the result.
	 * 
	 * @param xTrace
	 * @param aTrace
	 * @return
	 */
	public abstract boolean countInColouring(XTrace xTrace, AlignedTrace aTrace);

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
	private ColouringFilterGui panel = null;
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

	public ColouringFilterGui getPanel() {
		return panel;
	}
}
