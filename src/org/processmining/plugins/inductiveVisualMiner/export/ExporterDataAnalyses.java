package org.processmining.plugins.inductiveVisualMiner.export;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.table.TableModel;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

import gnu.trove.set.hash.THashSet;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class ExporterDataAnalyses extends IvMExporter {

	protected final InductiveVisualMinerConfiguration configuration;

	public ExporterDataAnalyses(InductiveVisualMinerConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public String getDescription() {
		return "xlsx (data analyses)";
	}

	@Override
	protected String getExtension() {
		return "xlsx";
	}

	@Override
	protected IvMObject<?>[] createInputObjects() {
		return new IvMObject<?>[] {};
	}

	@Override
	protected IvMObject<?>[] createNonTriggerObjects() {
		Set<IvMObject<?>> result = new THashSet<>();
		for (DataTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysis : configuration
				.getDataAnalysisTables()) {
			DataTable<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> table = analysis.createTable(null);
			for (DataRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> block : analysis
					.createRowBlocks(table)) {
				result.addAll(Arrays.asList(block.getOptionalObjects()));
			}
		}
		IvMObject<?>[] arr = new IvMObject<?>[result.size()];
		return result.toArray(arr);
	}

	@Override
	public void export(IvMObjectValues inputs, InductiveVisualMinerAnimationPanel panel, File file) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(file);

		int sheetIndex = 0;
		for (DataTab<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysis : configuration
				.getDataAnalysisTables()) {

			String name = analysis.getAnalysisName();
			WritableSheet sheet = workbook.createSheet(name, sheetIndex);

			//initialise the table
			DataTable<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysisTable = analysis
					.createTable(null);
			analysisTable.getModel().setBlocks(analysis.createRowBlocks(analysisTable));
			for (DataRowBlock<InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> block : analysisTable
					.getModel().getBlocks()) {
				if (inputs.has(block.getRequiredObjects())) {
					IvMObjectValues subInputs = inputs.getIfPresent(block.getRequiredObjects(),
							block.getOptionalObjects());
					block.updateGui(null, subInputs);
				}
			}

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
				case image :
					sheet.addCell(new Label(column, row, "[image not exported]"));
					return;
				default :
					break;
			}
		} else {
			sheet.addCell(new Label(column, row, value.toString()));
		}
	}

}
