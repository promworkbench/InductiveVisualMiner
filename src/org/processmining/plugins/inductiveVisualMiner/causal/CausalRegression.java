package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Set;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

public class CausalRegression {

	public static CausalAnalysisResult compute(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData,
			Set<Choice> adjustmentSet, IvMCanceller canceller) {

		return null;

		//		for (Pair<Choice, Choice> edge : graph.getEdges()) {
		//
		//			Choice choiceA = edge.getA();
		//			Choice choiceB = edge.getB();
		//			System.out.println(choiceA);
		//			if (choiceA.toString().equals("10-9i3-0") && choiceB.toString().equals("10-9i3-1")) {
		//				analysisResult.addEdgeCausal(edge, -0.624);
		//			} else if (choiceA.toString().equals("12-11i3-1") && choiceB.toString().equals("15-14i")) {
		//				analysisResult.addEdgeCausal(edge, 0.111);
		//				//analysisResult.addEdgeCausal(edge, 0);
		//			}
		//		}
	}

}