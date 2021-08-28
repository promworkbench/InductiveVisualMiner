package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class EventDataRowBlockVirtual<C, P> extends DataRowBlockComputer<C, P> {

	public String getName() {
		return "event-att-virt";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.aligned_log_filtered };
	}

	public List<DataRow> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		List<DataRow> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(TraceDataRowBlock.merge(
						EventDataRowBlock.createAttributeData(logFiltered, attribute, canceller),
						EventDataRowBlock.createAttributeData(negativeLog, attribute, canceller), canceller));
			}
		} else {
			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(EventDataRowBlock.createAttributeData(logFiltered, attribute, canceller));
			}
		}

		return result;
	}
}