package org.processmining.plugins.inductiveVisualMiner.chain;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Quintuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl07Align extends
		ChainLink<Quintuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses>, Pair<IvMLogNotFiltered, IvMLogInfo>> {

	private static ConcurrentHashMap<Triple<IvMEfficientTree, XEventPerformanceClassifier, XLog>, SoftReference<IvMLogNotFiltered>> cache = new ConcurrentHashMap<>();

	protected Quintuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses> generateInput(
			InductiveVisualMinerState state) {
		return Quintuple.of(state.getTree(), state.getPerformanceClassifier(), state.getSortedXLog(),
				state.getXLogInfo().getEventClasses(), state.getXLogInfoPerformance().getEventClasses());
	}

	protected Pair<IvMLogNotFiltered, IvMLogInfo> executeLink(
			Quintuple<IvMEfficientTree, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses> input,
			IvMCanceller canceller) throws Exception {
		IvMEfficientTree tree = input.getA();

		//attempt to get the alignment from cache
		Triple<IvMEfficientTree, XEventPerformanceClassifier, XLog> cacheKey = Triple.of(input.getA(), input.getB(),
				input.getC());
		SoftReference<IvMLogNotFiltered> fromCacheReference = cache.get(cacheKey);
		if (fromCacheReference != null) {
			IvMLogNotFiltered fromCache = fromCacheReference.get();
			if (fromCache != null) {
				System.out.println("obtain alignment from cache");
				return Pair.of(fromCache, new IvMLogInfo(fromCache, tree));
			}
		}

		IvMLogNotFiltered log = AlignmentPerformance.align(tree, input.getB(), input.getC(), input.getD(), input.getE(),
				canceller);
		if (log == null) {
			return null;
		}
		IvMLogInfo logInfo = new IvMLogInfo(log, tree);

		//cache the alignment
		cache.put(cacheKey, new SoftReference<IvMLogNotFiltered>(log));

		return Pair.of(log, logInfo);
	}

	protected void processResult(Pair<IvMLogNotFiltered, IvMLogInfo> result, InductiveVisualMinerState state) {
		state.setIvMLog(result.getA(), result.getB());
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setIvMLog(null, null);	
	}

	public String getName() {
		return "align";
	}
}
