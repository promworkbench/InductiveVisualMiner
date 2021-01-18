package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;

public interface PopupItem<T> {

	/**
	 * Request inputs. The get method will only be called when these are
	 * available.
	 * 
	 * @return
	 */
	public IvMObject<?>[] inputObjects();

	/**
	 * Returns zero or more popup items. The requested input objects are
	 * guaranteed to be available.
	 * 
	 * @param state
	 * @param input
	 * @return String[items][columns], where there can be 1 or 2 columns.
	 */
	public String[][] get(IvMObjectValues inputs, PopupItemInput<T> input);

	public static String[][] nothing = new String[0][0];

}
