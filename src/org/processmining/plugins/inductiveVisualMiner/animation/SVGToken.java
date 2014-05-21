package org.processmining.plugins.inductiveVisualMiner.animation;

public class SVGToken {
	private double endTime;
	private StringBuilder result;

	public SVGToken(boolean fade) {
		result = new StringBuilder();
		result.append("<ellipse fill='yellow' stroke='black' cx='0' cy='0' rx='4' ry='4' ");
		//we use fading at all times due to a flaw in svgsalamander
		//		if (fade) {
		result.append("opacity='0'>");
		//		} else {
		//			result.append("visibility='hidden'>");
		//		}
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
