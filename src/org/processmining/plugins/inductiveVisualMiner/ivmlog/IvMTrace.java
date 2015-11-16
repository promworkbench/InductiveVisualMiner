package org.processmining.plugins.inductiveVisualMiner.ivmlog;

import java.util.List;

import org.deckfour.xes.model.XAttributeMap;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTraceImpl.ActivityInstanceIterator;

public interface IvMTrace extends List<IvMMove> {

	/**
	 * Name to be shown in the trace view.
	 * 
	 * @return
	 */
	public String getName();

	public Double getStartTime();

	public Double getEndTime();

	public void setStartTime(double guessStartTime);

	public void setEndTime(double guessEndTime);

	public XAttributeMap getAttributes();

	public ActivityInstanceIterator activityInstanceIterator();
}
