package org.processmining.plugins.inductiveVisualMiner.logFiltering;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Filter events", returnLabels = { "Filtered log" }, returnTypes = { XLog.class }, parameterLabels = { "Log" }, userAccessible = true)
public class FilterEventsPlugin {

	private XLog log;
	private XLogInfo logInfo;
	private DefaultListModel<XEventClass> eventClassList = new DefaultListModel<XEventClass>();
	private JList<XEventClass> cEventClasses;

	private JComboBox<XEventClassifier> cClassifiers;
	private JRadioButton removeSelected;
	private JRadioButton replaceSelected;
	private JRadioButton removeUnselected;
	private JRadioButton replaceUnselected;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "S.J.J. Leemans", email = "s.j.j.leemans@tue.nl")
	@PluginVariant(variantLabel = "Filter log on life cycle, default", requiredParameterLabels = { 0 })
	public XLog filterLog(UIPluginContext context, final XLog log) throws Exception {

		this.log = log;

		XEventClassifier[] classifiers = { new XEventNameClassifier(), new XEventLifeTransClassifier(),
				new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()) };

		SlickerFactory factory = SlickerFactory.instance();

		JPanel thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		thresholdsPanel.setLayout(new BoxLayout(thresholdsPanel, BoxLayout.Y_AXIS));

		cClassifiers = factory.createComboBox(classifiers);
		cClassifiers.setAlignmentX(0.5f);
		thresholdsPanel.add(cClassifiers);

		cEventClasses = new JList<XEventClass>(eventClassList);
		cEventClasses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		cEventClasses.setLayoutOrientation(JList.VERTICAL);
		cEventClasses.setVisibleRowCount(-1);
		thresholdsPanel.add(new JScrollPane(cEventClasses));

		JPanel radioPanel = factory.createRoundedPanel(5, Color.gray);
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		thresholdsPanel.add(radioPanel);

		removeSelected = factory.createRadioButton("remove selected ");
		removeSelected.setSelected(true);
		radioPanel.add(removeSelected);

		replaceSelected = factory.createRadioButton("replace selected with 'environment' ");
		radioPanel.add(replaceSelected);

		removeUnselected = factory.createRadioButton("remove unselected ");
		radioPanel.add(removeUnselected);

		replaceUnselected = factory.createRadioButton("replace unselected with 'environment'");
		radioPanel.add(replaceUnselected);

		ButtonGroup group = new ButtonGroup();
		group.add(removeSelected);
		group.add(replaceSelected);
		group.add(removeUnselected);
		group.add(replaceUnselected);

		fillEventClasses();

		cClassifiers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fillEventClasses();
			}
		});

		InteractionResult result = context.showWizard("Filter events", true, true, thresholdsPanel);
		if (result != InteractionResult.FINISHED) {
			context.getFutureResult(0).cancel(true);
			return null;
		}

		return filter();
	}

	public void fillEventClasses() {
		XEventClassifier classifier = (XEventClassifier) cClassifiers.getSelectedItem();
		logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		eventClassList.clear();
		List<XEventClass> dumbJava = new LinkedList<XEventClass>(logInfo.getEventClasses().getClasses());
		Collections.sort(dumbJava);
		for (XEventClass ec : dumbJava) {
			eventClassList.addElement(ec);
		}
		if (eventClassList.getSize() > 0) {
			cEventClasses.setSelectionInterval(0, eventClassList.getSize() - 1);
		}
	}

	public XLog filter() {
		//make set of selected event classes
		Set<XEventClass> selectedEventClasses = new HashSet<XEventClass>(cEventClasses.getSelectedValuesList());

		//copy only the events with event class that was selected
		XLog result = new XLogImpl(log.getAttributes());
		
		//create environment event
		XAttributeMap environmentMap = new XAttributeMapImpl();
		environmentMap.put("concept:name", new XAttributeLiteralImpl("concept:name", "environment"));
		environmentMap.put("lifecycle:transition", new XAttributeLiteralImpl("lifecycle:transition", "complete"));
		environmentMap.put("org:resource", new XAttributeLiteralImpl("org:resource", "artificial"));
		
		for (XTrace trace : log) {
			XTrace copyTrace = new XTraceImpl(trace.getAttributes());
			for (XEvent event : trace) {
				if (removeSelected.isSelected()) {
					if (!selectedEventClasses.contains(logInfo.getEventClasses().getClassOf(event))) {
						copyTrace.add(event);
					}
				} else if (removeUnselected.isSelected()) {
					if (selectedEventClasses.contains(logInfo.getEventClasses().getClassOf(event))) {
						copyTrace.add(event);
					}
				} else if (replaceSelected.isSelected()) {
					if (selectedEventClasses.contains(logInfo.getEventClasses().getClassOf(event))) {
						copyTrace.add(new XEventImpl(environmentMap));
					} else {
						copyTrace.add(event);
					}
				} else {
					if (!selectedEventClasses.contains(logInfo.getEventClasses().getClassOf(event))) {
						copyTrace.add(new XEventImpl(environmentMap));
					} else {
						copyTrace.add(event);
					}
				}
			}
			if (copyTrace.size() > 0) {
				result.add(copyTrace);
			}
		}
		return result;
	}

}
