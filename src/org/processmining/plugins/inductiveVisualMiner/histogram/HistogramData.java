package org.processmining.plugins.inductiveVisualMiner.histogram;

import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HistogramData {
	private final int[] count;
	private double max;
	private final Scaler scaler;
	private final int buckets;

	public HistogramData(Scaler scaler, int buckets) {
		count = new int[buckets];
		this.scaler = scaler;
		this.buckets = buckets;
	}

	public void incorporate(IvMTrace trace) {
		Double t = trace.getStartTime();
		double x = scaler.userTime2Fraction(t) * buckets;
		int startCount = (int) Math.floor(x);
		int endCount = (int) (scaler.userTime2Fraction(trace.getEndTime()) * buckets);

		for (int i = startCount; i < endCount; i++) {
			count[i]++;
			max = Math.max(max, count[i]);
		}
	}

	public int getNrOfBuckets() {
		return buckets;
	}

	public double getBucketFraction(int bucketNr) {
		return count[bucketNr] / max;
	}
}
