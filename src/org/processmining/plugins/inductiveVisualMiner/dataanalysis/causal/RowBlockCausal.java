package org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal;

import java.util.ArrayList;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.dot.Dot;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

import gnu.trove.iterator.TIntIterator;

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
		Pair<Dot, CausalDataTable> p;
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

		List<DataRow<Object>> result = new ArrayList<>();

		CausalDataTable table = p.getB();
		for (Choice choice : table.getColumns()) {
			StringBuilder s = new StringBuilder();

			for (TIntIterator it = choice.nodes.iterator(); it.hasNext();) {
				int node = it.next();

				if (model.isActivity(node)) {
					s.append(model.getActivityName(node));
				} else if (model.isTau(node)) {
					s.append("[skip]");
				} else if (model.isTree()) {
					s.append("[" + model.getTree().getNodeType(node) + "]");
				} else {
					s.append("[" + node + "]");
				}

				if (it.hasNext()) {
					s.append(", ");
				}
			}

			result.add(new DataRow<Object>(choice.ids.toString(), DisplayType.literal(s.toString())));
		}

		return result;
	}
}