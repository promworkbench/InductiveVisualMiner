package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.Color;

import javax.swing.Box;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.RangeSlider;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class HighlightingFilterFollowsPanel extends IvMFilterGui {
	private static final long serialVersionUID = 4742499018638960929L;
	private final AttributeFilterGui panelBefore;
	private final JTextArea inBetweenSelectorExplanation;
	private final RangeSlider inBetweenSelector;
	private final AttributeFilterGui panelFollow;

	public HighlightingFilterFollowsPanel(String title, AttributesInfo attributesInfo, final Runnable onUpdate) {
		super(title);
		panelBefore = new AttributeFilterGui(null, attributesInfo.getEventAttributes(), onUpdate);
		add(panelBefore);
		
		add(Box.createVerticalStrut(10));

		{
			inBetweenSelectorExplanation = new JTextArea("Events in between");
			IvMDecorator.decorate(inBetweenSelectorExplanation);
			inBetweenSelectorExplanation.setEditable(false);
			inBetweenSelectorExplanation.setLineWrap(true);
			inBetweenSelectorExplanation.setWrapStyleWord(true);
			inBetweenSelectorExplanation.setOpaque(false);
			inBetweenSelectorExplanation.setHighlighter(null);
			add(inBetweenSelectorExplanation);
		}

		add(Box.createVerticalStrut(5));

		int maxTraceLength = (int) attributesInfo.getTraceAttributeValues("number of events").getTimeMax();
		inBetweenSelector = new RangeSlider(0, maxTraceLength);
		inBetweenSelector.setValue(0);
		inBetweenSelector.setUpperValue(maxTraceLength);
		add(inBetweenSelector);
		inBetweenSelector.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onUpdate.run();
			}
		});
		
		add(Box.createVerticalStrut(10));

		panelFollow = new AttributeFilterGui(null, attributesInfo.getEventAttributes(), onUpdate);
		panelFollow.getExplanationLabel().setText("Followed by");
		add(panelFollow);
	}

	public AttributeFilterGui getPanelBefore() {
		return panelBefore;
	}

	public int getMinimumEventsInBetween() {
		return inBetweenSelector.getValue();
	}

	public int getMaximumEventsInBetween() {
		return inBetweenSelector.getUpperValue();
	}

	public AttributeFilterGui getPanelFollow() {
		return panelFollow;
	}

	@Override
	protected void setForegroundRecursively(Color colour) {
		if (panelBefore != null && panelFollow != null) {
			panelBefore.setForegroundRecursively(colour);
			panelFollow.setForegroundRecursively(colour);
			inBetweenSelectorExplanation.setForeground(colour);
		}
	}
}