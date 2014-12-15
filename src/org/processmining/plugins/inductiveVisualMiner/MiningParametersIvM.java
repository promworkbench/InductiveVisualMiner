package org.processmining.plugins.inductiveVisualMiner;

import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.plugins.InductiveMiner.dfgOnly.log2dfg.IMLog2IMLogInfoDefault;
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
import org.processmining.plugins.InductiveMiner.mining.logSplitter.LogSplitterIvM;

public class MiningParametersIvM extends MiningParameters {

	/*
	 * No other parameter, except mentioned in this file, has influence on the mined model
	 */
	
	public MiningParametersIvM() {
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
		
		setLogSplitter(new LogSplitterIvM());
		
		setFallThroughs(new ArrayList<FallThrough>(Arrays.asList(
				new FallThroughLeaveOutActivitiesThenApplyOthers(),
				new FallThroughTauLoop(),
				new FallThroughFlower()
				)));
		
		//set parameters
		setNoiseThreshold((float) 0.2);
		setReduce(true);
		
		setDebug(false);
	}
}
