package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import org.processmining.plugins.InductiveMiner.Pair;

public class CausalAnalysis {
	public static CausalAnalysisResult analyse(CausalGraph graph, CausalDataTable table) {
		//TODO: perform the causal analysis

		//TODO: for now, hardcode the values
		CausalAnalysisResult analysisResult = new CausalAnalysisResult();

		for (Pair<Choice, Choice> edge : graph.getEdges()) {

			Choice choiceA = edge.getA();
			Choice choiceB = edge.getB();
			System.out.println(choiceA);
			if (choiceA.toString().equals("10-9i3-0") && choiceB.toString().equals("10-9i3-1")) {
				analysisResult.addEdgeCausal(edge, -0.624);
			} else if (choiceA.toString().equals("12-11i3-1") && choiceB.toString().equals("15-14i")) {
				analysisResult.addEdgeCausal(edge, 0.111);
				//analysisResult.addEdgeCausal(edge, 0);
			}
		}

		return analysisResult;
	}
}
