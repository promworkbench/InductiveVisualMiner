package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.table.TableModel;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.graphviz.visualisation.NavigableSVGPanel;
import org.processmining.plugins.graphviz.visualisation.export.Exporter;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTableFactory;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExporterDataAnalyses extends Exporter {

	private InductiveVisualMinerState state;

	public ExporterDataAnalyses(InductiveVisualMinerState state) {
		this.state = state;
	}

	@Override
	public String getDescription() {
		return "data analyses (xlsx)";
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
			if (!analysisTable.setData(state)) {
				sheet.addCell(new Label(0, 0, "Still computing at time of export."));
			} else {
				TableModel model = analysisTable.getModel();

				//write header
				for (int column = 0; column < model.getColumnCount(); column++) {
					sheet.addCell(new Label(column, 0, model.getColumnName(column)));
				}

				//write body
				for (int column = 0; column < model.getColumnCount(); column++) {
					for (int row = 0; row < model.getRowCount(); row++) {
						Object value = model.getValueAt(row, column);
						write(sheet, column, row + 1, value);
					}
				}
			}

			sheetIndex++;
		}

		workbook.write();
		workbook.close();
	}

	private void write(WritableSheet sheet, int column, int row, Object value)
			throws RowsExceededException, WriteException {
		if (value == null) {
		} else if (value instanceof ImageIcon) {
			sheet.addCell(new Label(column, row, "[image not exported]"));
		} else if (value instanceof Pair<?, ?>) {
			@SuppressWarnings("unchecked")
			Pair<Integer, ImageIcon> p = (Pair<Integer, ImageIcon>) value;
			sheet.addCell(new jxl.write.Number(column, row, p.getA()));
		} else if (value instanceof DisplayType) {
			switch (((DisplayType) value).getType()) {
				case NA :
					return;
				case duration :
				case numeric :
					sheet.addCell(new jxl.write.Number(column, row, ((DisplayType) value).getValue()));
					return;
				case literal :
					sheet.addCell(new Label(column, row, value.toString()));
					return;
				case time :
					sheet.addCell(
							new jxl.write.DateTime(column, row, new Date(((DisplayType.Time) value).getValueLong())));
					return;
				case html :
					sheet.addCell(new Label(column, row, ((DisplayType.HTML) value).getRawValue()));
					return;
				default :
					break;
			}
		} else {
			sheet.addCell(new Label(column, row, value.toString()));
		}
	}
}
