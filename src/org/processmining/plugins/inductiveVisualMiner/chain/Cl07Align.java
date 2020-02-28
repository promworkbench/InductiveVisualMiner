package org.processmining.plugins.inductiveVisualMiner.chain;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Septuple;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.alignment.ImportAlignment;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl07Align extends
		ChainLink<Septuple<IvMModel, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses, InductiveVisualMinerAlignment, InductiveVisualMinerConfiguration>, Pair<IvMLogNotFiltered, IvMLogInfo>> {

	private static ConcurrentHashMap<Triple<IvMModel, XEventPerformanceClassifier, XLog>, SoftReference<IvMLogNotFiltered>> cache = new ConcurrentHashMap<>();

	protected Septuple<IvMModel, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses, InductiveVisualMinerAlignment, InductiveVisualMinerConfiguration> generateInput(
			InductiveVisualMinerState state) {
		return Septuple.of(state.getModel(), state.getPerformanceClassifier(), state.getSortedXLog(),
				state.getXLogInfo().getEventClasses(), state.getXLogInfoPerformance().getEventClasses(),
				state.getPreMinedIvMLog(), state.getConfiguration());
	}

	protected Pair<IvMLogNotFiltered, IvMLogInfo> executeLink(
			Septuple<IvMModel, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses, InductiveVisualMinerAlignment, InductiveVisualMinerConfiguration> input,
			IvMCanceller canceller) throws Exception {
		IvMModel model = input.getA();

		//see whether the alignment was pre-mined
		InductiveVisualMinerAlignment preMinedAlignment = input.getF();
		if (preMinedAlignment != null) {
			IvMLogNotFiltered ivmLog = ImportAlignment.getIvMLog(preMinedAlignment, input.getD(), input.getE());
			if (ivmLog != null) {
				System.out.println("Alignment imported");
				return Pair.of(ivmLog, new IvMLogInfo(ivmLog, model));
			}
			System.out.println("Alignment importing failed. Recomputing...");
		}

		//attempt to get the alignment from cache
		Triple<IvMModel, XEventPerformanceClassifier, XLog> cacheKey = Triple.of(model, input.getB(), input.getC());
		SoftReference<IvMLogNotFiltered> fromCacheReference = cache.get(cacheKey);
		if (fromCacheReference != null) {
			IvMLogNotFiltered fromCache = fromCacheReference.get();
			if (fromCache != null) {
				System.out.println("obtain alignment from cache");
				return Pair.of(fromCache, new IvMLogInfo(fromCache, model));
			}
		}

		IvMLogNotFiltered log = AlignmentPerformance.align(input.getG().getAlignmentComputer(), model, input.getB(),
				input.getC(), input.getD(), input.getE(), canceller);
		if (log == null && !canceller.isCancelled()) {
			throw new Exception("alignment failed");
		}
		if (canceller.isCancelled()) {
			return null;
		}
		IvMLogInfo logInfo = new IvMLogInfo(log, model);

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

	public String getStatusBusyMessage() {
		return "Aligning log and model..";
	}
}