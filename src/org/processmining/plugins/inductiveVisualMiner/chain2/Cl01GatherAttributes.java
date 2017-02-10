package org.processmining.plugins.inductiveVisualMiner.chain2;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.AttributesInfo;

public class Cl01GatherAttributes
		extends ChainLink2<XLog, Triple<AttributesInfo, AttributeClassifier, AttributeClassifier[]>> {

	protected XLog generateInput(InductiveVisualMinerState state) {
		return state.getXLog();
	}

	protected Triple<AttributesInfo, AttributeClassifier, AttributeClassifier[]> executeLink(XLog input,
			IvMCanceller canceller) throws Exception {
		AttributesInfo info = new AttributesInfo(input);
		String[] attributes = info.getEventAttributes();

		Pair<AttributeClassifier[], AttributeClassifier> p = AttributeClassifiers.getAttributeClassifiers(input,
				attributes, true);
		AttributeClassifier[] attributeClassifiers = p.getA();
		AttributeClassifier firstClassifier = p.getB();

		return Triple.of(info, firstClassifier, attributeClassifiers);
	}

	protected void processResult(Triple<AttributesInfo, AttributeClassifier, AttributeClassifier[]> result,
			InductiveVisualMinerState state) {
		state.setAttributesInfo(result.getA(), result.getB(), result.getC());
		state.setClassifier(AttributeClassifiers.constructClassifier(result.getB()));
	}

	protected void invalidateResult(InductiveVisualMinerState state) {
		state.setAttributesInfo(null, null, null);
		state.setClassifier(null);
	}

}
