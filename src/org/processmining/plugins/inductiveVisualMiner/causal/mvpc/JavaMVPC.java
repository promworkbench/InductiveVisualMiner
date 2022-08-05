package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

import java.util.List;

import org.processmining.plugins.inductiveVisualMiner.causal.CausalDataTable;
import org.processmining.plugins.inductiveVisualMiner.causal.Choice;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMCanceller;

import gnu.trove.iterator.TObjectIntIterator;

public class JavaMVPC {

	public static DiGraph compute(CausalDataTable binaryChoiceData, IvMCanceller canceller) {

		double[][] data = new double[binaryChoiceData.getNumberOfRows()][binaryChoiceData.getNumberOfColumns()];
		int newRowIndex = 0;
		for (TObjectIntIterator<int[]> it = binaryChoiceData.iterator(); it.hasNext();) {
			it.advance();
			int[] row = it.key();

			for (int m = 0; m < it.value(); m++) {
				for (int columnIndex = 0; columnIndex < row.length; columnIndex++) {
					if (row[columnIndex] == binaryChoiceData.NO_VALUE) {
						//choice was not encountered
						data[newRowIndex][columnIndex] = Double.NaN;
					} else {
						//choice was encountered
						Choice choice = binaryChoiceData.getColumns().get(columnIndex);

						data[newRowIndex][columnIndex] = row[columnIndex] == choice.nodes.iterator().next() ? 1 : 0;
					}
				}

				newRowIndex++;
			}
		}

		//function defaults
		double alpha = 0.05;
		IndependenceTest indep_test = IndependenceTest.fisherz;
		boolean stable = true;
		int uc_rule = 0;
		Priority uc_priority = Priority.p2;
		boolean mvpc = false;
		Correction correction_name = Correction.MV_Crtn_Fisher_Z;
		BackgroundKnowledge background_knowledge = null;
		boolean verbose = false;
		boolean show_progress = true;
		List<String> node_names = null;

		//overwrite
		alpha = 0.01;
		indep_test = IndependenceTest.mv_fisherz;
		stable = true;
		uc_rule = 0;
		uc_priority = null;
		mvpc = true;

		org.processmining.plugins.inductiveVisualMiner.causal.mvpc.CausalGraph result = MVPC.pc(data, alpha, indep_test,
				stable, uc_rule, uc_priority, mvpc, correction_name, background_knowledge, verbose, show_progress,
				node_names);

		result.to_nx_graph();
		return result.nx_graph;
	}
}