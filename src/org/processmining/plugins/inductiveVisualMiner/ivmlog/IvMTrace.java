package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.List;

import org.deckfour.xes.model.XAttributeMap;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

public interface IvMTrace extends List<IvMMove> {

	/**
	 * Name to be shown in the trace view.
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * 
	 * @return start time of the trace in user time. This includes fading-in of
	 *         the animated tokens.
	 */
	public Double getStartTime();

	/**
	 * 
	 * @return the first timestamp that is present in the trace in log time.
	 */
	public Long getRealStartTime();

	/**
	 * 
	 * @return the last timestamp that is present in the trace in log time.
	 */
	public Long getRealEndTime();

	/**
	 * 
	 * @return end time of the trace in user time. This includes fading-out of
	 *         the animated tokens.
	 */
	public Double getEndTime();

	public void setStartTime(double guessStartTime);

	public void setEndTime(double guessEndTime);

	public void setRealStartTime(long startTime);

	public void setRealEndTime(long endTime);

	public XAttributeMap getAttributes();

	public ActivityInstanceIterator activityInstanceIterator(IvMModel model);

	public int getNumberOfEvents();
}
