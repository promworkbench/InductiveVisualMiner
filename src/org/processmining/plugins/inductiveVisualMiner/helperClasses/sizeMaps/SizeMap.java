package org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps;

public abstract class SizeMap {
	public abstract double size(long cardinality, long maxCardinality);
	
	public double size(long weight, long min, long max) {
		return size(weight - min, max - min);
	}
}
