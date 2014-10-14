package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
public class TimedTrace extends IMTraceG<TimedMove> {

	private static final long serialVersionUID = 9214941352493005077L;
	
	private Double startTime = null;
	private Double endTime = null;
	private String id;
	
	public TimedTrace(String id) {
		this.id = id;
	}
	
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	public Double getEndTime() {
		return endTime;
	}

	public Double getStartTime() {
		return startTime;
	}
	
	public String toString() {
		return "[@" + getStartTime() + " " + super.toString() + " " + " @" + getEndTime() + "]";
	}
	
	public String getId() {
		return id;
	}
	
	public TimedTrace clone() {
		TimedTrace copy = new TimedTrace(id);
		copy.addAll(this);
		copy.setStartTime(startTime);
		copy.setEndTime(endTime);
		return copy;
	}
}
