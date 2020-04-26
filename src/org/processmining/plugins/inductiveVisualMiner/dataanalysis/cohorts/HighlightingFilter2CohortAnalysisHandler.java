package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

/**
 * Communication from highlighting filters to data analysis
 * 
 * @author sander
 *
 */
public interface HighlightingFilter2CohortAnalysisHandler {
	public void showCohortAnalysis();

	public void setEnabled(boolean enabled);
}