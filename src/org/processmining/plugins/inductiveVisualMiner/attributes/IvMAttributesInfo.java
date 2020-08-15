package org.processmining.plugins.inductiveVisualMiner.attributes;

import java.util.Collection;
import java.util.TreeSet;

import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLog;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveminer2.attributes.Attribute;
import org.processmining.plugins.inductiveminer2.attributes.AttributeImpl;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtual;
import org.processmining.plugins.inductiveminer2.attributes.AttributesInfo;

import gnu.trove.map.hash.THashMap;

public class IvMAttributesInfo implements AttributesInfo {

	private final THashMap<String, Attribute> traceAttributes;
	private final THashMap<String, Attribute> eventAttributes;

	public IvMAttributesInfo(IvMLog log, AttributesInfo oldAttributes, IvMVirtualAttributeFactory factory) {
		//copy old attributes
		{
			traceAttributes = new THashMap<>();
			for (Attribute attribute : oldAttributes.getTraceAttributes()) {
				traceAttributes.put(attribute.getName(), attribute);
			}

			eventAttributes = new THashMap<>();
			for (Attribute attribute : oldAttributes.getEventAttributes()) {
				eventAttributes.put(attribute.getName(), attribute);
			}
		}

		//filter the real attributes
		THashMap<String, AttributeImpl> traceAttributesReal = new THashMap<>();
		THashMap<String, AttributeImpl> eventAttributesReal = new THashMap<>();
		{
			for (Attribute attribute : traceAttributes.values()) {
				if (attribute instanceof AttributeImpl) {
					traceAttributesReal.put(attribute.getName(), (AttributeImpl) attribute);
				}
			}
			for (Attribute attribute : eventAttributes.values()) {
				if (attribute instanceof AttributeImpl) {
					eventAttributesReal.put(attribute.getName(), (AttributeImpl) attribute);
				}
			}
		}

		//add the IvM attributes
		THashMap<String, AttributeVirtual> traceAttributesVirtual = new THashMap<>();
		THashMap<String, AttributeVirtual> eventAttributesVirtual = new THashMap<>();
		{
			for (AttributeVirtual attribute : factory.createVirtualIvMTraceAttributes(traceAttributesReal,
					eventAttributesReal)) {
				traceAttributesVirtual.put(attribute.getName(), attribute);
			}
			for (AttributeVirtual attribute : factory.createVirtualIvMEventAttributes(traceAttributesReal,
					eventAttributesReal)) {
				eventAttributesVirtual.put(attribute.getName(), attribute);
			}
		}

		//initialise the IvM attributes
		{
			for (IvMTrace trace : log) {
				for (AttributeVirtual traceAttribute : traceAttributesVirtual.values()) {
					traceAttribute.add(trace);
				}

				for (IvMMove event : trace) {
					for (AttributeVirtual eventAttribute : eventAttributesVirtual.values()) {
						eventAttribute.add(event);
					}
				}
			}
		}

		//copy the IvM attributes to the final result
		{
			traceAttributes.putAll(traceAttributesVirtual);
			eventAttributes.putAll(eventAttributesVirtual);
		}
	}

	@Override
	public Collection<Attribute> getEventAttributes() {
		return eventAttributes.values();
	}

	@Override
	public Attribute getEventAttributeValues(String attribute) {
		return eventAttributes.get(attribute);
	}

	@Override
	public Collection<Attribute> getTraceAttributes() {
		return new TreeSet<>(traceAttributes.values());
	}

	@Override
	public Attribute getTraceAttributeValues(String attribute) {
		return traceAttributes.get(attribute);
	}
}