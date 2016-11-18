package org.processmining.plugins.inductiveVisualMiner.traceview;

import org.processmining.framework.util.ui.widgets.traceview.ProMTraceList.TraceBuilder;
import org.processmining.plugins.inductiveVisualMiner.animation.tracecolouring.TraceColourMap;

public abstract class TraceBuilderWrapper implements TraceBuilder<Object> {

	protected TraceColourMap traceColourMap;

	protected TraceBuilderWrapper(TraceColourMap traceColourMap) {
		this.traceColourMap = traceColourMap;
	}
	
	public TraceColourMap getTraceColourMap() {
		return traceColourMap;
	}

	public void setTraceColourMap(TraceColourMap traceColourMap) {
		this.traceColourMap = traceColourMap;
	}
}
