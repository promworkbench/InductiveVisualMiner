package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.Color;

import javax.swing.border.EmptyBorder;

public class IvMDecoratorDefault extends IvMDecoratorAbstract {

	public static final Color textColour = new Color(40, 78, 107);
	public static final Color buttonColour = new Color(161, 207, 243);
	public static final Color backGroundColour1 = new Color(161, 207, 243);
	public static final Color backGroundColour2 = new Color(119, 175, 219);
	//public static final Color backGroundColour2 = new Color(84, 141, 184);
	public static final float fontSize = 11;
	public static final float fontSizeLarger = 12;
	public static final EmptyBorder paddings = new EmptyBorder(3, 3, 3, 3);

	public Color textColour() {
		return textColour;
	}

	public Color buttonColour() {
		return buttonColour;
	}

	public Color backGroundColour1() {
		return backGroundColour1;
	}

	public Color backGroundColour2() {
		return backGroundColour2;
	}

	public float fontSize() {
		return fontSize;
	}

	public float fontSizeLarger() {
		return fontSizeLarger;
	}

	public EmptyBorder paddings() {
		return paddings;
	}

}
