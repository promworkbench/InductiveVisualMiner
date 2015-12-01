package org.processmining.plugins.inductiveVisualMiner.histogram;

import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class ComputeHistogram {
	public static HistogramData computeHistogram(IvMLog log, Scaler scaler, int widthInPixels) {
		HistogramData result = new HistogramData(scaler, widthInPixels);
		for (IvMTrace trace : log) {
			result.incorporate(trace);
		}
		return result;
	}
}
