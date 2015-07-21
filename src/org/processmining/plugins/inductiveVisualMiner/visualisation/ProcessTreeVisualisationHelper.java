package org.processmining.plugins.inductiveVisualMiner.visualisation;

public class ProcessTreeVisualisationHelper {
	
	public static double getPenWidth(long cardinality, long minCardinality, long maxCardinality, boolean widen) {
		if (widen) {
			return getOccurrenceFactor(cardinality, minCardinality, maxCardinality) * 100 + 1;
		} else {
			return 1;
		}
	}
	
	public static double getOccurrenceFactor(long cardinality, long minCardinality, long maxCardinality) {
		if (minCardinality == maxCardinality) {
			return 1;
		}
		if (cardinality != -1 && minCardinality != -1 && maxCardinality != -1) {
			return (cardinality - minCardinality) / ((maxCardinality - minCardinality) * 1.0);
		}

		return 0;
	}
}
