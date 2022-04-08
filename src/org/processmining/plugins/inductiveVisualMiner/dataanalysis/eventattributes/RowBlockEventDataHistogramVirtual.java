package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.ConfigurationWithDecorator;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class RowBlockEventDataHistogramVirtual<C extends ConfigurationWithDecorator, P>
		extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "event-att-hist-virt";
	}

	public String getStatusBusyMessage() {
		return "Computing virtual event attribute histograms..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(TraceDataRowBlock.merge(
						RowBlockEventDataHistogram.createAttributeData(logFiltered, attribute, canceller,
								configuration.getDecorator()),
						RowBlockEventDataHistogram.createAttributeData(negativeLog, attribute, canceller,
								configuration.getDecorator()),
						canceller));
			}
		} else {
			for (Attribute attribute : attributes.getEventAttributes()) {
				result.addAll(RowBlockEventDataHistogram.createAttributeData(logFiltered, attribute, canceller,
						configuration.getDecorator()));
			}
		}

		return result;
	}
}