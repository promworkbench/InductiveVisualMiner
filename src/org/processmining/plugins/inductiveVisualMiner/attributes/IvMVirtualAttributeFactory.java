package org.processmining.plugins.inductiveVisualMiner.attributes;

import org.processmining.plugins.inductiveminer2.attributes.AttributeImpl;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtual;
import org.processmining.plugins.inductiveminer2.attributes.AttributeVirtualFactory;

import gnu.trove.map.hash.THashMap;

/**
 * There are two stages of adding virtual attributes: for ones that are stable
 * over XLog, IMLog and IvMLog (AttributeVirtualFactory) and the ones that are
 * only defined over IvMLog (here).
 * 
 * @author sander
 *
 */
public interface IvMVirtualAttributeFactory extends AttributeVirtualFactory {

	Iterable<AttributeVirtual> createVirtualIvMTraceAttributes(THashMap<String, AttributeImpl> traceAttributesReal,
			THashMap<String, AttributeImpl> eventAttributesReal);

	Iterable<AttributeVirtual> createVirtualIvMEventAttributes(THashMap<String, AttributeImpl> traceAttributesReal,
			THashMap<String, AttributeImpl> eventAttributesReal);
}