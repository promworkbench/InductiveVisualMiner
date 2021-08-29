package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

public class DataRowBlockLogAttributes<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "log-att";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.input_log };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		XLog log = inputs.get(IvMObject.input_log);

		List<DataRow<Object>> result = new ArrayList<>();

		Collection<XAttribute> xAttributes = log.getAttributes().values();
		for (XAttribute xAttribute : xAttributes) {
			if (xAttribute instanceof XAttributeDiscrete) {
				result.add(new DataRow<Object>(DisplayType.numeric(((XAttributeDiscrete) xAttribute).getValue()),
						xAttribute.getKey()));
			} else if (xAttribute instanceof XAttributeContinuous) {
				result.add(new DataRow<Object>(DisplayType.numeric(((XAttributeContinuous) xAttribute).getValue()),
						xAttribute.getKey()));
			} else if (xAttribute instanceof XAttributeLiteral) {
				result.add(new DataRow<Object>(DisplayType.literal(((XAttributeLiteral) xAttribute).getValue()),
						xAttribute.getKey()));
			} else if (xAttribute instanceof XAttributeBoolean) {
				result.add(new DataRow<Object>(DisplayType.literal(((XAttributeBoolean) xAttribute).getValue() + ""),
						xAttribute.getKey()));
			} else {
				result.add(new DataRow<Object>(DisplayType.literal(xAttribute.toString()), xAttribute.getKey()));
			}
		}

		return result;
	}

}