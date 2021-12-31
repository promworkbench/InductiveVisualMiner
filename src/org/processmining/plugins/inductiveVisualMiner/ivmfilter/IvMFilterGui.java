package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;

public abstract class IvMFilterGui extends JPanel {

	private static final long serialVersionUID = -7693755022689210425L;

	public static final Border explanationBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);

	protected boolean usesVerticalSpace = false;

	private final IvMDecoratorI decorator;

	public IvMFilterGui(String title, IvMDecoratorI decorator) {
		this.decorator = decorator;
		if (title != null) {
			Border innerBorder = BorderFactory.createLineBorder(decorator.backGroundColour2(), 2);
			TitledBorder border = BorderFactory.createTitledBorder(innerBorder,
					title.substring(0, 1).toUpperCase() + title.substring(1) + " filter");
			border.setTitleFont(decorator.fontLarger());
			border.setTitleColor(decorator.textColour());
			setBorder(border);
		} else {
			Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10);
			setBorder(border);
		}
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
	}

	public boolean isUsesVerticalSpace() {
		return usesVerticalSpace;
	}

	public JTextArea createExplanation(String explanation) {
		JTextArea textArea = new JTextArea(explanation);
		decorator.decorate(textArea);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setHighlighter(null);
		textArea.setBorder(explanationBorder);
		return textArea;
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
