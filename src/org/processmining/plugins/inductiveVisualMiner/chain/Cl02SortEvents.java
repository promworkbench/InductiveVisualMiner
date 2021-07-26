package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Collections;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.plugins.InductiveMiner.Function;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.ResourceTimeUtils;
import org.processmining.plugins.inductiveVisualMiner.plugins.SortEventsPlugin.EventsComparator;

public class Cl02SortEvents extends DataChainLinkComputationAbstract<Object> {

	private Function<Object, Boolean> onIllogicalTimeStamps;

	@Override
	public String getName() {
		return "sort events";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Performing log analysis..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.input_log };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.sorted_log, IvMObject.log_timestamps_logical };
	}

	@Override
	public IvMObjectValues execute(Object configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		XLog log = inputs.get(IvMObject.input_log);

		IvMObjectValues result = new IvMObjectValues();

		if (hasIllogicalTimeStamps(log, canceller)) {

			//ask the user whether to fix it
			if (onIllogicalTimeStamps.call(null)) {

				System.out.println("sort events in log");

				XLog sortedLog = new XLogImpl(log.getAttributes());
				for (XTrace trace : log) {

					if (canceller.isCancelled()) {
						return null;
					}

					XTrace resultTrace = new XTraceImpl(trace.getAttributes());
					resultTrace.addAll(trace);
					Collections.sort(resultTrace, new EventsComparator());
					sortedLog.add(resultTrace);
				}
				result.set(IvMObject.sorted_log, sortedLog);
				result.set(IvMObject.log_timestamps_logical, true);
			} else {
				//not-logical timestamps, but don't fix them
				result.set(IvMObject.sorted_log, log);
				result.set(IvMObject.log_timestamps_logical, false);
			}
		} else {
			//all good
			result.set(IvMObject.sorted_log, log);
			result.set(IvMObject.log_timestamps_logical, true);
		}

		return result;
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
}
