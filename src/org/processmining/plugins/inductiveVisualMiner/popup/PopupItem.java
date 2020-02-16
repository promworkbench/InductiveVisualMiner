package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public interface PopupItem<T> {

	/**
	 * Returns zero or more popup items. This will be called throughout the
	 * chain, so make sure that you test whether the data you need is available
	 * in the state.
	 * 
	 * @param state
	 * @param input
	 * @return String[items][columns], where there can be 1 or 2 columns.
	 */
	public String[][] get(InductiveVisualMinerState state, PopupItemInput<T> input);

	public static String[][] nothing = new String[0][0];

}
