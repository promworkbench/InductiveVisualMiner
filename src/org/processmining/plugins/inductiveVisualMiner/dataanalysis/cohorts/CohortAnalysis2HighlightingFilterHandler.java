package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import org.processmining.cohortanalysis.cohort.Cohort;

/**
 * Communication from data analysis to highlighting filters
 * 
 * @author sander
 *
 */
public interface CohortAnalysis2HighlightingFilterHandler {

	public void setSelectedCohort(Cohort cohort, boolean filterInCohort);

}
