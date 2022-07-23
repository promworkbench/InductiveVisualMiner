package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

import gnu.trove.map.hash.THashMap;

public class CausalAnalysis {
	public static CausalAnalysisResult analyse(IvMModel model, IvMLogFiltered logFiltered, IvMCanceller canceller) {

		int maxUnfolding = 2;

		//compute upper bound causal graph and choice data
		Pair<CausalGraph, CausalDataTable> p;
		if (model.isTree()) {
			p = EfficientTree2UpperBoundCausalGraph.convert(model.getTree(), logFiltered, maxUnfolding, canceller);
		} else {
			p = DirectlyFollowsModel2UpperBoundCausalGraph.convert(model.getDfg(), logFiltered, maxUnfolding,
					canceller);
		}

		if (canceller.isCancelled()) {
			return null;
		}

		CausalGraph upperBoundCausalGraph = p.getA();
		CausalDataTable choiceData = p.getB();

		System.out.println(upperBoundCausalGraph.toDot());

		//binarise upper bound causal graph and choice data
		Quadruple<CausalGraph, CausalDataTable, THashMap<Choice, Set<Choice>>, THashMap<Choice, Choice>> t = BinariseCausalGraphAndChoiceData
				.binarise(upperBoundCausalGraph, choiceData, canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		CausalGraph binaryUpperBoundCausalGraph = t.getA();
		CausalDataTable binaryChoiceData = t.getB();

		System.out.println(binaryUpperBoundCausalGraph.toDot());

		//compute causal graph
		CausalGraph binaryCausalGraph = MVPC.compute(binaryUpperBoundCausalGraph, binaryChoiceData, canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		//perform regression
		CausalAnalysisResult causalAnalysisResult = CausalRegression.compute(binaryCausalGraph, binaryChoiceData,
				canceller);

		if (canceller.isCancelled()) {
			return null;
		}

		return causalAnalysisResult;
	}
}
