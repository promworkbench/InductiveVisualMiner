package org.processmining.plugins.inductiveVisualMiner.chain;

import org.apache.commons.lang3.ArrayUtils;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;

public class Cl00GatherAttributes extends ChainLink<XLog, Triple<AttributesInfo, XEventClassifier, String[]>> {

	protected XLog generateInput(InductiveVisualMinerState state) {
		return state.getXLog();
	}

	protected Triple<AttributesInfo, XEventClassifier, String[]> executeLink(XLog input, IvMCanceller canceller)
			throws Exception {
		AttributesInfo info = new AttributesInfo(input);
		String[] classifiers = info.getEventAttributes();

		//remove lifecycle:transition as it interferes with performance measures
		int i = ArrayUtils.indexOf(classifiers, XLifecycleExtension.KEY_TRANSITION);
		if (i != ArrayUtils.INDEX_NOT_FOUND) {
			classifiers = ArrayUtils.remove(info.getEventAttributes(), i);
		}

		/**
		 * Choose a classifier: if concept:name is available, choose that.
		 */
		XEventClassifier classifier;
		if (ArrayUtils.contains(info.getEventAttributes(), XConceptExtension.KEY_NAME)) {
			classifier = new XEventAttributeClassifier(XConceptExtension.KEY_NAME, XConceptExtension.KEY_NAME);
		} else if (classifiers.length > 0) {
			classifier = new XEventAttributeClassifier(classifiers[0], classifiers[0]);
		} else {
			classifier = new XEventAttributeClassifier("empty classifier");
		}

		return Triple.of(info, classifier, classifiers);
	}

	protected void processResult(Triple<AttributesInfo, XEventClassifier, String[]> result,
			InductiveVisualMinerState state) {
		state.setAttributesInfo(result.getA(), result.getC());
		state.setClassifier(result.getB());
	}

}
