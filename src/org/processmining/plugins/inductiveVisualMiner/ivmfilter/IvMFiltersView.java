package org.processmining.plugins.inductiveVisualMiner.ivmfilter;

import java.awt.BorderLayout;
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

import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.IvMDecorator;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration.SwitchPanel;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

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
		panel.setBackground(IvMDecorator.backGroundColour1);
		panel.setOpaque(true);
		filter2panel = new THashMap<>();
		filter2initialisationLabel = new THashMap<>();
		filter2index = new TObjectIntHashMap<>();

		JTextArea explanation = new JTextArea(header);
		IvMDecorator.decorate(explanation);
		explanation.setWrapStyleWord(true);
		explanation.setLineWrap(true);
		explanation.setEnabled(false);
		panel.add(explanation);

		JScrollPane scrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getViewport().setBackground(IvMDecorator.backGroundColour1);
		add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * Initialise the view. Does not initialise the filters.
	 * 
	 * @param filters
	 */
	public void initialise(List<? extends IvMFilter> filters) {
		Collections.sort(filters, new Comparator<IvMFilter>() {
			public int compare(IvMFilter o1, IvMFilter o2) {
				return o1.getFilterName().compareTo(o2.getFilterName());
			}
		});
		int index = 1;
		for (IvMFilter filter : filters) {
			//add panel
			SwitchPanel subPanel = new SwitchPanel() {
				private static final long serialVersionUID = 132082897536007044L;

				@Override
				protected java.awt.GradientPaint getDisabledGradient() {
					return null;
				};
			};
			subPanel.setEnabled(false);
			subPanel.setBorder(2, 0, 0, 0, IvMDecorator.backGroundColour1);
			subPanel.setLayout(new BorderLayout());
			panel.add(subPanel);

			//add label
			JLabel label = new JLabel(filter.getFilterName() + " initialising ...");
			IvMDecorator.decorate(label);
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
			filter2initialisationLabel.get(filter).setText(filter.getFilterName()
					+ " did not initialise properly. It could be that the log contains inconsistent attribute types.");
			return;
		}

		final JPanel subPanel = filter2panel.get(filter);
		final int index = filter2index.get(filter);

		//remove initialising message
		subPanel.remove(filter2panel.get(filter));

		//add panel
		subPanel.add(filter.getPanel(), BorderLayout.CENTER);

		//add checkbox
		{
			JCheckBox checkBox = new JCheckBox();
			IvMDecorator.decorate(checkBox);
			subPanel.add(checkBox, BorderLayout.LINE_START);
			checkBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					boolean x = filter.swapEnabledFilter();
					subPanel.setEnabled(x);
					onUpdate.run();
				}
			});
		}
	}
}
