package org.processmining.plugins.inductiveVisualMiner.ivmlog;

public interface IvMLogNotFiltered extends IvMLog {
	
	public IvMTrace get(int traceIndex);
	
	public int size();
}
