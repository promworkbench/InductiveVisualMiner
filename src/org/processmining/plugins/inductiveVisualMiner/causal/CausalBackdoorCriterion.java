package org.processmining.plugins.inductiveVisualMiner.causal;

import java.util.Set;

import gnu.trove.set.hash.THashSet;

public class CausalBackdoorCriterion {

	public static Set<Choice> compute(CausalGraph binaryCausalGraph, CausalDataTable binaryChoiceData, Choice source,
			Choice target) {
		THashSet<Choice> result = new THashSet<>();
		return result;
	}

}