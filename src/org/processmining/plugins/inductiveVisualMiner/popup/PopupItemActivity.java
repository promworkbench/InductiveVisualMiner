package org.processmining.plugins.inductiveVisualMiner.popup;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

public interface PopupItemActivity {

	/**
	 * True = two columns; False = single column.
	 * 
	 * @return
	 */
	public boolean isTwoColumns();

	/**
	 * Only called when isTwoColumns() returns false;
	 * 
	 * @param state
	 * @param unode
	 * @return the entire item, spanning two columns.
	 */
	public String[] getSingleColumn(InductiveVisualMinerState state, int unode);

	/**
	 * Only called when isTwoColumns() returns true;
	 * 
	 * You are in the user-thread. And the pop-up should show up immediately. Do
	 * not perform any computations in this method. Rather, add a ChainLink in
	 * which you perform the computation and store it in the state as an object.
	 * Be aware that this method might be called before that ChainLink was
	 * executed, thus verify that your object is ready. This will be called
	 * twice for every popup refresh.
	 * 
	 * @param state
	 * @param unode
	 * @return the first column (typically the name of a measure).
	 */
	public String[] getColumnA(InductiveVisualMinerState state, int unode);

	/**
	 * Only called when isTwoColumns() returns true;
	 * 
	 * You are in the user-thread. And the pop-up should show up immediately. Do
	 * not perform any computations in this method. Rather, add a ChainLink in
	 * which you perform the computation and store it in the state as an object.
	 * Be aware that this method might be called before that ChainLink was
	 * executed, thus verify that your object is ready.
	 * 
	 * @param state
	 * @param unode
	 * @return the second column (typically the value of the measure).
	 */
	public String[] getColumnB(InductiveVisualMinerState state, int unode);

}