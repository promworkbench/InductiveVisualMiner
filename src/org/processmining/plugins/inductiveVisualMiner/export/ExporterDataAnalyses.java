package org.processmining.plugins.inductiveVisualMiner.export;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerAnimationPanel;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObject;
import org.processmining.plugins.inductiveVisualMiner.chain.IvMObjectValues;
import org.processmining.plugins.inductiveVisualMiner.configuration.InductiveVisualMinerConfiguration;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysesView;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlock;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRowBlockComputer;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTab;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataTableModel;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;

import gnu.trove.set.hash.THashSet;
import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableImage;
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
		for (DataTab<?, ?, ?> dataTab : configuration.getDataAnalysisTables()) {
			result.addAll(createNonTriggerObjectsForDataTab(dataTab));
		}
		IvMObject<?>[] arr = new IvMObject<?>[result.size()];
		return result.toArray(arr);
	}

	private <O, C, P> Set<IvMObject<?>> createNonTriggerObjectsForDataTab(DataTab<O, C, P> dataTab) {
		Set<IvMObject<?>> result = new THashSet<>();

		DataTable<O, C, P> table = dataTab.createTable(null);
		for (DataRowBlock<O, C, P> rowBlock : dataTab.createRowBlocks(table)) {
			result.addAll(Arrays.asList(rowBlock.getOptionalObjects()));
			result.addAll(Arrays.asList(rowBlock.getRequiredObjects()));
		}
		for (DataRowBlockComputer<O, C, P> rowBlockComputer : dataTab.createRowBlockComputers()) {
			result.add(rowBlockComputer.getOutputObject());
		}

		return result;
	}

	public <O> void createBlocks(Set<IvMObject<?>> result,
			DataTab<O, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysis) {
		DataTable<O, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> table = analysis.createTable(null);
		for (DataRowBlock<O, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> block : analysis
				.createRowBlocks(table)) {
			result.addAll(Arrays.asList(block.getOptionalObjects()));
		}
	}

	@Override
	public void export(IvMObjectValues inputs, InductiveVisualMinerAnimationPanel panel, File file) throws Exception {
		WritableWorkbook workbook = Workbook.createWorkbook(file);

		int sheetIndex = 0;
		for (DataTab<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysis : configuration
				.getDataAnalysisTables()) {

			String name = analysis.getAnalysisName();
			WritableSheet sheet = workbook.createSheet(name, sheetIndex);

			//initialise the table
			DataTable<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysisTable = createTable(
					inputs, analysis);

			DataTableModel<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> model = analysisTable
					.getModel();

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

			//resize name columns
			for (int column = 0; column < model.getNumberOfNameColumns(); column++) {
				resizeColumn(sheet, column);
			}

			sheetIndex++;
		}

		workbook.write();
		workbook.close();
	}

	public <O> DataTable<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> createTable(
			IvMObjectValues inputs, DataTab<O, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysis)
			throws Exception {
		DataTable<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> analysisTable = DataAnalysesView
				.createAndFillTable(analysis, null);
		for (DataRowBlock<?, InductiveVisualMinerConfiguration, InductiveVisualMinerPanel> block : analysisTable
				.getModel().getBlocks()) {
			if (inputs.has(block.getRequiredObjects())) {
				IvMObjectValues subInputs = inputs.getIfPresent(block.getRequiredObjects(), block.getOptionalObjects());
				block.updateGui(null, subInputs);
			}
		}
		return analysisTable;
	}

	private void write(WritableSheet sheet, int column, int row, Object value)
			throws RowsExceededException, WriteException, IOException {
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
					BufferedImage image = ((DisplayType.Image) value).getImage();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ImageIO.write(image, "PNG", baos);

					//black magic coming:
					double CELL_DEFAULT_HEIGHT_PIXELS = 17;
					double CELL_DEFAULT_HEIGHT_UNITS = 255;
					double IMAGE_HEIGHT_CELLS = image.getHeight() / CELL_DEFAULT_HEIGHT_PIXELS;
					double CELL_HEIGHT_UNITS = CELL_DEFAULT_HEIGHT_UNITS * IMAGE_HEIGHT_CELLS;
					sheet.setRowView(row, (int) CELL_HEIGHT_UNITS);

					double CELL_DEFAULT_WIDTH = 64;
					double CELL_DEFAULT_WIDTH_UNITS = 8;
					double IMAGE_WIDTH_CELLS = image.getWidth() / CELL_DEFAULT_WIDTH;
					double CELL_WIDTH_UNITS = CELL_DEFAULT_WIDTH_UNITS * IMAGE_WIDTH_CELLS;
					sheet.setColumnView(column, (int) CELL_WIDTH_UNITS);
					//int heightInPoints = 20 * 20; // 1/20pt units
					//int widthInPoints = 2 * 4;
					//sheet.setColumnView(column, (int) CELL_DEFAULT_WIDTH);
					//CellView columnView = sheet.getColumnView(row);
					//columnView.setSize(4 * 256); //256 units per character width
					//sheet.setColumnView(column, columnView);

					//sheet.setRowView(row, 4 * 20);

					sheet.addImage(new WritableImage(column, row, 1, 1, baos.toByteArray()));

					//sheet.addImage(new WritableImage(column, row, 1, 1, baos.toByteArray()));
					//sheet.addCell(new Label(column, row, "[image]"));
					return;
				default :
					break;
			}
		} else {
			sheet.addCell(new Label(column, row, value.toString()));
		}
	}

	/**
	 * Derived from:
	 * https://stackoverflow.com/questions/1665391/jxl-cell-formatting
	 * 
	 * @param sheet
	 */
	private static void resizeColumn(WritableSheet sheet, int column) {
		Cell[] cells = sheet.getColumn(column);
		int longestStrLen = -1;

		if (cells.length == 0) {
			return;
		}

		/* Find the widest cell in the column. */
		for (int j = 0; j < cells.length; j++) {
			if (cells[j].getContents().length() > longestStrLen) {
				String str = cells[j].getContents();
				if (str == null || str.isEmpty()) {
					continue;
				}
				longestStrLen = str.trim().length();
			}
		}

		/* If not found, skip the column. */
		if (longestStrLen == -1) {
			return;
		}

		/* If wider than the max width, crop width */
		if (longestStrLen > 255) {
			longestStrLen = 255;
		}

		CellView cv = sheet.getColumnView(column);
		cv.setSize(longestStrLen * 256
				+ 100); /*
						 * Every character is 256 units wide, so scale it.
						 */
		sheet.setColumnView(column, cv);
	}
}
