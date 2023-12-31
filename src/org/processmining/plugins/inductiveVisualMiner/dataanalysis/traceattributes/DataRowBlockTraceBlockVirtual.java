package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class DataRowBlockTraceBlockVirtual<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "trace-att-virt";
	}

	public String getStatusBusyMessage() {
		return "Gathering virtual trace attributes..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		//count number of traces
		int numberOfTraces = DataRowBlockTrace.getNumberOfTraces(logFiltered);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();
			int numberOfTracesNegative = DataRowBlockTrace.getNumberOfTraces(negativeLog);

			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(DataRowBlockTrace.merge(
						DataRowBlockTrace.createAttributeData(logFiltered, attribute, numberOfTraces, canceller),
						DataRowBlockTrace.createAttributeData(negativeLog, attribute, numberOfTracesNegative,
								canceller),
						canceller));
			}
		} else {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(DataRowBlockTrace.createAttributeData(logFiltered, attribute, numberOfTraces, canceller));
			}
		}

		return result;
	}
}