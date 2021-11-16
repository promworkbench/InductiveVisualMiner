package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
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

		//compute causal objects
		Pair<CausalGraph, CausalDataTable> p;
		if (model.isTree()) {
			p = EfficientTree2CausalGraph.convert(model.getTree(), logFiltered);
		} else {
			p = DirectlyFollowsModel2CausalGraph.convert(model.getDfg(), logFiltered);
		}

		//		System.out.println(p);
		//
		//		try {
		//			FileUtils.writeStringToFile(
		//					new File("/home/sander/Documents/svn/49 - causality in process mining - niek/bpic12a.dot"),
		//					p.getA().toString());
		//			FileUtils.writeStringToFile(
		//					new File("/home/sander/Documents/svn/49 - causality in process mining - niek/bpic12a.csv"),
		//					p.getB().toString(-1));
		//		} catch (IOException e1) {
		//			e1.printStackTrace();
		//		}

		//perform the analysis
		CausalAnalysisResult analysisResult = CausalAnalysis.analyse(p);

		//display the results
		List<DataRow<Object>> result = new ArrayList<>();
		for (Pair<Pair<Choice, Choice>, Double> x : analysisResult.getResults()) {
			result.add(new DataRow<Object>(x.getA().getA().toString(model), x.getA().getB().toString(model),
					DisplayType.numeric(x.getB())));
		}

		return result;
	}
}