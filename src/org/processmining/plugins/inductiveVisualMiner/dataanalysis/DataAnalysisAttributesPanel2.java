package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData.Field;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysis.AttributeData.FieldType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.Attribute;
import org.processmining.plugins.inductiveVisualMiner.ivmfilter.AttributesInfo;

public class DataAnalysisAttributesPanel2 extends JTable {

	private static final long serialVersionUID = -991411001730872783L;
	private AttributesInfo attributesInfo;
	private DataAnalysis dataAnalysis;
	private AbstractTableModel model;

	private static final int rowHeightText = 22;
	private static final int rowHeightImage = CorrelationDensityPlot.getHeight();
	private static final int rowMargin = 3;
	private static final int rowMarginAttribute = 20;
	private static final int columnMargin = 5;

	private static final int headerRows = 2;

	private static final int fields = Field.values().length;

	public DataAnalysisAttributesPanel2() {
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
							return "n/a";
						}
					} else if (column == 3) {
						BufferedImage im = dataAnalysis.getLogDataNegative().pieSize;
						int numberOfTraces = dataAnalysis.getLogDataNegative().numberOfTraces;
						if (im != null) {
							return Pair.of(numberOfTraces, new ImageIcon(im));
						} else {
							return "n/a";
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
						return "n/a";
					}
				} else if (field.type() == FieldType.number) {
					if (data.getNumber(field) > -Double.MAX_VALUE) {
						return DataAnalysis.getString(attribute, field, data.getNumber(field));
					} else {
						return "n/a";
					}
				}
				return "";
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		setModel(model);
		setDefaultRenderer(Object.class, new Renderer());
		setOpaque(false);
		setShowGrid(false);
		setRowMargin(rowMargin);
		getColumnModel().setColumnMargin(columnMargin);
	}

	public void setAttributesInfo(AttributesInfo attributesInfo) {
		this.attributesInfo = attributesInfo;
	}

	public void setDataAnalysis(DataAnalysis dataAnalysis) {
		this.dataAnalysis = dataAnalysis;
		setRowHeights();
		model.fireTableStructureChanged();
	}

	public int getRows() {
		if (dataAnalysis == null) {
			return 0;
		}
		return headerRows + //header rows
				dataAnalysis.getTraceAttributes().size() * DataAnalysis.AttributeData.Field.values().length;
	}

	public void setRowHeights() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setRowHeight(0, rowHeightText + rowMarginAttribute);
				setRowHeight(1, DataAnalysis.pieSize + 5);
				for (int row = headerRows; row < getRowCount(); row++) {
					int height;
					if (isImage(row)) {
						height = rowHeightImage;
					} else {
						height = rowHeightText;
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

	public class Renderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = -7148998664457522071L;

		public final DecimalFormat numberFormat = new DecimalFormat("0.0000");

		public Renderer() {
			IvMDecorator.decorate(this);
			setHorizontalTextPosition(SwingConstants.LEADING);
			setVerticalAlignment(JLabel.TOP);
		}

		public Component getTableCellRendererComponent(JTable table, Object object, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (object == null) {
				setText("");
				setIcon(null);
			} else if (object instanceof ImageIcon) {
				setText("");
				setIcon((ImageIcon) object);
			} else if (object instanceof Pair<?, ?>) {
				@SuppressWarnings("unchecked")
				Pair<Integer, ImageIcon> p = (Pair<Integer, ImageIcon>) object;
				setText(p.getA() + " ");
				setIcon(p.getB());
			} else {
				setText(object.toString());
				setIcon(null);
			}

			if (column == 2 || column == 3) {
				setHorizontalAlignment(JLabel.RIGHT);
				setFont(IvMDecorator.fontMonoSpace);
			} else {
				setHorizontalAlignment(JLabel.LEFT);
				setFont(IvMDecorator.fontLarger);
			}

			return this;
		}
	}
}
