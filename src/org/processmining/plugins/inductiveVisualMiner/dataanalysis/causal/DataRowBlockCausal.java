package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.causal.CausalAnalysisResult;
import org.processmining.plugins.inductiveVisualMiner.causal.Choice;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;

public class DataRowBlockCausal<C, P> extends DataRowBlockAbstract<Object, C, P> {

	@Override
	public String getName() {
		return "causal";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.data_analysis_causal };
	}

	@Override
	public List<DataRow<Object>> gather(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		CausalAnalysisResult analysisResult = inputs.get(IvMObject.data_analysis_causal);

		//display the results
		List<DataRow<Object>> result = new ArrayList<>();
		for (Pair<Pair<Choice, Choice>, Double> x : analysisResult.getResultsCausal()) {
			result.add(new DataRow<Object>(x.getA().getA().toString(model), x.getA().getB().toString(model),
					DisplayType.numeric(x.getB())));
		}

		return result;
	}
}