package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Scrollable;

import org.processmining.plugins.InductiveMiner.MultiComboBox;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public abstract class IvMFiltersView extends SideWindow {
	private static final long serialVersionUID = -5500440632866414477L;
	private final JPanel panel;
	private final Map<IvMFilter, JPanel> filter2panel;
	private final Map<IvMFilter, JLabel> filter2initialisationLabel;
	private final TObjectIntMap<IvMFilter> filter2index;

	public class IvMFiltersViewPanel extends JPanel implements Scrollable {
		private static final long serialVersionUID = 8311080909592746520L;

		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}

	public IvMFiltersView(Component parent, String title, String header) {
		super(parent, title);
		setLayout(new BorderLayout());

		panel = new IvMFiltersViewPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(MultiComboBox.even_colour_bg);
		filter2panel = new THashMap<>();
		filter2initialisationLabel = new THashMap<>();
		filter2index = new TObjectIntHashMap<>();

		JTextArea explanation = new JTextArea(header);
		explanation.setWrapStyleWord(true);
		explanation.setLineWrap(true);
		explanation.setOpaque(false);
		explanation.setEnabled(false);
		explanation.setFont(new JLabel("blaa").getFont());
		explanation.setDisabledTextColor(MultiComboBox.colour_fg);
		panel.add(explanation);

		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		SlickerDecorator.instance().decorate(scrollPane, MultiComboBox.even_colour_bg, Color.DARK_GRAY, Color.GRAY);
		scrollPane.setOpaque(false);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void initialise(List<? extends IvMFilter> filters) {
		Collections.sort(filters, new Comparator<IvMFilter>() {
			public int compare(IvMFilter o1, IvMFilter o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		int index = 1;
		for (IvMFilter filter : filters) {
			//add panel
			JPanel subPanel = new JPanel();
			subPanel.setOpaque(true);
			subPanel.setLayout(new BorderLayout());
			subPanel.setBackground(index % 2 == 0 ? MultiComboBox.even_colour_bg : MultiComboBox.odd_colour_bg);
			subPanel.setForeground(MultiComboBox.colour_fg);
			panel.add(subPanel);

			//add label
			JLabel label = SlickerFactory.instance().createLabel(filter.getName() + " initialising ...");
			subPanel.add(label);

			filter2panel.put(filter, subPanel);
			filter2initialisationLabel.put(filter, label);
			filter2index.put(filter, index);
			index++;
		}
	}

	public void setPanel(final IvMFilter filter, final Runnable onUpdate) {

		//if the colouringfilter did not initialise, print error message
		if (filter.getPanel() == null) {
			filter2initialisationLabel
					.get(filter)
					.setText(
							filter.getName()
									+ " did not initialise properly. It could be that the log contains inconsistent attribute types.");
			return;
		}

		final JPanel subPanel = filter2panel.get(filter);
		final int index = filter2index.get(filter);

		//remove initialising message
		subPanel.remove(filter2panel.get(filter));

		//add panel
		subPanel.add(filter.getPanel(), BorderLayout.CENTER);
		filter.getPanel().setForeground(MultiComboBox.colour_fg);

		//add checkbox
		{
			JCheckBox checkBox = new JCheckBox();
			SlickerDecorator.instance().decorate(checkBox);
			subPanel.add(checkBox, BorderLayout.LINE_START);
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean x = filter.swapEnabledFilter();
					if (x) {
						subPanel.setBackground(MultiComboBox.selection_colour_bg);
						subPanel.setForeground(MultiComboBox.selection_colour_fg);
						filter.getPanel().setForeground(MultiComboBox.selection_colour_fg);
					} else {
						subPanel.setBackground(index % 2 == 0 ? MultiComboBox.even_colour_bg
								: MultiComboBox.odd_colour_bg);
						subPanel.setForeground(MultiComboBox.colour_fg);
						filter.getPanel().setForeground(MultiComboBox.colour_fg);
					}
					onUpdate.run();
				}
			});
		}
	}
}
