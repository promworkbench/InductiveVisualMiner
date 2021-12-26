package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.JTextArea;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public class HighlightingFilterCohortPanel extends IvMFilterGui {

	private static final long serialVersionUID = 6944629750562248418L;

	private final JTextArea explanation;

	public HighlightingFilterCohortPanel(String title, IvMDecoratorI decorator) {
		super(title, decorator);
		usesVerticalSpace = false;

		//explanation
		{
			explanation = new JTextArea();
			decorator.decorate(explanation);
			explanation.setEditable(false);
			explanation.setLineWrap(true);
			explanation.setWrapStyleWord(true);
			explanation.setOpaque(false);
			explanation.setHighlighter(null);
			add(explanation);
		}

		add(Box.createVerticalStrut(10));
	}

	public JTextArea getExplanationLabel() {
		return explanation;
	}

	protected void setForegroundRecursively(Color colour) {
		if (explanation != null) {
			explanation.setForeground(colour);
		}
	}

}
