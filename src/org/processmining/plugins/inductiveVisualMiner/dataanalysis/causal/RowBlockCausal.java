package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class RowBlockCausal<C, P> extends DataRowBlockComputer<Object, C, P> {

	public String getName() {
		return "causal-graph";
	}

	public String getStatusBusyMessage() {
		return "Gathering trace attributes..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.data_analyses_delay };
	}

	public List<DataRow<Object>> compute(C configuration, IvMObjectValues inputs, IvMCanceller canceller)
			throws Exception {
		IvMModel model = inputs.get(IvMObject.model);
		IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);

		List<DataRow<Object>> result = new ArrayList<>();

		Dot dot;
		if (model.isTree()) {
			dot = EfficientTree2CausalGraph.convert(model.getTree(), logFiltered);
		} else {
			dot = new DirectlyFollowsModel2Choices().getChoices(model.getDfg(), logFiltered);
			result.add(new DataRow<>(DisplayType.literal("only trees supported")));
		}
		System.out.println(dot);
		
		try {
			FileUtils.writeStringToFile(
					new File("/home/sander/Documents/svn/49 - causality in process mining - niek/bpic12a.dot"),
					dot.toString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		return result;
	}
}