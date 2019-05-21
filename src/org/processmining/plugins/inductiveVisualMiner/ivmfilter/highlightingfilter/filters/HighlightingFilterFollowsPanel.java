package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.Color;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.RangeSlider;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public class HighlightingFilterFollowsPanel extends IvMFilterGui {
	private static final long serialVersionUID = 4742499018638960929L;
	private final AttributeFilterGui panelBefore;
	private final RangeSlider valueNumericSelector;
	private final AttributeFilterGui panelFollow;

	public HighlightingFilterFollowsPanel(String title, AttributesInfo attributesInfo, final Runnable onUpdate) {
		super(title);
		panelBefore = new AttributeFilterGui(getName(), attributesInfo.getEventAttributes(), onUpdate);
		add(panelBefore);

		int maxTraceLength = (int) attributesInfo.getTraceAttributeValues("number of events").getTimeMax();
		valueNumericSelector = new RangeSlider(0, maxTraceLength);
		valueNumericSelector.setValue(0);
		valueNumericSelector.setUpperValue(maxTraceLength);
		add(valueNumericSelector);
		valueNumericSelector.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				onUpdate.run();
			}
		});

		panelFollow = new AttributeFilterGui(getName(), attributesInfo.getEventAttributes(), onUpdate);
		add(panelFollow);
	}

	public AttributeFilterGui getPanelBefore() {
		return panelBefore;
	}

	public int getMinimumEventsInBetween() {
		return valueNumericSelector.getValue();
	}

	public int getMaximumEventsInBetween() {
		return valueNumericSelector.getUpperValue();
	}

	public AttributeFilterGui getPanelFollow() {
		return panelFollow;
	}

	@Override
	protected void setForegroundRecursively(Color colour) {
		if (panelBefore != null && panelFollow != null) {
			panelBefore.setForegroundRecursively(colour);
			panelFollow.setForegroundRecursively(colour);
		}
	}
}