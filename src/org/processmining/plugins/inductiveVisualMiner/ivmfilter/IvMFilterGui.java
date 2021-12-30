package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public abstract class IvMFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	protected boolean usesVerticalSpace = false;

	public IvMFilterGui(String title, IvMDecoratorI decorator) {
		if (title != null) {
			Border innerBorder = BorderFactory.createLineBorder(decorator.backGroundColour2(), 2);
			TitledBorder border = BorderFactory.createTitledBorder(innerBorder, title);
			border.setTitleFont(decorator.fontLarger());
			border.setTitleColor(decorator.textColour());
			setBorder(border);
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
	}

	public boolean isUsesVerticalSpace() {
		return usesVerticalSpace;
	}

	/**
	 * We're drastically changing colours. Make sure the gui is not-opaque and
	 * recursively set the text colour.
	 */
	@Deprecated
	protected abstract void setForegroundRecursively(Color colour);

	@Override
	public void setForeground(Color colour) {
		super.setForeground(colour);
		setForegroundRecursively(colour);
		if (getBorder() != null) {
			((TitledBorder) getBorder()).setTitleColor(colour);
		}
	}
}
