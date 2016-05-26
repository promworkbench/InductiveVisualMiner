package org.processmining.plugins.inductiveVisualMiner.animation;

public interface AnimationEnabledChangedListener {

	/**
	 * Method is called when the user presses the shortcut combination to enable
	 * or disable the animation.
	 * 
	 * @return This method should return whether the animation is enabled after
	 *         the change has occurred. The panel will then update de
	 *         description in the popup.
	 */
	public boolean animationEnabledChanged();
}
