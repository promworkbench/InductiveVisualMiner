package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignedTrace;

@IvMColouringFilterPlugin
public abstract class ColouringFilter {

	protected final JFrame jFrame;
	protected final XLog xLog;
	
	private Runnable onUpdate = null;

	public ColouringFilter(JComponent parent, XLog xLog) {
		this.xLog = xLog;
		jFrame = createGui(parent);
		jFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	/**
	 * Returns whether this filter is actually filtering something. If this
	 * method returns false, no other filtering function will be called and the
	 * entire log will be used.
	 * 
	 * @return
	 */
	public abstract boolean isEnabled();

	/**
	 * Initialises the JFrame containing the filter settings.
	 * 
	 * @param parent
	 * @param update
	 * @return
	 */
	public abstract JFrame createGui(JComponent parent);

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
	 * Hides the filter settings.
	 */
	public void hide() {
		if (jFrame != null) {
			jFrame.setVisible(false);
		}
	}

	/**
	 * Shows the filter settings.
	 */
	public void show() {
		if (jFrame != null) {
			jFrame.setVisible(true);
		}
	}

	/**
	 * This function is called when the user updates a filter and the filtering
	 * has to be recomputed.
	 */
	protected void update() {
		if (onUpdate != null) {
			onUpdate.run();
		}
	}

	public Runnable getOnUpdate() {
		return onUpdate;
	}

	public void setOnUpdate(Runnable onUpdate) {
		this.onUpdate = onUpdate;
	}
}
