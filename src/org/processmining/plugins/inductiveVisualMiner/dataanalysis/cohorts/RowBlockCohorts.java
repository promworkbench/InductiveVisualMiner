package org.processmining.plugins.inductiveVisualMiner.dataanalysis.cohorts;

import java.util.ArrayList;
import java.util.List;

import org.processmining.cohortanalysis.cohort.Cohort;
import org.processmining.cohortanalysis.cohort.Cohorts;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

public class RowBlockCohorts<C, P> extends DataRowBlockAbstract<Cohort, C, P> {

	public String getName() {
		return "cohort-att";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_cohort };
	}

	public List<DataRow<Cohort>> gather(IvMObjectValues inputs) {
		Cohorts cohorts = inputs.get(IvMObject.data_analysis_cohort);

		List<DataRow<Cohort>> result = new ArrayList<>();

		for (Cohort cohort : cohorts) {
			result.add(new DataRow<>(//
					cohort.getFeatures().iterator().next().getDescriptionField(), //
					cohort, //
					DisplayType.html(cohort.getFeatures().iterator().next().getDescriptionSelector()), //
					DisplayType.numericUnpadded(cohort.getSize()), //
					DisplayType.numeric(cohort.getDistance())));
		}

		return result;
	}

}
