package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.Random;

import org.processmining.plugins.InductiveMiner.Pair;

public class CausalAnalysis {
	public static CausalAnalysisResult analyse(CausalGraph graph, CausalDataTable table) {
		//TODO: perform the causal analysis

		//TODO: for now, make up random values
		Random random = new Random();

		CausalAnalysisResult analysisResult = new CausalAnalysisResult();

		for (Pair<Choice, Choice> edge : graph.getEdges()) {
			analysisResult.addEdgeCausal(edge, random.nextDouble());
		}

		return analysisResult;
	}
}
