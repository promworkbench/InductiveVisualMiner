package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree;

public interface FilterCommunicator<TI, TO, FI, FO> {

	public abstract class toFilterChannel<TI, TO> {
		public abstract TO toFilter(TI input);
	}

	public abstract class toFilterController {
		public abstract void setAndSelectRootFilter(String name);
	}

	/**
	 * 
	 * @return a name to distinguish the communication channel. All filters will
	 *         be offered the channel, and may selectively choose to use the
	 *         communicator based on this value. Only the most recently accepted
	 *         channel will be used by each filter.
	 */
	public String getName();

	/**
	 * The filter will respond to this method.
	 * 
	 * @param input
	 * @return
	 */
	public TO toFilter(TI input);

	/**
	 * The filter will call this method.
	 * 
	 * @param input
	 * @return
	 */
	public FO fromFilter(FI input);

	/**
	 * Ask the controller to set the filter with the given name as root and to
	 * select it.
	 */
	public void setAndSelectRootFilter(String name);

	/**
	 * The filter will set this method to receive from the channel.
	 * 
	 * @param to
	 */
	public void setToFilter(toFilterChannel<TI, TO> to);

	/**
	 * The controller will set this method to receive from the channel.
	 * 
	 * @param name
	 */
	public void setSetAndSelectRootFilter(toFilterController to);
}