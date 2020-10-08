package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.MultiComboBox;

public interface IvMDecoratorI {

	public JPanel getPanel();

	public GradientPaint getGradient(int height);

	public void decorate(JLabel label);

	public void decorate(JButton button);

	public <X> void decorate(JComboBox<X> combobox);

	public <X> void decorate(MultiComboBox<X> combobox);

	public void decorate(JCheckBox checkBox);

	public void decorate(JTextArea textArea);

	public Font font();

	public Font fontLarger();

	public Font fontMonoSpace();

	public Color textColour();

	public Color buttonColour();

	public Color backGroundColour1();

	public Color backGroundColour2();

	public float fontSize();

	public float fontSizeLarger();

	public EmptyBorder paddings();

}
