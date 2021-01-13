package org.processmining.plugins.inductiveVisualMiner.chain;

import java.util.Collection;
import java.util.Iterator;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers.AttributeClassifier;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.attributes.IvMVirtualAttributeFactory;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfoImpl;

public class Cl01GatherAttributes implements DataChainLinkComputation {

	public String getName() {
		return "gather attributes";
	}

	public String getStatusBusyMessage() {
		return "Gathering attributes..";
	}

	public String[] getInputNames() {
		return new String[] { DataState.input_log };
	}

	public String[] getOutputNames() {
		return new String[] { DataState.log_info, DataState.initial_classifier, DataState.classifiers };
	}

	@Override
	public Object[] execute(InductiveVisualMinerConfiguration configuration, Object[] inputs, IvMCanceller canceller)
			throws Exception {
		XLog log = (XLog) inputs[0];
		IvMVirtualAttributeFactory virtualAttributes = configuration.getVirtualAttributes();
		AttributesInfo info = new AttributesInfoImpl(log, virtualAttributes);
		Collection<Attribute> attributes = info.getEventAttributes();

		String[] names = new String[attributes.size()];
		Iterator<Attribute> it = attributes.iterator();
		for (int i = 0; i < names.length; i++) {
			names[i] = it.next().getName();
		}
		Pair<AttributeClassifier[], AttributeClassifier> p = AttributeClassifiers.getAttributeClassifiers(log, names,
				true);
		AttributeClassifier[] attributeClassifiers = p.getA();

		//construct the initial classifier
		AttributeClassifier firstClassifier = p.getB();
		XEventClassifier initialClassifier = AttributeClassifiers.constructClassifier(firstClassifier);

		return new Object[] { info, initialClassifier, attributeClassifiers };
	}

}
