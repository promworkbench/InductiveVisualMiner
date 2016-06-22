package org.processmining.plugins.inductiveVisualMiner.ivmfilter.preminingfilters.filters;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JSlider;

import org.processmining.plugins.inductiveVisualMiner.ivmfilter.IvMFilterGui;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class SliderGui extends IvMFilterGui {
	private static final long serialVersionUID = 436297522448887470L;

	private final JLabel explanation;
	private final JSlider slider;

	public SliderGui(String title) {
		super(title);
		usesVerticalSpace = false;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		//explanation
		{
			explanation = new JLabel("title");
			add(explanation);
		}

		//slider
		{
			slider = SlickerFactory.instance().createSlider(JSlider.HORIZONTAL);
			slider.setMaximum(1000);
			slider.setMinimum(0);
			slider.setValue(1000);
			add(slider);
		}
	}

	public JLabel getExplanation() {
		return explanation;
	}

	public JSlider getSlider() {
		return slider;
	}

	protected void setForegroundRecursively(Color colour) {
		if (explanation != null) {
			explanation.setForeground(colour);
		}
	}
}
