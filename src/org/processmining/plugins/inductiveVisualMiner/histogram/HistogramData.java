package org.processmining.plugins.inductiveVisualMiner.histogram;

import org.processmining.plugins.inductiveVisualMiner.animation.Scaler;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IteratorWithPosition;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;

public class HistogramData {
	private final int[] countFiltered;
	private final int[] countUnfiltered;
	private double max;
	private final Scaler scaler;
	private final int buckets;

	public HistogramData(IvMLogFiltered log, Scaler scaler, int buckets) {
		countFiltered = new int[buckets];
		countUnfiltered = new int[buckets];
		this.scaler = scaler;
		this.buckets = buckets;

		for (IteratorWithPosition<IvMTrace> it = log.iteratorUnfiltered(); it.hasNext();) {
			IvMTrace trace = it.next();
			boolean survivedFiltering = log.isFilteredOut(it.getPosition());

			addTrace(trace, survivedFiltering);
		}
	}

	private void addTrace(IvMTrace trace, boolean isFilteredOut) {

		Long realStartTime = trace.getRealStartTime();
		Long realEndTime = trace.getRealEndTime();

		if (realStartTime != null) {
			int startBucket = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(realStartTime)) * buckets);
			int endCount = (int) (scaler.userTime2Fraction(scaler.logTime2UserTime(realEndTime)) * buckets);

			for (int i = startBucket; i < endCount; i++) {
				countUnfiltered[i]++;
				max = Math.max(max, countUnfiltered[i]);

				if (!isFilteredOut) {
					countFiltered[i]++;
				}
			}
		}
	}

	public int getNrOfBuckets() {
		return buckets;
	}

	public double getBucketFraction(int bucketNr) {
		return countFiltered[bucketNr] / max;
	}
}
