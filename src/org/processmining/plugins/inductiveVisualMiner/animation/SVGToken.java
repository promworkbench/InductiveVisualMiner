package org.processmining.plugins.inductiveVisualMiner.animation;

public class SVGToken {
	private double endTime;
	private StringBuilder result;
	
	public SVGToken() {
		result = new StringBuilder();
		result.append("<ellipse fill='yellow' stroke='black' cx='0' cy='0' rx='4' ry='4' opacity='0'>");
	}
	
	public void addTrace(String trace, double endTime) {
		this.endTime = endTime;
		result.append(trace);
	}
	
	public double getEndTime() {
		return endTime;
	}
	
	public String toString() {
		return result.toString() + "</ellipse>\n";
	}
}
