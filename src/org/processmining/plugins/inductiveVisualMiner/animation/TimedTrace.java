package org.processmining.plugins.inductiveVisualMiner.animation;

import org.processmining.plugins.InductiveMiner.mining.IMTraceG;
public class TimedTrace extends IMTraceG<TimedMove> {

	private static final long serialVersionUID = 9214941352493005077L;
	
	private double startTime = 0;
	private double endTime = 0;
	
	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}
	
	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}
	
	public double getEndTime() {
		return endTime;
	}

	public double getStartTime() {
		return startTime;
	}
}
