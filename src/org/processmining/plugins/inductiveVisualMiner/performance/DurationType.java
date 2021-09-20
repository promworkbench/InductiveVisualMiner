package org.processmining.plugins.inductiveVisualMiner.performance;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.performance.PerformanceLevel.Level;

/**
 * This enum takes the heavy lifting out of time computations by providing
 * methods to extract durations from instances.
 * 
 * @author sander
 *
 */
public enum DurationType {

	@PerformanceLevel(Level.activity)
	elapsed {
		public String toString() {
			return "elapsed time  ";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = traceFirstTimestamp;
			if (a == null || a.getLogTimestamp() == null) {
				return false;
			}

			return getB(instance) != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = traceFirstTimestamp;
			IvMMove b = getB(instance);
			return b.getLogTimestamp() - a.getLogTimestamp();
		}

		private IvMMove getB(Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance) {
			IvMMove b = instance.getC();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getD();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getE();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getF();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			return null;
		}
	},

	@PerformanceLevel({ Level.activity, Level.process })
	queueing {
		public String toString() {
			return "queueing time ";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getC();
			IvMMove b = instance.getD();
			return a != null && a.getLogTimestamp() != null & b != null && b.getLogTimestamp() != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getC();
			IvMMove b = instance.getD();
			return b.getLogTimestamp() - a.getLogTimestamp();
		}
	},

	@PerformanceLevel({ Level.activity, Level.process })
	waiting {
		public String toString() {
			return "waiting time  ";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getD();
			IvMMove b = instance.getE();
			return a != null && a.getLogTimestamp() != null & b != null && b.getLogTimestamp() != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getD();
			IvMMove b = instance.getE();
			return b.getLogTimestamp() - a.getLogTimestamp();
		}
	},

	@PerformanceLevel({ Level.activity, Level.process })
	service {
		public String toString() {
			return "service time  ";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getE();
			IvMMove b = instance.getF();
			return a != null && a.getLogTimestamp() != null & b != null && b.getLogTimestamp() != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getE();
			IvMMove b = instance.getF();
			return b.getLogTimestamp() - a.getLogTimestamp();
		}
	},

	/**
	 * Sojourn time on the process is confusing.
	 */
	@PerformanceLevel(Level.activity)
	sojourn {
		public String toString() {
			return "sojourn time  ";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getC();
			IvMMove b = instance.getF();
			return a != null && a.getLogTimestamp() != null & b != null && b.getLogTimestamp() != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = instance.getC();
			IvMMove b = instance.getF();
			return b.getLogTimestamp() - a.getLogTimestamp();
		}
	},

	@PerformanceLevel({ Level.activity })
	remaining {
		public String toString() {
			return "remaining time";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = traceLastTimestamp;
			if (a == null || a.getLogTimestamp() == null) {
				return false;
			}

			return getB(instance) != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			IvMMove a = getB(instance);
			IvMMove b = traceLastTimestamp;
			return b.getLogTimestamp() - a.getLogTimestamp();
		}

		private IvMMove getB(Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance) {
			IvMMove b = instance.getF();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getE();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getD();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			b = instance.getC();
			if (b != null && b.getLogTimestamp() != null) {
				return b;
			}

			return null;
		}
	},

	@PerformanceLevel(Level.process)
	traceDuration {

		public String toString() {
			return "trace duration";
		}

		public boolean applies(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			return traceFirstTimestamp != null && traceFirstTimestamp.getLogTimestamp() != null
					&& traceLastTimestamp != null && traceLastTimestamp.getLogTimestamp() != null;
		}

		public long getDistance(IvMMove traceFirstTimestamp,
				Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp) {
			return traceLastTimestamp.getLogTimestamp() - traceFirstTimestamp.getLogTimestamp();
		}
	};

	public abstract String toString();

	public abstract boolean applies(IvMMove traceFirstTimestamp,
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp);

	public abstract long getDistance(IvMMove traceFirstTimestamp,
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove traceLastTimestamp);

	// cache values
	static EnumMap<Level, DurationType[]> valuesAtCache = new EnumMap<>(Level.class);
	static {
		for (Level level : Level.values()) {
			List<DurationType> result = new ArrayList<>();
			for (DurationType durationType : DurationType.values()) {
				try {
					PerformanceLevel[] annotations = durationType.getClass().getField(durationType.name())
							.getAnnotationsByType(PerformanceLevel.class);
					for (PerformanceLevel annotation : annotations) {
						for (Level level2 : annotation.value()) {
							if (level == level2) {
								result.add(durationType);
							}
						}
					}
				} catch (NoSuchFieldException | SecurityException e) {
					e.printStackTrace();
				}
			}
			DurationType[] result2 = new DurationType[result.size()];
			valuesAtCache.put(level, result.toArray(result2));
		}
	}

	public static DurationType[] valuesAt(PerformanceLevel.Level level) {
		return valuesAtCache.get(level);
	}
}