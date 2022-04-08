package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.attributes.IvMAttributesInfo;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.ConfigurationWithDecorator;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class DataRowBlockTraceHistogramVirtual<C extends ConfigurationWithDecorator, P>
		extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "trace-att-hist-virt";
	}

	public String getStatusBusyMessage() {
		return "Computing virtual trace attribute histograms..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.ivm_attributes_info, IvMObject.aligned_log_filtered,
				IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(ConfigurationWithDecorator configuration, IvMObjectValues inputs,
			IvMCanceller canceller) throws Exception {
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);
		IvMAttributesInfo attributes = inputs.get(IvMObject.ivm_attributes_info);

		List<DataRow<Object>> result = new ArrayList<>();

		if (logFiltered.isSomethingFiltered()) {
			IvMLogFilteredImpl negativeLog = logFiltered.clone();
			negativeLog.invert();

			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(DataRowBlockTrace.merge(
						DataRowBlockTraceHistogram.createAttributeData(logFiltered, attribute, canceller,
								configuration.getDecorator()),
						DataRowBlockTraceHistogram.createAttributeData(negativeLog, attribute, canceller,
								configuration.getDecorator()),
						canceller));
			}
		} else {
			for (Attribute attribute : attributes.getTraceAttributes()) {
				result.addAll(DataRowBlockTraceHistogram.createAttributeData(logFiltered, attribute, canceller,
						configuration.getDecorator()));
			}
		}

		return result;
	}
}