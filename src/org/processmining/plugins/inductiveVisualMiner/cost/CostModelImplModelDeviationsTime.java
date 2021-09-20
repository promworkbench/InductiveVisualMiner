package org.processmining.plugins.inductiveVisualMiner.cost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XAttribute;
import org.processmining.plugins.InductiveMiner.Pair;
import org.processmining.plugins.InductiveMiner.Sextuple;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DataRow;
import org.processmining.plugins.inductiveVisualMiner.dataanalysis.DisplayType;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMModel;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMLogInfo;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMMove;
import org.processmining.plugins.inductiveVisualMiner.ivmlog.IvMTrace;
import org.processmining.plugins.inductiveVisualMiner.performance.DurationType;
import org.processmining.plugins.inductiveminer2.attributes.AttributeUtils;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * Parameter structure: [0,n-1] log move cost on per activity that has at least
 * one log move; [n,n+m*x] per model node a few timing things cost.
 * 
 * @author sander
 *
 */
public class CostModelImplModelDeviationsTime extends CostModelAbstract {

	private final int numberOfParameters;
	private final int[] node2index;
	private final TObjectIntMap<String> logMove2index = new TObjectIntHashMap<>();

	public static enum ParameterNodeType {
		synchronousMove {
			public String toString() {
				return "step in log and model";
			}

			public double value2user(double value) {
				return value;
			};

			public DurationType durationType() {
				return null;
			}
		},
		modelMove {
			public String toString() {
				return "skip step in model";
			}

			public double value2user(double value) {
				return value;
			};

			public DurationType durationType() {
				return null;
			}
		},
		sojournTime {
			public String toString() {
				return "sojourn time (/hr)";
			}

			public double value2user(double value) {
				return value * ms2hr;
			};

			public DurationType durationType() {
				return DurationType.sojourn;
			}
		},
		waitingTime {
			public String toString() {
				return "waiting time (/hr)";
			}

			public double value2user(double value) {
				return value * ms2hr;
			};

			public DurationType durationType() {
				return DurationType.waiting;
			}
		},
		queueingTime {
			public String toString() {
				return "queueing time (/hr)";
			}

			public double value2user(double value) {
				return value * ms2hr;
			};

			public DurationType durationType() {
				return DurationType.queueing;
			}
		},
		serviceTime {
			public String toString() {
				return "service time (/hr)";
			}

			public double value2user(double value) {
				return value * ms2hr;
			};

			public DurationType durationType() {
				return DurationType.service;
			}
		};

		public abstract double value2user(double value);

		public abstract DurationType durationType();
	}

	public CostModelImplModelDeviationsTime(IvMModel model, IvMLogInfo logInfoFiltered) {
		super(model);

		int index = 0;
		for (String logMoveActivity : logInfoFiltered.getUnlabeledLogMoves()) {
			logMove2index.put(logMoveActivity, index);
			index += 1;
		}

		//count nodes in model
		int nodes = 0;
		{
			for (Iterator<Integer> it = model.getAllNodes().iterator(); it.hasNext();) {
				it.next();
				nodes++;
			}
		}

		node2index = new int[nodes];
		for (int node : model.getAllNodes()) {
			if (model.isActivity(node)) {
				node2index[node] = index;
				index += ParameterNodeType.values().length;
			} else {
				node2index[node] = -1;
			}
		}

		numberOfParameters = index;
	}

	@Override
	public String getName() {
		return "model, deviations & time";
	}

	@Override
	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	@Override
	public double[] getInputs(IvMMove startTrace,
			Sextuple<Integer, String, IvMMove, IvMMove, IvMMove, IvMMove> instance, IvMMove endTrace) {
		int node = instance.getA();
		IvMMove complete = instance.getF();

		double[] result = new double[numberOfParameters];

		if (complete != null && complete.isLogMove()) {
			//result[node2index[node] * 2] += 1;
			String activity = complete.getActivityEventClass().toString();
			result[logMove2index.get(activity)]++;
			return result;
		}

		if (node < 0 || !model.isActivity(node)) {
			return result;
		}

		if (complete.isModelMove()) {
			//model move
			result[node2index[node] + ParameterNodeType.modelMove.ordinal()]++;
		} else {
			//sync
			result[node2index[node] + ParameterNodeType.synchronousMove.ordinal()]++;
		}

		//timing parameters
		for (ParameterNodeType type : ParameterNodeType.values()) {
			if (type.durationType() != null && type.durationType().applies(startTrace, instance, endTrace)) {
				long value = type.durationType().getDistance(startTrace, instance, endTrace);
				result[node2index[node] + type.ordinal()] += value;
			}
		}

		return result;
	}

	/**
	 * Times/durations will be in ms.
	 * 
	 * @param node
	 * @param parameterType
	 * @return
	 */
	public double getNodeParameter(int node, ParameterNodeType parameterType) {
		if (node2index[node] >= 0) {
			return parameters[node2index[node] + parameterType.ordinal()];
		} else {
			return 0;
		}
	}

	@Override
	public double getCost(IvMTrace trace) {
		if (!trace.hasAttributes()) {
			return Double.NaN;
		}

		if (!trace.getAttributes().containsKey(attribute)) {
			return Double.NaN;
		}

		XAttribute att = trace.getAttributes().get(attribute);
		double value = AttributeUtils.parseDoubleFast(att);
		if (value == -Double.MAX_VALUE) {
			return Double.NaN;
		}

		return value;
	}

	@Override
	public Pair<Long, String> getNodeRepresentationModel(int node) {
		return Pair.of( //
				(long) (getNodeParameter(node, ParameterNodeType.synchronousMove)), //
				format(getNodeParameter(node, ParameterNodeType.synchronousMove)));
	}

	@Override
	public String[][] getNodeRepresentationPopup(int node) {
		List<String[]> result = new ArrayList<>();
		for (ParameterNodeType parameterType : ParameterNodeType.values()) {
			if (getNodeParameter(node, parameterType) != 0) {
				result.add(new String[] { "cost " + parameterType.toString(),
						format(parameterType.value2user(getNodeParameter(node, parameterType))) });
			}
		}

		String[][] r = new String[result.size()][2];
		return result.toArray(r);
	}

	@Override
	public List<DataRow<Object>> getModelRepresentation() {
		List<DataRow<Object>> result = new ArrayList<>();

		result.addAll(super.getModelRepresentation());

		for (String activity : logMove2index.keySet()) {
			int index = logMove2index.get(activity);
			result.add(
					new DataRow<Object>(DisplayType.numeric(parameters[index]), "cost of extra step in log", activity));
		}

		for (int node = 0; node < node2index.length; node++) {
			if (node2index[node] >= 0) {
				for (ParameterNodeType parameterType : ParameterNodeType.values()) {
					double value = parameterType.value2user(getNodeParameter(node, parameterType));
					result.add(new DataRow<Object>(DisplayType.numeric(value), "cost of " + parameterType.toString(),
							model.getActivityName(node)));
				}
			}

		}

		return result;
	}
}