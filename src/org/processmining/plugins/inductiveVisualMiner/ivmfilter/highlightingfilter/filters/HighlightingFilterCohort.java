package org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.filters;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.feature.Feature;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts.HighlightingFilter2CohortAnalysisHandler;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.highlightingfilter.HighlightingFilter;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class HighlightingFilterCohort extends HighlightingFilter {

	HighlightingFilterCohortPanel panel = null;
	private HighlightingFilter2CohortAnalysisHandler showCohortAnalysisHandler;
	private Cohort selectedCohort = null;
	private boolean highlightInCohort = true;

	public boolean countInColouring(IvMTrace trace) {
		if (selectedCohort == null) {
			return true;
		}
		return !(inCohort(trace, selectedCohort) ^ highlightInCohort);
	}

	public static boolean inCohort(IvMTrace trace, Cohort cohort) {
		/*
		 * Create a temporary xtrace. The CohortAnalysis package doesn't know
		 * about IvMTraces or events (and conceptually, is unaware of alignment
		 * results), thus we have to take this step. The GUI is not waiting on
		 * this step, so that's fine.
		 */
		XTrace xTrace = new XTraceImpl(trace.getAttributes());
		for (IvMMove event : trace) {
			if (event.getAttributes() != null) {
				xTrace.add(new XEventImpl(event.getAttributes()));
			}
		}

		//a trace is in a cohort when all of the cohort's features are present in the trace.
		for (Feature feature : cohort.getFeatures()) {
			if (!feature.includes(xTrace)) {
				return false;
			}
		}
		return true;
	}

	public String getName() throws Exception {
		return "Cohort filter";
	}

	public IvMFilterGui createGui(AttributesInfo attributesInfo) throws Exception {
		panel = new HighlightingFilterCohortPanel(getName());
		updateExplanation();

		panel.getExplanationLabel().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (showCohortAnalysisHandler != null) {
					showCohortAnalysisHandler.showCohortAnalysis();
				}
			}
		});

		return panel;
	}

	@Override
	public void setEnabledFilter(boolean enabled) {
		super.setEnabledFilter(enabled);
		if (showCohortAnalysisHandler != null) {
			showCohortAnalysisHandler.setEnabled(enabled);
		}
	}

	protected boolean isEnabled() {
		return selectedCohort != null;
	}

	protected boolean fillGuiWithLog(IMLog log, XLog xLog, IvMLog ivmLog) throws Exception {
		return false;
	}

	public void updateExplanation() {
		if (!isEnabled()) {
			panel.getExplanationLabel()
					.setText("Include only traces that are in the cohort selected in data analysis.\n"
							+ "Click here to select a cohort in the data analysis window.");
		} else {
			StringBuilder s = new StringBuilder();
			s.append("Include only traces having ");
			if (!highlightInCohort) {
				s.append("not ");
			}
			for (Iterator<Feature> it = selectedCohort.getFeatures().iterator(); it.hasNext();) {
				Feature feature = it.next();
				s.append("attribute `");
				s.append(feature.getDescriptionField());
				s.append("' being ");
				s.append(feature.getDescriptionSelector().replace("&gt;", "greater than").replace("&lt;", "less than"));

				if (it.hasNext()) {
					s.append(", and ");
				}
			}
			s.append(".\n");
			s.append("Click here to select a cohort in the data analysis window.");

			panel.getExplanationLabel().setText(s.toString());
		}
	}

	public void setShowCohortAnalysisHandler(HighlightingFilter2CohortAnalysisHandler showCohortAnalysisHandler) {
		this.showCohortAnalysisHandler = showCohortAnalysisHandler;
	}

	/**
	 * Set the selected cohort, or null to not select any cohort.
	 * 
	 * @param cohort
	 * @param highlightInCohort
	 */
	public void setSelectedCohort(Cohort cohort, boolean highlightInCohort) {
		this.selectedCohort = cohort;
		this.highlightInCohort = highlightInCohort;
		updateExplanation();
		update();
	}

}