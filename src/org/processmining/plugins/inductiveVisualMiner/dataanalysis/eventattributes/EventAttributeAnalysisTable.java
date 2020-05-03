package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis.Field;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class EventAttributeAnalysisTable extends DataAnalysisTable<EventAttributeAnalysis> {

	private static final long serialVersionUID = 4494120805010679270L;

	private EventAttributeAnalysis dataAnalysis;
	private AbstractTableModel model;
	private int[] startRowOfField;

	public EventAttributeAnalysisTable() {
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 729361192982206380L;

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
					default :
						return "not-highlighted traces";
				}
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

			private Object getValue(Attribute attribute, Field field, EnumMap<Field, DisplayType> data) {
				if (data.get(field) != null) {
					return data.get(field);
				} else {
					return DisplayType.NA();
				}
			}

		};
		setModel(model);
	}

	public void setAttributesInfo(AttributesInfo attributesInfo) {

	}

	public void setData(EventAttributeAnalysis dataAnalysis) {
		this.dataAnalysis = dataAnalysis;

		startRowOfField = new int[dataAnalysis.getEventAttributes().size() + 1];
		int i = 0;
		int row = 0;
		for (Attribute attribute : dataAnalysis.getEventAttributes()) {
			EnumMap<Field, DisplayType> map = dataAnalysis.getAttributeData(attribute);
			startRowOfField[i] = row;
			i++;
			row += map.size();
		}
		startRowOfField[i] = row;

		model.fireTableStructureChanged();
	}

	private Attribute getAttribute(int rowNr) {
		if (dataAnalysis == null) {
			return null;
		}
		int index = Arrays.binarySearch(startRowOfField, rowNr);
		if (index < 0) {
			index = (~index) - 1;
		}

		for (Attribute attribute : dataAnalysis.getEventAttributes()) {
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
		EnumMap<Field, DisplayType> map = dataAnalysis.getAttributeData(attribute);

		Iterator<Field> it = map.keySet().iterator();
		Field result = it.next();
		while (nrInAttribute > 0) {
			result = it.next();
			nrInAttribute--;
		}
		return result;
	}
}