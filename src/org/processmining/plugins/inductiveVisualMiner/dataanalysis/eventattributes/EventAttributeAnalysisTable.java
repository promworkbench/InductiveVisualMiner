package org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes;

import java.util.EnumMap;

import javax.swing.table.AbstractTableModel;

import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.eventattributes.EventAttributeAnalysis.Field;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class EventAttributeAnalysisTable extends DataAnalysisTable<EventAttributeAnalysis> {

	private static final long serialVersionUID = 4494120805010679270L;
	private static final int fields = Field.values().length;

	private EventAttributeAnalysis dataAnalysis;
	private AbstractTableModel model;

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
				int result = 0;
				for (Attribute attribute : dataAnalysis.getEventAttributes()) {

				}
				return dataAnalysis.getEventAttributes().size() * Field.values().length;
			}

			public Object getValueAt(int row, int column) {
				Attribute attribute = getAttribute(row);
				int fieldNr = getNrInAttribute(row);
				Field field = getField(row);

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
		model.fireTableStructureChanged();
	}

	private Attribute getAttribute(int rowNr) {
		if (dataAnalysis == null) {
			return null;
		}
		int attributeNr = rowNr / fields;
		for (Attribute attribute : dataAnalysis.getEventAttributes()) {
			if (attributeNr == 0) {
				return attribute;
			}
			attributeNr--;
		}
		return null;
	}

	private int getNrInAttribute(int rowNr) {
		return rowNr % fields;
	}

	private Field getField(int rowNr) {
		int fieldNr = getNrInAttribute(rowNr);
		return Field.values()[fieldNr];
	}
}