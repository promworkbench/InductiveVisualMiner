package org.processmining.plugins.inductiveVisualMiner.helperClasses.decoration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
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
import javax.swing.text.JTextComponent;

import org.processmining.plugins.inductiveVisualMiner.helperClasses.MultiComboBox;

/**
 * Class to decorate gui elements. Adapted from Fluxicon's Slicker Factory.
 * 
 * @author sander
 *
 */
public abstract class IvMDecoratorAbstract implements IvMDecoratorI {

	private final Font font;
	private final Font fontLarger;
	private final Font fontMonoSpace;

	public IvMDecoratorAbstract() {
		JLabel label = new JLabel("abc");
		font = label.getFont().deriveFont(fontSize());
		fontLarger = label.getFont().deriveFont(fontSizeLarger());
		fontMonoSpace = new Font(Font.MONOSPACED, Font.BOLD, 12);
	}

	public JPanel getPanel() {
		return new IvMPanel(this);
	}

	public GradientPaint getGradient(int height) {
		return new GradientPaint(0, 0, backGroundColour1(), 0, height, backGroundColour2());
	}

	public void decorate(JLabel label) {
		label.setUI(new IvMLabelUI());
	}

	public void decorate(JButton button) {
		button.setUI(new IvMButtonUI());
	}

	public <X> void decorate(JComboBox<X> combobox) {
		combobox.setUI(new IvMComboBoxUI<X>());
	}

	public <X> void decorate(MultiComboBox<X> combobox) {
		combobox.setUI(new IvMMultiComboBoxUI<X>());
	}

	public void decorate(JCheckBox checkBox) {
		checkBox.setUI(new IvMCheckBoxUI());
	}

	public void decorate(JTextArea textArea) {
		textArea.setUI(new IvMTextAreaUI());
	}

	public Font font() {
		return font;
	}

	public Font fontLarger() {
		return fontLarger;
	}

	public Font fontMonoSpace() {
		return fontMonoSpace;
	}

	public class IvMPanelUI extends BasicPanelUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JPanel panel = (JPanel) c;
			panel.setOpaque(false);
			panel.setBackground(Color.cyan);
		}
	}

	public class IvMLabelUI extends BasicLabelUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JLabel label = (JLabel) c;
			label.setOpaque(false);
			label.setForeground(textColour());
			label.setFont(font);
		}
	}

	public class IvMButtonUI extends BasicButtonUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			final JButton button = (JButton) c;
			button.setOpaque(true);
			button.setForeground(textColour());
			button.setBackground(buttonColour());
			button.setBorder(paddings());
			button.setFont(font());

			button.addMouseListener(new MouseListener() {
				public void mouseClicked(MouseEvent arg0) {
				}

				public void mouseEntered(MouseEvent arg0) {
					button.setForeground(buttonColour());
					button.setBackground(textColour());
					button.repaint();
				}

				public void mouseExited(MouseEvent arg0) {
					button.setForeground(textColour());
					button.setBackground(buttonColour());
					button.repaint();
				}

				public void mousePressed(MouseEvent arg0) {

				}

				public void mouseReleased(MouseEvent arg0) {

				}
			});
		}
	}

	public class IvMComboBoxUI<X> extends BasicComboBoxUI {
		public void installUI(JComponent c) {
			super.installUI(c);

			UIManager.put("ComboBox.borderPaintsFocus", Boolean.TRUE);

			@SuppressWarnings("unchecked")
			final JComboBox<X> comboBox = (JComboBox<X>) c;
			comboBox.setOpaque(true);
			comboBox.setForeground(textColour());
			comboBox.setBackground(buttonColour());
			comboBox.setBorder(paddings());
			comboBox.setFocusable(false);
			comboBox.setFont(font());
			if (comboBox.getRenderer() instanceof JLabel) {
				final JLabel button = (JLabel) comboBox.getRenderer();
				button.setHorizontalAlignment(SwingConstants.CENTER);
			}

			//add hover effect similar to buttons
			comboBox.addMouseListener(new MouseAdapter() {
				public void mouseExited(MouseEvent arg0) {
					comboBox.setForeground(textColour());
					comboBox.setBackground(buttonColour());

					//search for the arrow button and style it
					for (Component component : comboBox.getComponents()) {
						if (component instanceof BasicArrowButton) {
							BasicArrowButton component2 = ((BasicArrowButton) component);
							component2.setBackground(buttonColour());
							try {
								Field field = BasicArrowButton.class.getDeclaredField("darkShadow");
								field.setAccessible(true);
								field.set(component2, textColour());
								field.setAccessible(false);
							} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
									| IllegalAccessException e) {
								e.printStackTrace();
							}
						}
					}
				}

				public void mouseEntered(MouseEvent arg0) {
					comboBox.setForeground(buttonColour());
					comboBox.setBackground(textColour());

					for (Component component : comboBox.getComponents()) {
						if (component instanceof BasicArrowButton) {
							BasicArrowButton component2 = ((BasicArrowButton) component);
							component2.setBackground(textColour());
							try {
								Field field = BasicArrowButton.class.getDeclaredField("darkShadow");
								field.setAccessible(true);
								field.set(component2, buttonColour());
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
			JButton result = new BasicArrowButton(BasicArrowButton.SOUTH, buttonColour(), buttonColour(), textColour(),
					buttonColour());
			result.setBorder(new EmptyBorder(0, 0, 0, 10));
			result.setOpaque(false);
			result.setBorderPainted(false);
			result.setFocusable(false);
			return result;
		}
	}

	public class IvMMultiComboBoxUI<X> extends IvMComboBoxUI<X> {
		public void installUI(JComponent c) {
			super.installUI(c);

			@SuppressWarnings("unchecked")
			MultiComboBox<X> comboBox = (MultiComboBox<X>) c;
			@SuppressWarnings("unchecked")
			MultiComboBox<X>.ButtonsRenderer renderer = (MultiComboBox<X>.ButtonsRenderer) comboBox.getRenderer();
			JLabel label = renderer.getLabel();
			decorate(label);
			label.setHorizontalAlignment(SwingConstants.CENTER);

			comboBox.addListeners();
		}
	}

	public class IvMCheckBoxUI extends BasicCheckBoxUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JCheckBox checkBox = (JCheckBox) c;
			checkBox.setOpaque(false);
		}
	}

	public class IvMTextAreaUI extends BasicTextAreaUI {
		public void installUI(JComponent c) {
			super.installUI(c);
			JTextComponent text = (JTextComponent) c;
			text.setOpaque(false);
			text.setFont(font());
			text.setForeground(textColour());
			text.setDisabledTextColor(textColour());
		}
	}
}
