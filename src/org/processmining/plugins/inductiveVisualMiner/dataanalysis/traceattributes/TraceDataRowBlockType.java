package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

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

public class TraceDataRowBlockType<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "trace-att-type";
	}
	
	public String getStatusBusyMessage() {
		return "Gathering trace attribute types..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.attributes_info };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		AttributesInfo attributes = inputs.get(IvMObject.attributes_info);

		List<DataRow<Object>> result = new ArrayList<>();

		for (Attribute attribute : attributes.getTraceAttributes()) {
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

			result.add(new DataRow<Object>(attribute.getName(), "data type", DisplayType.literal(type)));
		}

		return result;
	}

}
