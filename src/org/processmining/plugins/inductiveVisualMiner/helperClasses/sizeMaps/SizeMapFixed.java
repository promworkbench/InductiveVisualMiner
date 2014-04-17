package org.processmining.plugins.inductiveVisualMiner.helperClasses.sizeMaps;

public class SizeMapFixed extends SizeMap {

	private final double size;
	
	public SizeMapFixed(double size) {
		this.size = size;
	}
	
	public double size(long cardinality, long maxCardinality) {
		return size;
	}

}
