package org.processmining.plugins.inductiveVisualMiner.dataanalysis.logattributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataAnalysisTable;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

public class LogAttributeAnalysisTable extends DataAnalysisTable<XLog> {

	private static final long serialVersionUID = 192566312464894607L;

	private AbstractTableModel model;
	private List<XAttribute> attributes;

	public LogAttributeAnalysisTable() {
		//fill the table
		model = new AbstractTableModel() {

			private static final long serialVersionUID = 5457412338454004753L;

			public int getColumnCount() {
				return 2;
			}

			public String getColumnName(int column) {
				switch (column) {
					case 0 :
						return "Attribute";
					default :
						return "value";
				}
			}

			public int getRowCount() {
				if (attributes == null) {
					return 0;
				}
				return attributes.size();
			}

			public Object getValueAt(int row, int column) {
				if (attributes == null) {
					return "";
				}

				switch (column) {
					case 0 :
						//attribute name
						return attributes.get(row).getKey();
					default :
						//attribute value
						return attributes.get(row).toString();
				}
			}

		};

		setModel(model);
	}

	public void setAttributesInfo(AttributesInfo attributesInfo) {

	}

	public void setData(XLog log) {
		attributes = new ArrayList<>(log.getAttributes().values());
		Collections.sort(attributes, new Comparator<XAttribute>() {

			public int compare(XAttribute o1, XAttribute o2) {
				return o1.getKey().toLowerCase().compareTo(o2.getKey().toLowerCase());
			}
		});
		model.fireTableStructureChanged();
	}

}