package org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps;

public class SizeMapLinear extends SizeMap {

	private final double minSize;
	private final double maxSize;	
	
	public SizeMapLinear(double minSize, double maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
	
	public double size(long cardinality, long maxCardinality) {
		if (maxCardinality == 0) {
			return (minSize + maxSize) / 2;
		}
		return (cardinality / (maxCardinality * 1.0)) * (maxSize - minSize) + minSize;
	}

}
