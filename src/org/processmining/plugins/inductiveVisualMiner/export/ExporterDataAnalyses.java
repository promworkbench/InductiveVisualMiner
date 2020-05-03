package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;

import javax.swing.table.TableModel;

import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class ExporterDataAnalyses extends Exporter {

	private final InductiveVisualMinerState state;
	private final InductiveVisualMinerPanel panel;

	public ExporterDataAnalyses(InductiveVisualMinerState state, InductiveVisualMinerPanel panel) {
		this.state = state;
		this.panel = panel;
	}

	@Override
	public String getDescription() {
		return "xlsx (data analyses)";
	}

	protected String getExtension() {
		return "xlsx";
	}

	public void export(NavigableSVGPanel panel, File file) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(file);

		int sheetIndex = 0;
		for (DataAnalysisTableFactory analysis : state.getConfiguration().getDataAnalysisTables()) {
			String name = analysis.getAnalysisName();
			WritableSheet sheet = workbook.createSheet(name, sheetIndex);

			DataAnalysisTable analysisTable = analysis.create();
			analysisTable.setData(state);
			TableModel model = analysisTable.getModel();

			//write header
			for (int column = 0; column < model.getColumnCount(); column++) {
				sheet.addCell(new Label(column, 0, model.getColumnName(column)));
			}

			sheetIndex++;
		}

		workbook.close();
	}
}
