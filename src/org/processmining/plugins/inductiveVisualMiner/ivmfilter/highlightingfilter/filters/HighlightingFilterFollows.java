package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributeFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HighlightingFilterFollows extends HighlightingFilter {

	private HighlightingFilterFollowsPanel panel;

	public boolean countInColouring(IvMTrace trace) {
		return isTraceIncluded(trace, panel);
	}

	public String getName() throws Exception {
		return "Event-followed-by filter";
	}

	public IvMFilterGui createGui(AttributesInfo attributesInfo) throws Exception {
		panel = new HighlightingFilterFollowsPanel(getName(), attributesInfo, new Runnable() {
			public void run() {
				updateExplanation();
				update();
			}
		});
		updateExplanation();
		return panel;
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getPanelBefore().getExplanationLabel().setText(
					"Include only traces that have a completion event followed by another completion event as selected.");
		} else {
			panel.getPanelBefore().getExplanationLabel()
					.setText("Include only traces that have a completion event "
							+ panel.getPanelBefore().getExplanation() + ", followed by an event "
							+ panel.getPanelFollow().getExplanation() + ", with (" + panel.getMinimumEventsInBetween()
							+ ", " + panel.getMaximumEventsInBetween() + ") completion events in between.");
		}

	}

	protected boolean isEnabled() {
		return panel.getPanelBefore().isFiltering() && panel.getPanelFollow().isFiltering();
	}

	protected boolean fillGuiWithLog(IMLog log, XLog xLog, IvMLog ivmLog) throws Exception {
		return false;
	}

	public static boolean isTraceIncluded(IvMTrace trace, HighlightingFilterFollowsPanel panel) {
		int min = panel.getMinimumEventsInBetween();
		int max = panel.getMaximumEventsInBetween();

		boolean[] eventSelectedFollow = new boolean[trace.size()];
		boolean[] eventCheckedFollow = new boolean[trace.size()];

		//first, look for the first selected event
		for (int eventIndexBefore = 0; eventIndexBefore < trace.size() - 1; eventIndexBefore++) {
			if (isEventSelected(trace, eventIndexBefore, panel.getPanelBefore())) {

				int eventIndexFollow = eventIndexBefore;

				//first, walk to the first completion event that we could consider, given min
				for (int completions = 0; completions <= min; completions++) {
					eventIndexFollow = nextComplete(trace, eventIndexFollow);
				}

				//then, walk up to the max while checking for candidates
				for (int completions = min; completions <= max; completions++) {
					if (eventIndexFollow >= 0) {
						//make sure that this event has been considered
						if (!eventCheckedFollow[eventIndexFollow]) {
							eventSelectedFollow[eventIndexFollow] = isEventSelected(trace, eventIndexFollow,
									panel.getPanelFollow());
							eventCheckedFollow[eventIndexFollow] = true;
						}

						if (eventSelectedFollow[eventIndexFollow]) {
							return true;
						}
					}
					eventIndexFollow = nextComplete(trace, eventIndexFollow);
				}
			}
		}
		return false;
	}

	/**
	 * Look for the next complete event after index (may be -1), or -2 if that
	 * does not exist.
	 * 
	 * @param trace
	 * @param index
	 *            (may be -2, then -2 is returned)
	 * @param walk
	 * @return
	 */
	public static int nextComplete(IvMTrace trace, int index) {
		if (index == -2) {
			return -2;
		}
		index++;
		while (index < trace.size()) {
			if (trace.get(index).isComplete() && trace.get(index).getAttributes() != null) {
				return index;
			}
			index++;
		}
		return -2;
	}

	public static boolean isEventSelected(IvMTrace trace, int eventIndex, AttributeFilterGui panel) {
		Attribute attribute = panel.getSelectedAttribute();
		IvMMove event = trace.get(eventIndex);
		if (event.isComplete()) {
			if (attribute.isLiteral()) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName()) && panel
						.getSelectedLiterals().contains(event.getAttributes().get(attribute.getName()).toString())) {
					return true;
				}
			} else if (attribute.isNumeric()) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					double value = Attribute.parseDoubleFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedNumericMin() && value <= panel.getSelectedNumericMax()) {
						return true;
					}
				}
			} else if (attribute.isTime()) {
				if (event.getAttributes() != null && event.getAttributes().containsKey(attribute.getName())) {
					long value = Attribute.parseTimeFast(event.getAttributes().get(attribute.getName()));
					if (value >= panel.getSelectedTimeMin() && value <= panel.getSelectedTimeMax()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}
