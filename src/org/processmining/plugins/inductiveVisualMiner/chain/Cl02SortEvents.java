package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Collections;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes.LogAttributeAnalysis;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.plugins.SortEventsPlugin.EventsComparator;

public class Cl02SortEvents extends ChainLink<XLog, Pair<XLog, LogAttributeAnalysis>> {

	private Function<Object, Boolean> onIllogicalTimeStamps;

	protected XLog generateInput(InductiveVisualMinerState state) {
		return state.getXLog();
	}

	protected Pair<XLog, LogAttributeAnalysis> executeLink(XLog input, IvMCanceller canceller) throws Exception {
		if (hasIllogicalTimeStamps(input, canceller)) {

			//ask the user whether to fix it
			if (onIllogicalTimeStamps.call(null)) {

				System.out.println("fix log");

				XLog result = new XLogImpl(input.getAttributes());
				for (XTrace trace : input) {

					if (canceller.isCancelled()) {
						return null;
					}

					XTrace resultTrace = new XTraceImpl(trace.getAttributes());
					resultTrace.addAll(trace);
					Collections.sort(resultTrace, new EventsComparator());
					result.add(resultTrace);
				}
				return Pair.of(result, new LogAttributeAnalysis(result, canceller));
			} else {
				return null;
			}
		}
		return Pair.of(input, new LogAttributeAnalysis(input, canceller));
	}

	protected void processResult(Pair<XLog, LogAttributeAnalysis> result, InductiveVisualMinerState state) {
		if (result != null) {
			state.setSortedXLog(result.getA());
			state.setIllogicalTimeStamps(false);
			state.setLogAttributesAnalysis(result.getB());
		} else {
			state.setSortedXLog(state.getXLog());
			state.setIllogicalTimeStamps(true);
		}
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setSortedXLog(null);
	}

	public Function<Object, Boolean> getOnIllogicalTimeStamps() {
		return onIllogicalTimeStamps;
	}

	/**
	 * 
	 * @param onIllogicalTimeStamps
	 *            This function will be called when illogical time stamps are
	 *            detected. If the function returns true, the ordering of the
	 *            events will be fixed.
	 */
	public void setOnIllogicalTimeStamps(Function<Object, Boolean> onIllogicalTimeStamps) {
		this.onIllogicalTimeStamps = onIllogicalTimeStamps;
	}

	public boolean hasIllogicalTimeStamps(XLog log, IvMCanceller canceller) {
		long lastTimeStamp;
		Long timeStamp;
		for (XTrace trace : log) {
			lastTimeStamp = Long.MIN_VALUE;
			for (XEvent event : trace) {
				timeStamp = ResourceTimeUtils.getTimestamp(event);
				if (timeStamp != null) {
					if (timeStamp < lastTimeStamp) {
						return true;
					}
					lastTimeStamp = timeStamp;
				}
			}

			if (canceller.isCancelled()) {
				return false;
			}
		}
		return false;
	}

	public String getName() {
		return "sort events & log analysis";
	}

	public String getStatusBusyMessage() {
		return "Performing log analysis..";
	}
}
