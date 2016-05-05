package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.efficienttree.EfficientTree;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.IvMEfficientTree;
import org.processmining.plugins.inductiveVisualMiner.helperClasses.SideWindow;

public class EditModelView extends SideWindow {

	private static final int update_delay = 500;
	private static final int spacesPerTab = 3;
	private static final Color errorColour = Color.yellow;

	private static final long serialVersionUID = 7325996031344244682L;
	private IvMEfficientTree tree = null;
	private final RSyntaxTextArea text;
	private final JLabel errorMessage;
	private ActionListener actionListener;
	private boolean contentChangedFromController = false;

	public EditModelView(Component parent) {
		super(parent, "edit model - Inductive visual Miner");
		
		//text area
		text = new RSyntaxTextArea();
		text.setTabSize(spacesPerTab);
		text.setWhitespaceVisible(true);
		text.discardAllEdits();
		JScrollPane textScroll = new JScrollPane(text);
		add(textScroll, BorderLayout.CENTER);
		
		//error message
		errorMessage = new JLabel(" ");
		errorMessage.setBackground(errorColour);
		add(errorMessage, BorderLayout.PAGE_END);
		
		setTree(null);

		// set update timer
		final AtomicReference<Timer> updateTimerR = new AtomicReference<Timer>();
		final Timer updateTimer = new Timer(update_delay, new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//if we're good to go, send an update to the controller
				if (tree != null && actionListener != null && !contentChangedFromController) {
					try {
						Triple<EfficientTree, Integer, String> result = ProcessTreeParser.parse(text.getText(),
								spacesPerTab);
						if (result.getA() == null) {
							//set error message
							setErrorMessage(result.getB(), result.getC());
						} else {
							//remove error message
							setErrorMessage(-1, null);
							
							IvMEfficientTree newTree = new IvMEfficientTree(result.getA());
							final ActionEvent e2 = new ActionEvent(newTree, 0, "");
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									actionListener.actionPerformed(e2);
								}
							});
						}
					} catch (UnknownTreeNodeException | IOException e1) {
						e1.printStackTrace();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					}
				}
				contentChangedFromController = false;
				updateTimerR.get().stop();
			}
		});
		updateTimer.setCoalesce(true);
		updateTimerR.set(updateTimer);

		// set onkey handlers
		text.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}

			public void insertUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					setErrorMessage(-1, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				updateGraphOnTimer(updateTimer);
			}
		});
	}

	protected static void updateGraphOnTimer(Timer updateTimer) {
		updateTimer.restart();
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	public void setTree(IvMEfficientTree tree) {
		contentChangedFromController = true;
		this.tree = tree;
		if (tree == null) {
			setMessage("Mining a model...");
			text.setEnabled(false);
		} else {
			text.setText(TreeLanguage.toString(tree));
			text.setEnabled(true);
		}
	}
	
	private void setMessage(String message) {
		text.removeAllLineHighlights();
		errorMessage.setOpaque(false);
		errorMessage.setText(message);
	}
	
	private void setErrorMessage(int line, String message) throws BadLocationException {
		if (line >= 0) {
			errorMessage.setText(message);
			errorMessage.setOpaque(true);
			text.addLineHighlight(line, errorColour);
		} else {
			errorMessage.setText("Operators: xor, sequence, concurrent, interleaved, loop & or.");
			errorMessage.setOpaque(false);
			text.removeAllLineHighlights();
		}
	}
}
