package org.processmining.plugins.inductiveVisualMiner.chain;

import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerState;

import gnu.trove.map.hash.THashMap;

public class DataState extends InductiveVisualMinerState {

	//inputs
	public static final String input_log = "input log";

	//computed objects
	public static final String sorted_log = "sorted log";
	public static final String log_info = "log info";
	public static final String initial_classifier = "initial classifier";
	public static final String classifiers = "classifiers";
	public static final String selected_miner = "selected miner";
	public static final String model = "model";
	public static final String trace_colour_map = "trace colour map";
	public static final String trace_colour_map_settings = "trace colour map settings";

	//controllers
	public static final String highlighting_filters_controller = "highlighting filters controller";

	//user selections
	public static final String selected_activities_threshold = "selected activities threshold";
	public static final String selected_visualisation_mode = "selected visualisation mode";
	public static final String selected_model_selection = "selected model selection";
	public static final String selected_graph_user_settings = "selected graph user settings";
	public static final String selected_animation_enabled = "selected animation enabled";

	public DataState(XLog xLog) throws UnknownTreeNodeException {
		super(xLog);
		putObject(input_log, xLog);
	}

	private THashMap<String, Object> objects;

	public boolean hasObject(String name) {
		return objects.containsKey(name);
	}

	public Object getObject(String name) {
		return objects.get(name);
	}

	public void putObject(String name, Object object) {
		objects.put(name, object);
	}

	public void removeObject(String name) {
		objects.remove(name);
	}
}
