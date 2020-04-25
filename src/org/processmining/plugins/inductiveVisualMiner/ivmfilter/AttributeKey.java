package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import org.processmining.plugins.inductiveminer2.attributes.Attribute;

public class AttributeKey {
	public enum Type {
		attribute, message
	}

	private final Type type;
	private final String message;
	private final Attribute attribute;

	public static AttributeKey attribute(Attribute attribute) {
		return new AttributeKey(Type.attribute, attribute, null);
	}

	public static AttributeKey message(String message) {
		return new AttributeKey(Type.message, null, message);
	}

	private AttributeKey(Type type, Attribute attribute, String message) {
		this.message = message;
		this.type = type;
		this.attribute = attribute;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public String toString() {
		if (getType() == Type.attribute) {
			return attribute.toString();
		}
		return message;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		return toString().equals(obj.toString());
	}

	public Type getType() {
		return type;
	}
}
