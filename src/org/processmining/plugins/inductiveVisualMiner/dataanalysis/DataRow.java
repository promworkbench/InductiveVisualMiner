package org.processmining.plugins.inductiveVisualMiner.dataanalysis;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

public class DataRow<O> {
	private String[] names;
	private DisplayType[] values;
	private O payload;

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

	public DataRow(String property, DataRow<O> base) {
		this.names = new String[base.names.length + 1];
		System.arraycopy(base.names, 0, this.names, 1, base.names.length);
		this.names[0] = property;
		this.values = Arrays.copyOf(base.values, base.values.length);
	}

	public DataRow(String name, O payload, DisplayType... values) {
		this.names = new String[] { name };
		this.payload = payload;
		this.values = values;
	}

	public DataRow(String prefixProperty, DataRow<O> copyFrom) {
		this.names = new String[copyFrom.names.length + 1];
		this.names[0] = prefixProperty;
		System.arraycopy(copyFrom.names, 0, this.names, 1, copyFrom.names.length);
		this.payload = copyFrom.payload;
		this.values = ArrayUtils.clone(copyFrom.values);
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

	public O getPayload() {
		return payload;
	}

	public void setPayload(O payload) {
		this.payload = payload;
	}

}