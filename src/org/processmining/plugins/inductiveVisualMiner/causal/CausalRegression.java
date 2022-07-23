package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Set;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public class CausalRegression {

	public static CausalAnalysisResult compute(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData,
			IvMCanceller canceller) {

		CausalAnalysisResult result = new CausalAnalysisResult();

		for (Pair<Choice, Choice> edge : binaryCausalGraph.getEdges()) {
			Choice source = edge.getA();
			Choice target = edge.getB();

			double causalStrength = computeCausalStrength(binaryCausalGraph, binaryChoiceData, source, target);

			result.addEdgeCausal(Pair.of(source, target), causalStrength);
		}

		return result;
	}

	private static double computeCausalStrength(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData,
			Choice source, Choice target) {
		Set<Choice> adjustmentSet = CausalBackdoorCriterion.compute(binaryCausalGraph, binaryChoiceData, source,
				target);

		return 1;
	}
}