package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;

public class ColouringFilterPluginManager {

	public static ColouringFilter[] getAllMetricInfos(PluginContext context, JComponent parent, XLog xLog, Runnable onUpdate) {
		List<ColouringFilter> knownInfos = new LinkedList<ColouringFilter>();
		ColouringFilter[] knownInfosArray;

//		Set<Class<?>> coverageEstimatorClasses = context.getPluginManager().getKnownClassesAnnotatedWith(
//				IvMColouringFilterPlugin.class);
//		if (coverageEstimatorClasses != null) {
//			for (Class<?> coverClass : coverageEstimatorClasses) {
//				try {
//					Constructor<?> constructor = coverClass.getConstructor(JComponent.class, XLog.class);
//					Object xyz = constructor.newInstance(parent, xLog);
//
//					if (xyz instanceof ColouringFilter) {
//						knownInfos.add((ColouringFilter) xyz);
//					}
//				} catch (Exception e) {
//					//Catch and ignore all exceptions to be resistant to external faults. 
//					e.printStackTrace();
//				}
//			}
//		}
//
//		knownInfosArray = new ColouringFilter[knownInfos.size()];
//		knownInfos.toArray(knownInfosArray);
//		
//		System.out.println(knownInfosArray);
		
		 TraceAttributeFilter f = new TraceAttributeFilter(parent, xLog);
		 f.setOnUpdate(onUpdate);
		 knownInfosArray = new ColouringFilter[]{f};

		return knownInfosArray;
	}

}
