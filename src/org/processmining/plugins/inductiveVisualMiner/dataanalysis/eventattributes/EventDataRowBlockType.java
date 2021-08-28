package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class EventDataRowBlockType<C, P> extends DataRowBlockComputer<C, P> {

	public String getName() {
		return "event-att-type";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info };
	}

	public List<DataRow> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		AttributesInfo attributes = inputs.get(IvMObject.attributes_info);

		List<DataRow> result = new ArrayList<>();

		for (Attribute attribute : attributes.getEventAttributes()) {
			String type = "";
			if (attribute.isNumeric()) {
				type += "numeric";
			} else if (attribute.isTime()) {
				type += "time";
			} else if (attribute.isLiteral()) {
				type += "literal";
			} else if (attribute.isDuration()) {
				type += "duration";
			} else {
				type += "other";
			}

			result.add(new DataRow(attribute.getName(), "data type", DisplayType.literal(type)));
		}

		return result;
	}

}
