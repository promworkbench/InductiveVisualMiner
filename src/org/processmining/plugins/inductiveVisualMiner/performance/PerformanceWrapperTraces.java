package org.processmining.plugins.inductiveVisualMiner.performance;

import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceWrapper.TypeGlobal;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

public class PerformanceWrapperTraces {

	public static enum Type {
		queueing {
			public TypeGlobal toPerformance() {
				return TypeGlobal.queueing;
			}
		},
		waiting {
			public TypeGlobal toPerformance() {
				return TypeGlobal.waiting;
			}
		},
		service {
			public TypeGlobal toPerformance() {
				return TypeGlobal.service;
			}
		},
		sojourn {
			public TypeGlobal toPerformance() {
				return TypeGlobal.sojourn;
			}
		};

		public abstract TypeGlobal toPerformance();
	}

	private final TIntLongMap[] values = new TIntLongMap[Type.values().length];

	public PerformanceWrapperTraces() {
		for (Type type : Type.values()) {
			values[type.ordinal()] = new TIntLongHashMap(10, 0.5f, -1, 0);
		}
	}

	public void addValue(Type type, int trace, long value) {
		values[type.ordinal()].adjustOrPutValue(trace, value, value);
	}

	public void finalise(PerformanceWrapper result) {
		for (Type type : Type.values()) {
			for (long value : values[type.ordinal()].values()) {
				result.addGlobalValue(type.toPerformance(), value);
			}
		}
	}
}
