package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Field;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicTextAreaUI;

import org.processmining.plugins.InductiveMiner.MultiComboBox;

/**
 * Class to decorate gui elements. Adapted from Fluxicon's Slicker Factory.
 * 
 * @author sander
 *
 */
public class IvMDecorator {

	public static final Color textColour = new Color(40, 78, 107);
	public static final Color buttonColour = new Color(161, 207, 243);
	public static final Color backGroundColour1 = new Color(119, 175, 219);
	public static final Color backGroundColour2 = new Color(84, 141, 184);
	public static final float fontSize = 11;
	public static final float fontSizeLarger = 12;
	public static final Font font;
	public static final Font fontLarger;
	static {
		JLabel label = new JLabel("abc");
		font = label.getFont().deriveFont(fontSize);
		fontLarger = label.getFont().deriveFont(fontSizeLarger);
	}

	private static final EmptyBorder paddings = new EmptyBorder(3, 3, 3, 3);

	public static class IvMPanel extends JPanel {
		private static final long serialVersionUID = 4780045831156679205L;

		public IvMPanel() {
			setOpaque(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			GradientPaint gp = getGradient();
			if (gp != null) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setPaint(gp);
				g2d.fillRect(0, 0, getWidth(), getHeight());
			}

			super.paintComponent(g);
		}

		protected GradientPaint getGradient() {
			return new GradientPaint(0, 0, backGroundColour1, 0, getHeight(), backGroundColour2);
		}
	}

	public static void decorate(JLabel label) {
		label.setUI(new IvMLabelUI());
	}

	public static void decorate(JButton button) {
		button.setUI(new IvMButtonUI());
	}

	public static <X> void decorate(JComboBox<X> combobox) {
		combobox.setUI(new IvMComboBoxUI<X>());
	}

	public static <X> void decorate(MultiComboBox<X> combobox) {
		//combobox.setUI(new IvMMultiComboBoxUI<X>());
	}

	public static void decorate(JCheckBox checkBox) {
		checkBox.setUI(new IvMCheckBoxUI());
	}

	public static void decorate(JTextArea textArea) {
		textArea.setUI(new IvMTextAreaUI());
	}

	public static class IvMPanelUI extends BasicPanelUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JPanel panel = (JPanel) c;
			panel.setOpaque(false);
			panel.setBackground(Color.cyan);
		}
	}

	public static class IvMLabelUI extends BasicLabelUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JLabel label = (JLabel) c;
			label.setOpaque(false);
			label.setForeground(textColour);
			label.setFont(font);
		}
	}

	public static class IvMButtonUI extends BasicButtonUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			final JButton button = (JButton) c;
			button.setOpaque(true);
			button.setForeground(textColour);
			button.setBackground(buttonColour);
			button.setBorder(paddings);
			button.setFont(font);

			button.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
				}

				public void mouseEntered(MouseEvent arg0) {
					button.setForeground(buttonColour);
					button.setBackground(textColour);
					button.repaint();
				}

				public void mouseExited(MouseEvent arg0) {
					button.setForeground(textColour);
					button.setBackground(buttonColour);
					button.repaint();
				}

				public void mousePressed(MouseEvent arg0) {

				}

				public void mouseReleased(MouseEvent arg0) {

				}
			});
		}
	}

	public static class IvMComboBoxUI<X> extends BasicComboBoxUI {
		public void installUI(JComponent c) {
			super.installUI(c);

			UIManager.put("ComboBox.borderPaintsFocus", Boolean.TRUE);

			@SuppressWarnings("unchecked")
			final JComboBox<X> comboBox = (JComboBox<X>) c;
			comboBox.setOpaque(true);
			comboBox.setForeground(textColour);
			comboBox.setBackground(buttonColour);
			comboBox.setBorder(paddings);
			comboBox.setFocusable(false);
			comboBox.setFont(font);
			if (comboBox.getRenderer() instanceof JLabel) {
				final JLabel button = (JLabel) comboBox.getRenderer();
				button.setHorizontalAlignment(SwingConstants.CENTER);
			}

			//add hover effect similar to buttons
			comboBox.addMouseListener(new MouseAdapter() {
				public void mouseExited(MouseEvent arg0) {
					comboBox.setForeground(textColour);
					comboBox.setBackground(buttonColour);

					for (Component component : comboBox.getComponents()) {
						if (component instanceof BasicArrowButton) {
							BasicArrowButton component2 = ((BasicArrowButton) component);
							component2.setBackground(buttonColour);
							try {
								Field field = BasicArrowButton.class.getDeclaredField("darkShadow");
								field.setAccessible(true);
								field.set(component2, textColour);
								field.setAccessible(false);
							} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
									| IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}

				public void mouseEntered(MouseEvent arg0) {
					comboBox.setForeground(buttonColour);
					comboBox.setBackground(textColour);

					for (Component component : comboBox.getComponents()) {
						if (component instanceof BasicArrowButton) {
							BasicArrowButton component2 = ((BasicArrowButton) component);
							component2.setBackground(textColour);
							try {
								Field field = BasicArrowButton.class.getDeclaredField("darkShadow");
								field.setAccessible(true);
								field.set(component2, buttonColour);
								field.setAccessible(false);
							} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
									| IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		}

		@Override
		protected JButton createArrowButton() {
			JButton result = new BasicArrowButton(BasicArrowButton.SOUTH, buttonColour, buttonColour, textColour,
					buttonColour);
			result.setBorder(new EmptyBorder(0, 0, 0, 10));
			result.setOpaque(false);
			result.setBorderPainted(false);
			result.setFocusable(false);
			return result;
		}
	}

//	public static class IvMMultiComboBoxUI<X> extends IvMComboBoxUI<X> {
//		public void installUI(JComponent c) {
//			super.installUI(c);
//
//			@SuppressWarnings("unchecked")
//			MultiComboBox<X> comboBox = (MultiComboBox<X>) c;
//			@SuppressWarnings("unchecked")
//			MultiComboBox<X>.ButtonsRenderer renderer = (MultiComboBox<X>.ButtonsRenderer) comboBox.getRenderer();
//			decorate(renderer.label);
//		}
//	}

	public static class IvMCheckBoxUI extends BasicCheckBoxUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JCheckBox checkBox = (JCheckBox) c;
			checkBox.setOpaque(false);
		}
	}

	public static class IvMTextAreaUI extends BasicTextAreaUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JTextArea text = (JTextArea) c;
			text.setOpaque(false);
			text.setFont(font);
			text.setForeground(textColour);
			text.setDisabledTextColor(textColour);
		}
	}
}
