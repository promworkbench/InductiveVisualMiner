package org.processmining.plugins.inductiveVisualMiner.colouringFilter;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.plugins.inductiveVisualMiner.InductiveVisualMinerPanel;
import org.processmining.plugins.inductiveVisualMiner.alignment.LogMovePosition;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.UnfoldedNode;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ColouringFiltersView extends SideWindow {

	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final Map<ColouringFilter, JComponent> filter2label;
	private final Map<ColouringFilter, Integer> filter2position;
	private int highFilters;

	public ColouringFiltersView(Component parent) {
		super(parent, "Inductive visual Miner - highlighting filters");
		panel = new JPanel(new GridBagLayout());
		panel.setBackground(Color.gray);
		add(panel);
		filter2label = new HashMap<>();
		filter2position = new HashMap<>();
		highFilters = 0;
	}

	public void initialise(List<ColouringFilter> colouringFilters) {
		int gridy = 1;
		Collections.sort(colouringFilters, new Comparator<ColouringFilter>() {
			public int compare(ColouringFilter o1, ColouringFilter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (ColouringFilter colouringFilter : colouringFilters) {
			//add label
			JLabel label = SlickerFactory.instance().createLabel(colouringFilter.getName() + " initialising ...");
			GridBagConstraints cLabel = new GridBagConstraints();
			cLabel.gridx = 1;
			cLabel.gridy = gridy;
			cLabel.fill = GridBagConstraints.VERTICAL;
			cLabel.anchor = GridBagConstraints.EAST;
			panel.add(label, cLabel);

			filter2label.put(colouringFilter, label);
			filter2position.put(colouringFilter, new Integer(gridy));

			gridy++;
		}
	}

	public void setPanel(final ColouringFilter colouringFilter, final Runnable onUpdate) {

		//if the colouringfilter did not initialise, print error message
		if (colouringFilter.getPanel() == null) {
			((JLabel) filter2label.get(colouringFilter)).setText(colouringFilter.getName()
					+ " did not initialise properly. It could be that the log contains inconsistent attribute types.");
			return;
		}

		//remove initialising message
		panel.remove(filter2label.get(colouringFilter));

		//add panel
		{
			GridBagConstraints cPanel = new GridBagConstraints();
			cPanel.gridx = 1;
			cPanel.gridy = filter2position.get(colouringFilter);
			cPanel.fill = GridBagConstraints.BOTH;
			cPanel.anchor = GridBagConstraints.NORTH;
			cPanel.weightx = 1;
			if (colouringFilter.getPanel().isUsesVerticalSpace()) {
				highFilters++;
				cPanel.weighty = 1 / (highFilters * 1.0);
			}
			panel.add(colouringFilter.getPanel(), cPanel);
		}

		//add checkbox
		{
			JCheckBox checkBox = new JCheckBox();
			checkBox.setBackground(Color.gray);
			GridBagConstraints cCheckBox = new GridBagConstraints();
			cCheckBox.gridx = 2;
			cCheckBox.gridy = filter2position.get(colouringFilter);
			cCheckBox.anchor = GridBagConstraints.CENTER;
			panel.add(checkBox, cCheckBox);
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean x = colouringFilter.swapEnabledFilter();
					if (x) {
						colouringFilter.getPanel().setBackground(Color.white);
					} else {
						colouringFilter.getPanel().setBackground(Color.gray);
					}
					onUpdate.run();
				}
			});
		}
	}

	/**
	 * Tell the user which traces are being coloured/selected
	 * 
	 * @param panel
	 * @param selectedNodes
	 * @param selectedLogMoves
	 * @param colouringFilters
	 * @param maxAnimatedTraces
	 * @param numberOfTraces
	 */
	public static void updateSelectionDescription(InductiveVisualMinerPanel panel, Set<UnfoldedNode> selectedNodes,
			Set<LogMovePosition> selectedLogMoves, List<ColouringFilter> colouringFilters, long numberOfTraces,
			long maxAnimatedTraces) {
		//show the user which traces are shown

		String[] criteria = new String[3];

		//selected nodes
		if (!selectedNodes.isEmpty()) {
			String nodes = "(should) include ";
			Iterator<UnfoldedNode> it = selectedNodes.iterator();
			{
				nodes += "`" + it.next().getNode() + "'";
			}
			while (it.hasNext()) {
				String p = it.next().getNode().toString();
				if (it.hasNext()) {
					nodes += ", `" + p + "'";
				} else {
					nodes += " or `" + p + "'";
				}
			}
			nodes += " according to the model";
			criteria[0] = nodes;
		} else {
			criteria[0] = "";
		}

		//selected log moves
		if (!selectedLogMoves.isEmpty()) {
			criteria[1] = "have an only-in-log event as selected";
		} else {
			criteria[1] = "";
		}

		//colouring filters
		{
			int enabledColouringFilters = 0;
			for (ColouringFilter colouringFilter : colouringFilters) {
				if (colouringFilter.isEnabledFilter()) {
					enabledColouringFilters++;
				}
			}
			if (enabledColouringFilters == 1) {
				criteria[2] = "pass the highlighting filter";
			} else if (enabledColouringFilters > 1) {
				criteria[2] = "pass the highlighting filters";
			} else {
				criteria[2] = "";
			}
		}

		//construct a sentence
		String s;
		int count = 0;
		for (int i = 0; i < 3; i++) {
			if (criteria[i] != "") {
				count++;
			}
		}
		if (count == 0) {
			//no criteria active
			s = "Highlighting all traces";
		} else if (count == 1) {
			//one criterion active
			s = "Highlighting traces that " + criteria[0] + criteria[1] + criteria[2];
		} else if (count == 2) {
			if (criteria[0] != "" && criteria[1] != "") {
				s = "Highlighting traces that " + criteria[0] + " or " + criteria[1];
			} else {
				s = "Highlighting traces that " + criteria[0] + criteria[1] + ", and " + criteria[2];
			}
		} else {
			//all criteria active
			s = "Highlighting traces that " + criteria[0] + " or " + criteria[1] + ", and that " + criteria[2];
		}

		//add animation message
		if (maxAnimatedTraces < numberOfTraces) {
			s += "; animating the first " + maxAnimatedTraces + ".";
		} else {
			s += ".";
		}

		panel.getSelectionLabel().setText(s);
	}
}
