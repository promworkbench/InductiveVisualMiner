package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.processmining.framework.plugin.PluginContext;

public class IvMFiltersFinder {

	@SafeVarargs
	public static List<IvMFilter> findFilteringPlugins(PluginContext context,
			Class<? extends IvMFilter> clazz, Class<? extends Annotation>... annotations) {
		List<IvMFilter> colouringFilters = new ArrayList<>();

		for (Class<? extends Annotation> annotation : annotations) {
			Set<Class<?>> coverageEstimatorClasses = context.getPluginManager()
					.getKnownClassesAnnotatedWith(annotation);
			if (coverageEstimatorClasses != null) {
				for (Class<?> coverClass : coverageEstimatorClasses) {
					try {
						Constructor<?> constructor = coverClass.getConstructor();
						Object xyz = constructor.newInstance();

						if (clazz.isInstance(xyz)) {
							colouringFilters.add((IvMFilter) xyz);
						}
					} catch (Exception e) {
						//Catch and ignore all exceptions to be resistant to external faults. 
						//e.printStackTrace();
					}
				}
			}

		}

		return colouringFilters;
	}

}
