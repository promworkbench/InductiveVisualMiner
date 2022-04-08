package org.processmining.plugins.inductiveVisualMiner.chain;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal.CausalDataTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal.CausalGraph;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal.DirectlyFollowsModel2CausalGraph;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.causal.EfficientTree2CausalGraph;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogFiltered;

public class Cl22AdvancedAnalysisCausal<C> extends DataChainLinkComputationAbstract<C> {

	public String getName() {
		return "Cl22 causal";
	}

	public String getStatusBusyMessage() {
		return "Computing causalities..";
	}

	public IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] { IvMObject.model, IvMObject.aligned_log_filtered, IvMObject.data_analyses_delay,
				IvMObject.selected_causal_enabled };
	}

	public IvMObject<?>[] createOutputObjects() {
		return new IvMObject<?>[] { IvMObject.data_analysis_causal_upper_bound_graph,
				IvMObject.data_analysis_causal_data_table };
	}

	public IvMObjectValues execute(C configuration, IvMObjectValues inputs, IvMCanceller canceller) throws Exception {
		if (inputs.get(IvMObject.selected_causal_enabled)) {
			IvMModel model = inputs.get(IvMObject.model);
			IvMLogFiltered logFiltered = inputs.get(IvMObject.aligned_log_filtered);

			int maxUnfolding = 2;

			//compute causal objects
			Pair<CausalGraph, CausalDataTable> p;
			if (model.isTree()) {
				p = EfficientTree2CausalGraph.convert(model.getTree(), logFiltered, maxUnfolding);
			} else {
				p = DirectlyFollowsModel2CausalGraph.convert(model.getDfg(), logFiltered, maxUnfolding);
			}

			//		System.out.println(p);
			//
			if (false) {
				try {
					String name = "bpic17-o-dfm";
					FileUtils.writeStringToFile(
							new File("/home/sander/Documents/svn/49 - causality in process mining - niek/experiments/"
									+ name + ".dot"),
							p.getA().toDot().toString());
					FileUtils.writeStringToFile(
							new File("/home/sander/Documents/svn/49 - causality in process mining - niek/experiments/"
									+ name + ".csv"),
							p.getB().toString(-1));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			//			System.out.println("choices " + p.getB().getColumns().size());
			//			System.out.println("UBCG edges " + p.getA().getEdges().size());

			//perform the analysis
			//CausalAnalysisResult analysisResult = CausalAnalysis.analyse(p.getA(), p.getB());

			//CausalAnalysisResult2Correlation.convert(p.getA(), p.getB(), model);

			return new IvMObjectValues().//
					s(IvMObject.data_analysis_causal_upper_bound_graph, p.getA()) //
					.s(IvMObject.data_analysis_causal_data_table, p.getB());

		} else {
			return null;
		}
	}
}