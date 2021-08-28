package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.Arrays;

public class DataRow {
	private String[] names;
	private DisplayType[] values;

	public DataRow(DisplayType value, String... names) {
		this.names = names;
		this.values = new DisplayType[] { value };
	}

	public DataRow(String name1, String name2, DisplayType... values) {
		this.names = new String[] { name1, name2 };
		this.values = values;
	}

	public DataRow(String name, DisplayType... values) {
		this.names = new String[] { name };
		this.values = values;
	}

	public int getNumberOfNames() {
		return names.length;
	}

	public String getName(int index) {
		return names[index];
	}

	public String[] getNames() {
		return names;
	}

	public void setNames(String... names) {
		this.names = names;
	}

	public int getNumberOfValues() {
		return values.length;
	}

	public DisplayType getValue(int index) {
		return values[index];
	}

	public DisplayType[] getValues() {
		return values;
	}

	public void setValues(DisplayType... value) {
		this.values = value;
	}

	public void setAllValues(DisplayType value) {
		Arrays.fill(values, value);
	}

}