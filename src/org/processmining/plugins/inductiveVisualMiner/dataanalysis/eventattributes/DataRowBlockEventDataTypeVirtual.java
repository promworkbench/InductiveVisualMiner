package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class DataRowBlockEventDataTypeVirtual<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "event-att-type-virt";
	}

	public String getStatusBusyMessage() {
		return "Gathering virtual event attribute types..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		List<DataRow<Object>> result = new ArrayList<>();

		for (Attribute attribute : attributes.getEventAttributes()) {
			String type = "";
			if (attribute.isNumeric()) {
				type += "numeric";
			} else if (attribute.isBoolean()) {
				type += "boolean";
			} else if (attribute.isTime()) {
				type += "time";
			} else if (attribute.isLiteral()) {
				type += "literal";
			} else if (attribute.isDuration()) {
				type += "duration";
			} else {
				type += "other";
			}

			result.add(new DataRow<>(attribute.getName(), "data type", DisplayType.literal(type)));
		}

		return result;
	}

}
