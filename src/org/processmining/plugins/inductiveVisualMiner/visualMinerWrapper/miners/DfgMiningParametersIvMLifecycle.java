package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.DfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgBaseCaseFinder.SimpleDfgBaseCaseFinder;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderCombination;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderNoiseFiltering;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgCutFinder.DfgCutFinderSimple;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThrough;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgFallThrough.DfgFallThroughFlower;
import org.processmining.plugins.InductiveMiner.dfgOnly.dfgSplitter.SimpleDfgSplitter;

public class DfgMiningParametersIvMLifecycle extends DfgMiningParameters {
	public DfgMiningParametersIvMLifecycle() {
		setDfgBaseCaseFinders(new ArrayList<DfgBaseCaseFinder>(Arrays.asList(
				new SimpleDfgBaseCaseFinder()
				)));

		setDfgCutFinder(new DfgCutFinderCombination(
				new DfgCutFinderSimple(),
				new DfgCutFinderNoiseFiltering()
				));

		setDfgFallThroughs(new ArrayList<DfgFallThrough>(Arrays.asList(
				new DfgFallThroughFlower()
				)));

		setDfgSplitter(
				new SimpleDfgSplitter()
				);

		setReduce(true);
		setNoiseThreshold(0.2f);
	}
}
