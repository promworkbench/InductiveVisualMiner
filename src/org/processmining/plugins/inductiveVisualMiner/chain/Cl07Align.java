package org.processmining.plugins.inductiveVisualMiner.chain;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.Quadruple;
import org.processmining.plugins.inductiveVisualMiner.alignment.AlignmentPerformance;
import org.processmining.plugins.inductiveVisualMiner.alignment.ImportAlignment;
import org.processmining.plugins.inductiveVisualMiner.alignment.InductiveVisualMinerAlignment;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogNotFiltered;
import org.processmining.plugins.inductiveVisualMiner.performance.XEventPerformanceClassifier;

public class Cl07Align extends DataChainLinkComputationAbstract<InductiveVisualMinerConfiguration> {

	private static ConcurrentHashMap<Quadruple<IvMModel, XEventPerformanceClassifier, XLog, String>, SoftReference<IvMLogNotFiltered>> cache = new ConcurrentHashMap<>();

	@Override
	public String getName() {
		return "align";
	}

	@Override
	public String getStatusBusyMessage() {
		return "Aligning log and model..";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.selected_classifier, IvMObject.sorted_log,
				IvMObject.xlog_info, IvMObject.xlog_info_performance };
	}

	@Override
	public IvMObject<?>[] createOptionalObjects() {
		return new IvMObject<?>[] { IvMObject.imported_alignment };
	}

	@Override
	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.aligned_log, IvMObject.aligned_log_info };
	}

	@Override
	public IvMObjectValues execute(InductiveVisualMinerConfiguration configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {

		//	protected Septuple<IvMModel, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses, InductiveVisualMinerAlignment, InductiveVisualMinerConfiguration> generateInput(
		//			InductiveVisualMinerState state) {
		//		return Septuple.of(state.getModel(), state.getPerformanceClassifier(), state.getSortedXLog(),
		//				state.getXLogInfo().getEventClasses(), state.getXLogInfoPerformance().getEventClasses(),
		//				state.getPreMinedIvMLog(), state.getConfiguration());
		//	}
		//
		//	protected Pair<IvMLogNotFiltered, IvMLogInfo> executeLink(
		//			Septuple<IvMModel, XEventPerformanceClassifier, XLog, XEventClasses, XEventClasses, InductiveVisualMinerAlignment, InductiveVisualMinerConfiguration> input,
		//			IvMCanceller canceller) throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		XEventPerformanceClassifier performanceClassifier = new XEventPerformanceClassifier(
				AttributeClassifiers.constructClassifier(inputs.get(IvMObject.selected_classifier)));
		XLog log = inputs.get(IvMObject.sorted_log);
		XEventClasses eventClasses = inputs.get(IvMObject.xlog_info).getEventClasses();
		XEventClasses eventClassesPerformance = inputs.get(IvMObject.xlog_info_performance).getEventClasses();

		if (inputs.has(IvMObject.imported_alignment)) {
			//see whether the alignment was pre-mined
			InductiveVisualMinerAlignment preMinedAlignment = inputs.get(IvMObject.imported_alignment);
			IvMLogNotFiltered ivmLog = ImportAlignment.getIvMLog(preMinedAlignment, eventClasses,
					eventClassesPerformance, configuration.getDecorator());
			if (ivmLog != null) {
				System.out.println("Alignment imported");
				return new IvMObjectValues().//
						s(IvMObject.aligned_log, ivmLog).//
						s(IvMObject.aligned_log_info, new IvMLogInfo(ivmLog, model));
			}
			System.out.println("Alignment importing failed. Recomputing...");
		}

		//attempt to get the alignment from cache
		Quadruple<IvMModel, XEventPerformanceClassifier, XLog, String> cacheKey = Quadruple.of(model,
				performanceClassifier, log, configuration.getAlignmentComputer().getUniqueIdentifier());
		SoftReference<IvMLogNotFiltered> fromCacheReference = cache.get(cacheKey);
		if (fromCacheReference != null) {
			IvMLogNotFiltered fromCache = fromCacheReference.get();
			if (fromCache != null) {
				System.out.println("obtain alignment from cache");
				IvMLogInfo logInfo = new IvMLogInfo(fromCache, model);

				return new IvMObjectValues().//
						s(IvMObject.aligned_log, fromCache).//
						s(IvMObject.aligned_log_info, logInfo);
			}
		}

		IvMLogNotFiltered aLog = AlignmentPerformance.align(configuration.getAlignmentComputer(), model,
				performanceClassifier, log, eventClasses, eventClassesPerformance, canceller,
				configuration.getDecorator());
		if (aLog == null && !canceller.isCancelled()) {
			throw new Exception("alignment failed");
		}
		if (canceller.isCancelled()) {
			return null;
		}
		IvMLogInfo logInfo = new IvMLogInfo(aLog, model);

		//cache the alignment
		cache.put(cacheKey, new SoftReference<IvMLogNotFiltered>(aLog));

		return new IvMObjectValues().//
				s(IvMObject.aligned_log, aLog).//
				s(IvMObject.aligned_log_info, logInfo);
	}
}