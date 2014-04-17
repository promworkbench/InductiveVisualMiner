package org.processmining.plugins.inductiveVisualMiner.alignment;


public class AlignmentResult {
	public final AlignedLog log;
	public final AlignedLogInfo logInfo;

	public AlignmentResult(AlignedLog log, AlignedLogInfo logInfo) {
		this.log = log;
		this.logInfo = logInfo;
	}
}
