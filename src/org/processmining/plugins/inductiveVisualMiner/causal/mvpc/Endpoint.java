package org.processmining.plugins.inductiveVisualMiner.causal.mvpc;

public enum Endpoint {
	TAIL {
		public int value() {
			return -1;
		}
	},
	NULL {
		public int value() {
			return 0;
		}
	},
	ARROW {
		public int value() {
			return 1;
		}
	},
	CIRCLE {
		public int value() {
			return 2;
		}
	},
	STAR {
		public int value() {
			return 3;
		}
	},
	TAIL_AND_ARROW {
		public int value() {
			return 4;
		}
	},
	ARROW_AND_ARROW {
		public int value() {
			return 5;
		}
	};

	public abstract int value();

	public static Endpoint get(int value) {
		for (Endpoint e : Endpoint.values()) {
			if (e.value() == value) {
				return e;
			}
		}
		return null;
	}
}
