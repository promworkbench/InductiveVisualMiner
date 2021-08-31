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

public class TraceDataRowBlockVirtual<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "trace-att-virt";
	}
	
	public String getStatusBusyMessage() {
		return "Gathering virtual trace attributes..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.aligned_log_filtered };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		//count number of traces
		int numberOfTraces = TraceDataRowBlock.getNumberOfTraces(logFiltered);
		double[] trace2fitness = TraceDataRowBlock.getTrace2fitness(logFiltered, numberOfTraces);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();
			int numberOfTracesNegative = TraceDataRowBlock.getNumberOfTraces(negativeLog);
			double[] trace2fitnessNegative = TraceDataRowBlock.getTrace2fitness(negativeLog, numberOfTracesNegative);

			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(TraceDataRowBlock.merge(
						TraceDataRowBlock.createAttributeData(logFiltered, attribute, numberOfTraces, trace2fitness,
								canceller),
						TraceDataRowBlock.createAttributeData(negativeLog, attribute, numberOfTracesNegative,
								trace2fitnessNegative, canceller),
						canceller));
			}
		} else {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(TraceDataRowBlock.createAttributeData(logFiltered, attribute, numberOfTraces,
						trace2fitness, canceller));
			}
		}

		return result;
	}
}