package org.processmining.plugins.inductiveVisualMiner.ivmfilter.tree.filters;

import java.awt.Color;
import java.util.Iterator;

import javax.swing.JTextArea;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.feature.Feature;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecoratorI;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

public class FilterIvMTraceCohortPanel extends IvMFilterGui {

	private static final long serialVersionUID = 6944629750562248418L;

	private final Runnable onUpdate;
	private final JTextArea explanation;

	private Cohort selectedCohort;
	private boolean highlightInCohort;
	public static final String baseExplanation = "Include only traces that are in the cohort selected in Data analysis.\n"
			+ "Click here to select a cohort in the Data analysis window.\n\n";

	public FilterIvMTraceCohortPanel(String title, Runnable onUpdate, IvMDecoratorI decorator) {
		super(title, decorator);
		this.onUpdate = onUpdate;
		usesVerticalSpace = false;

		explanation = createExplanation(baseExplanation + "No cohort selected.");
		add(explanation);
	}

	public JTextArea getExplanationLabel() {
		return explanation;
	}

	public void getExplanation(StringBuilder s) {
		if (selectedCohort != null) {
			s.append("having ");
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
		} else {
			s.append("any trace");
		}
	}

	@Override
	@Deprecated
	protected void setForegroundRecursively(Color colour) {
		if (explanation != null) {
			explanation.setForeground(colour);
		}
	}

	public void setSelectedCohort(Cohort cohort, boolean highlightInCohort) {
		this.selectedCohort = cohort;
		this.highlightInCohort = highlightInCohort;

		StringBuilder s = new StringBuilder();
		s.append(baseExplanation);
		if (selectedCohort != null) {
			s.append("Selected cohort: traces ");
			getExplanation(s);
			s.append(".");
		} else {
			s.append("No cohort selected.");
		}

		explanation.setText(s.toString());

		onUpdate.run();
	}

	public boolean isHighlightInCohort() {
		return highlightInCohort;
	}

	public Cohort getSelectedCohort() {
		return selectedCohort;
	}
}