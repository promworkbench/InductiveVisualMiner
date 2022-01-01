package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.RangeSlider;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public class FilterIvMTraceFollowsPanel extends IvMFilterGui {
	private static final long serialVersionUID = 4742499018638960929L;
	private final JLabel inBetweenSelectorExplanation;
	private final RangeSlider inBetweenSelector;

	public FilterIvMTraceFollowsPanel(String title, final Runnable onUpdate, IvMDecoratorI decorator) {
		super(title, decorator);

		setLayout(new BorderLayout());

		//header
		{
			JPanel header = new JPanel();
			header.setOpaque(false);
			header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
			add(header, BorderLayout.PAGE_START);

			JTextArea explanation = createExplanation(
					"Include only traces that have a completion event followed by other completion events as per the sub-filters.");
			explanation.setAlignmentX(0);
			header.add(explanation);

			header.add(Box.createVerticalStrut(10));

			{
				inBetweenSelectorExplanation = new JLabel("Events in between: ");
				inBetweenSelectorExplanation.setAlignmentX(0);
				decorator.decorate(inBetweenSelectorExplanation);
				inBetweenSelectorExplanation.setBorder(explanationBorder);
				inBetweenSelectorExplanation.setOpaque(false);
				header.add(inBetweenSelectorExplanation);
			}

			header.add(Box.createVerticalStrut(5));

			inBetweenSelector = new RangeSlider(0, 1);
			inBetweenSelector.setAlignmentX(0);
			header.add(inBetweenSelector);
			inBetweenSelector.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					setRangeExplanation();
					onUpdate.run();
				}
			});
		}
	}

	private void setRangeExplanation() {
		inBetweenSelectorExplanation.setText(
				"Events in between: (" + getMinimumEventsInBetween() + "-" + getMaximumEventsInBetween() + ")");
	}

	public int getMinimumEventsInBetween() {
		return inBetweenSelector.getValue();
	}

	public int getMaximumEventsInBetween() {
		return inBetweenSelector.getUpperValue();
	}

	public void setMaxTraceLength(int maxTraceLength) {
		inBetweenSelector.setValue(0);
		inBetweenSelector.setMaximum(maxTraceLength);
		inBetweenSelector.setUpperValue(maxTraceLength);
		setRangeExplanation();
	}
}