package org.processmining.plugins.inductiveVisualMiner.dataanalysis.modeltime;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockAbstract;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceDataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFilteredImpl;
import org.processmining.plugins.inductiveVisualMiner.performance.Aggregate;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveVisualMiner.performance.Performance;

public class RowBlockModelPerformance<C, P> extends DataRowBlockAbstract<Object, C, P> {

	@Override
	public String getName() {
		return "model performance";
	}

	@Override
	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.data_analyses_delay,
				IvMObject.performance, IvMObject.performance_negative };
	}

	@Override
	public List<DataRow<Object>> gather(IvMObjectValues inputs) {
		IvMModel model = inputs.get(IvMObject.model);
		Performance performance = inputs.get(IvMObject.performance);
		IvMLogFilteredImpl log = inputs.get(IvMObject.aligned_log_filtered);

		if (log.isSomethingFiltered()) {
			Performance performanceNegative = inputs.get(IvMObject.performance_negative);
			return TraceDataRowBlock.merge(createAttributeData(model, performance),
					createAttributeData(model, performanceNegative), null);
		} else {
			return createAttributeData(model, performance);
		}
	}

	private List<DataRow<Object>> createAttributeData(IvMModel model, Performance performance) {
		List<DataRow<Object>> result = new ArrayList<>();

		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {

				for (DurationType type : DurationType.values()) {
					for (Aggregate gather : Aggregate.values()) {
						long m = performance.getNodeMeasure(type, gather, node);
						if (m > -1) {
							result.add(new DataRow<Object>(DisplayType.duration(m), model.getActivityName(node),
									type.toString(), gather.toString()));
						}
					}
				}
			}
		}

		return result;
	}
}
