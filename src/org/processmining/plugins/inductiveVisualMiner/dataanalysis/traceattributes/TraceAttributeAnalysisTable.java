package org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis.AttributeData;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis.AttributeData.Field;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.traceattributes.TraceAttributeAnalysis.AttributeData.FieldType;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class TraceAttributeAnalysisTable extends DataAnalysisTable<TraceAttributeAnalysis> {

	private static final long serialVersionUID = -991411001730872783L;
	private AttributesInfo attributesInfo;
	private TraceAttributeAnalysis dataAnalysis;
	private AbstractTableModel model;

	private static final int rowHeightImage = CorrelationDensityPlot.getHeight();
	private static final int rowMarginAttribute = 20;

	private static final int headerRows = 2;

	private static final int fields = Field.values().length;

	public TraceAttributeAnalysisTable() {
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 5459459044383246441L;

			public String getColumnName(int col) {
				switch (col) {
					case 0 :
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

			public int getRowCount() {
				return getRows();
			}

			public int getColumnCount() {
				return 1 //attribute name
						+ 2 //property + value
						+ (dataAnalysis != null && dataAnalysis.isSomethingFiltered() ? 1 : 0); //property + value
			}

			@Override
			public Object getValueAt(int row, int column) {
				Attribute attribute = getAttribute(row);
				int fieldNr = getNrInAttribute(row);
				Field field = getField(row);

				//header rows
				if (row == 0) {
					if (dataAnalysis.isSomethingFiltered()) {
						if (column == 0) {
							return "stochastic similarity";
						} else if (column == 1) {
							return dataAnalysis.getStochasticSimilarity();
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
					AttributeData data = dataAnalysis.getAttributeData(attribute);
					return getValue(attribute, field, data);
				} else if (column == 3) {
					AttributeData data = dataAnalysis.getAttributeDataNegative(attribute);
					return getValue(attribute, field, data);
				}
				return "";
			}

			private Object getValue(Attribute attribute, Field field, AttributeData data) {
				if (field.type() == FieldType.image) {
					BufferedImage im = data.getImage(field);
					if (im != null) {
						return new ImageIcon(im);
					} else {
						return DisplayType.NA();
					}
				} else if (field.type() == FieldType.value) {
					if (data.getValue(field) != null) {
						return data.getValue(field);
					} else {
						return DisplayType.NA();
					}
				}
				return "";
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		setModel(model);
	}

	public void setAttributesInfo(AttributesInfo attributesInfo) {
		this.attributesInfo = attributesInfo;
	}

	public void setData(TraceAttributeAnalysis dataAnalysis) {
		this.dataAnalysis = dataAnalysis;
		setRowHeights();
		model.fireTableStructureChanged();
	}

	public int getRows() {
		if (dataAnalysis == null) {
			return 0;
		}
		return headerRows + //header rows
				dataAnalysis.getTraceAttributes().size() * TraceAttributeAnalysis.AttributeData.Field.values().length;
	}

	public void setRowHeights() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setRowHeight(0, rowHeight + rowMarginAttribute);
				setRowHeight(1, TraceAttributeAnalysis.pieSize + 5);
				for (int row = headerRows; row < getRowCount(); row++) {
					int height;
					if (isImage(row)) {
						height = rowHeightImage;
					} else {
						height = rowHeight;
					}

					//add a bit of margin to the last row of each attribute
					if (getNrInAttribute(row) == fields - 1) {
						height += rowMarginAttribute;
					}
					setRowHeight(row, height);
				}
			}
		});

	}

	private Attribute getAttribute(int rowNr) {
		rowNr -= headerRows;
		if (attributesInfo == null) {
			return null;
		}
		int attributeNr = rowNr / fields;
		for (Attribute attribute : dataAnalysis.getTraceAttributes()) {
			if (attributeNr == 0) {
				return attribute;
			}
			attributeNr--;
		}
		return null;
	}

	private int getNrInAttribute(int rowNr) {
		rowNr -= headerRows;
		return rowNr % fields;
	}

	private boolean isImage(int rowNr) {
		if (rowNr == 0) {
			return true;
		}
		if (rowNr == 1) {
			return false;
		}
		return getField(rowNr).type() == FieldType.image;
	}

	private Field getField(int rowNr) {
		if (rowNr < headerRows) {
			return null;
		}
		int fieldNr = getNrInAttribute(rowNr);
		return Field.values()[fieldNr];
	}
}