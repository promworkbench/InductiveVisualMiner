package org.processmining.plugins.inductiveVisualMiner.visualMinerWrapper.miners;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinder;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIM;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyLog;
import org.processmining.plugins.InductiveMiner.mining.baseCases.BaseCaseFinderIMiEmptyTrace;
import org.processmining.plugins.InductiveMiner.mining.cuts.CutFinder;
import org.processmining.plugins.InductiveMiner.mining.cuts.IM.CutFinderIM;
import org.processmining.plugins.InductiveMiner.mining.cuts.IMi.CutFinderIMi;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThrough;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughFlower;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughLeaveOutActivitiesThenApplyOthers;
import org.processmining.plugins.InductiveMiner.mining.fallthrough.FallThroughTauLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterCombination;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterInterleaving;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterLoop;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterParallel;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterSequenceFiltering;
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterXor;

public class NoLifeCycleMiningParametersIvM extends MiningParameters {

	/*
	 * No other parameter, except mentioned in this file, has influence on the mined model
	 */
	
	public NoLifeCycleMiningParametersIvM() {
		//determine algorithm
		
		setLogConverter(new IMLog2IMLogInfoDefault());
		
		setBaseCaseFinders(new ArrayList<BaseCaseFinder>(Arrays.asList(
				new BaseCaseFinderIMiEmptyLog(),
				new BaseCaseFinderIMiEmptyTrace(),
				new BaseCaseFinderIMi(),
				new BaseCaseFinderIM()
				)));
		
		setCutFinder(new ArrayList<CutFinder>(Arrays.asList(
				new CutFinderIM(),
				new CutFinderIMi()
				)));
		
		setLogSplitter(new LogSplitterCombination(
						new LogSplitterXor(), 
						new LogSplitterSequenceFiltering(), 
						new LogSplitterParallel(),
						new LogSplitterLoop(),
						new LogSplitterInterleaving()
		));
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughLeaveOutActivitiesThenApplyOthers(),
				new FallThroughTauLoop(false),
				new FallThroughFlower()
				)));
		
		//set parameters
		setNoiseThreshold((float) 0.2);
	}
}
