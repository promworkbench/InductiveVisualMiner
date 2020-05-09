package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType.Type;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis.Field;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class TraceAttributeAnalysisTable extends DataAnalysisTable {

	private static final long serialVersionUID = -991411001730872783L;

	private TraceAttributeAnalysis dataAnalysis;
	private AbstractTableModel model;
	private int[] startRowOfField;

	private static final int rowHeightImage = CorrelationDensityPlot.getHeight();
	private static final int rowMarginAttribute = 20;

	private static final int headerRows = 2;

	public TraceAttributeAnalysisTable() {
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 5459459044383246441L;

			public String getColumnName(int col) {
				switch (col) {
					case 0 :
						return "Attribute";
					case 1 :
						return "";
					case 2 :
						if (dataAnalysis != null && dataAnalysis.isSomethingFiltered()) {
							return "highlighted traces";
						} else {
							return "full log";
						}
					case 3 :
						return "not-highlighted traces";
				}
				return "";
			}

			public int getColumnCount() {
				return 1 //attribute name
						+ 2 //property + value
						+ (dataAnalysis != null && dataAnalysis.isSomethingFiltered() ? 1 : 0); //not-highlighted value
			}

			public int getRowCount() {
				if (dataAnalysis == null) {
					return 0;
				}
				return startRowOfField[startRowOfField.length - 1];
			}

			@Override
			public Object getValueAt(int row, int column) {
				if (dataAnalysis == null) {
					return null;
				}
				Attribute attribute = getAttribute(row);
				int nrInAttribute = getNrInAttribute(row);
				Field field = getField(attribute, nrInAttribute);

				if (column == 0) {
					if (nrInAttribute == 0) {
						return attribute.getName();
					} else {
						return "";
					}
				} else if (column == 1) {
					return field.toString();
				} else if (column == 2) {
					EnumMap<Field, DisplayType> data = dataAnalysis.getAttributeData(attribute);
					return getValue(attribute, field, data);
				} else if (column == 3) {
					EnumMap<Field, DisplayType> data = dataAnalysis.getAttributeDataNegative(attribute);
					return getValue(attribute, field, data);
				}
				return "";
			}

			public Object getValueAt2(int row, int column) {
				Attribute attribute = getAttribute(row);
				int fieldNr = getNrInAttribute(row);
				Field field = getField(row);

				//header rows
				if (row == 0) {
					if (dataAnalysis.isSomethingFiltered()) {
						if (column == 0) {
							return "stochastic similarity";
						} else if (column == 1) {
							return DisplayType.numeric(dataAnalysis.getStochasticSimilarity());
						}
					}
					return "";
				} else if (row == 1) {
					if (column == 0) {
						return "number of traces";
					} else if (column == 1) {
						return "";
					} else if (column == 2) {
						BufferedImage im = dataAnalysis.getLogData().pieSize;
						int numberOfTraces = dataAnalysis.getLogData().numberOfTraces;
						if (im != null) {
							return Pair.of(numberOfTraces, new ImageIcon(im));
						} else {
							return DisplayType.NA();
						}
					} else if (column == 3) {
						BufferedImage im = dataAnalysis.getLogDataNegative().pieSize;
						int numberOfTraces = dataAnalysis.getLogDataNegative().numberOfTraces;
						if (im != null) {
							return Pair.of(numberOfTraces, new ImageIcon(im));
						} else {
							return DisplayType.NA();
						}

					}

				}

				//body rows
				if (column == 0) {
					if (fieldNr == 0) {
						return attribute.getName();
					} else {
						return "";
					}
				} else if (column == 1) {
					return field.toString();
				} else if (column == 2) {
					EnumMap<Field, DisplayType> data = dataAnalysis.getAttributeData(attribute);
					return getValue(attribute, field, data);
				} else if (column == 3) {
					EnumMap<Field, DisplayType> data = dataAnalysis.getAttributeDataNegative(attribute);
					return getValue(attribute, field, data);
				}
				return "";
			}

			private DisplayType getValue(Attribute attribute, Field field, EnumMap<Field, DisplayType> data) {
				if (data.get(field) != null) {
					return data.get(field);
				} else {
					return DisplayType.NA();
				}
			}
		};

		setModel(model);
	}

	public boolean setData(InductiveVisualMinerState state) {
		dataAnalysis = state.getTraceAttributesAnalysis();

		if (dataAnalysis == null) {
			return false;
		}

		//count the row starts
		startRowOfField = new int[dataAnalysis.getTraceAttributes().size() + 1];
		int i = 0;
		int row = 0;
		for (Attribute attribute : dataAnalysis.getTraceAttributes()) {
			EnumMap<?, ?> map = dataAnalysis.getAttributeData(attribute);
			startRowOfField[i] = row;
			i++;
			row += map.size();
		}
		startRowOfField[i] = row;

		setRowHeights();

		model.fireTableStructureChanged();
		return true;
	}

	public void invalidateData() {
		dataAnalysis = null;
		model.fireTableStructureChanged();
	}

	public void setRowHeights() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setRowHeight(getRowHeight());

				for (int row = 0; row < startRowOfField[startRowOfField.length - 1]; row++) {
					for (int column = 0; column < model.getColumnCount(); column++) {
						Object value = model.getValueAt(row, column);
						if (value instanceof DisplayType && ((DisplayType) value).getType() == Type.image
								&& ((DisplayType.Image) value).getImage() != null) {
							setRowHeight(row, rowHeightImage);
						}
					}
				}
			}
		});
	}

	private Attribute getAttribute(int rowNr) {
		if (dataAnalysis == null) {
			return null;
		}
		int index = Arrays.binarySearch(startRowOfField, rowNr);
		if (index < 0) {
			index = (~index) - 1;
		}

		for (Attribute attribute : dataAnalysis.getTraceAttributes()) {
			if (index == 0) {
				return attribute;
			}
			index--;
		}
		return null;
	}

	private int getNrInAttribute(int rowNr) {
		if (dataAnalysis == null) {
			return Integer.MIN_VALUE;
		}
		int index = Arrays.binarySearch(startRowOfField, rowNr);
		if (index < 0) {
			index = (~index - 1);
		}

		return rowNr - startRowOfField[index];
	}

	private Field getField(Attribute attribute, int nrInAttribute) {
		if (dataAnalysis == null) {
			return null;
		}
		EnumMap<Field, ?> map = dataAnalysis.getAttributeData(attribute);

		Iterator<Field> it = map.keySet().iterator();
		Field result = it.next();
		while (nrInAttribute > 0) {
			result = it.next();
			nrInAttribute--;
		}
		return result;
	}

	private Field getField(int rowNr) {
		if (rowNr < headerRows) {
			return null;
		}
		int fieldNr = getNrInAttribute(rowNr);
		return Field.values()[fieldNr];
	}
}