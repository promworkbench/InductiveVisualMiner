package org.processmining.plugins.inductiveVisualMiner.editModel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.processmining.plugins.InductiveMiner.Triple;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.efficienttree.UnknownTreeNodeException;

public class DfgEditor extends JPanel {
	private static final long serialVersionUID = -75989442629887735L;

	private static final int update_delay = 500;
	private static final int spacesPerTab = 3;
	private static final Color errorColour = Color.yellow;

	protected final RSyntaxTextArea textStartActivities;
	protected final JLabel labelStartActivities;
	protected final RSyntaxTextArea textEdges;
	protected final JLabel labelEdges;
	protected final RSyntaxTextArea textEndActivities;
	protected final JLabel labelEndActivities;
	protected final JLabel errorMessage;
	private ActionListener actionListener;
	private boolean contentChangedFromController = false;

	/**
	 * 
	 * @param tree
	 *            May be null, in which case no tree will be set-up in the
	 *            editor.
	 * @param message
	 *            If not null, the message will be shown to the user and editing
	 *            the model will be disabled.
	 */
	public DfgEditor(Dfg dfg, String message) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);

		labelStartActivities = new JLabel("Start activities");
		add(labelStartActivities);

		//start activities text area
		{
			textStartActivities = new RSyntaxTextArea();
			textStartActivities.setTabSize(spacesPerTab);
			textStartActivities.setWhitespaceVisible(false);
			textStartActivities.discardAllEdits();
			JScrollPane textScroll = new JScrollPane(textStartActivities);
			add(textScroll);
		}

		labelEdges = new JLabel("Edges") {
			private static final long serialVersionUID = -5108825558323321667L;

			@Override
			public Dimension getMaximumSize() {
				Dimension d = super.getMaximumSize();
				d.width = Integer.MAX_VALUE;
				return d;
			}
		};
		add(labelEdges);

		//edges text area
		{
			textEdges = new RSyntaxTextArea();
			textEdges.setTabSize(spacesPerTab);
			textEdges.setWhitespaceVisible(false);
			textEdges.discardAllEdits();
			JScrollPane textScroll = new JScrollPane(textEdges);
			add(textScroll);
		}

		labelEndActivities = new JLabel("End activities") {
			private static final long serialVersionUID = 5730799157198750391L;

			@Override
			public Dimension getMaximumSize() {
				Dimension d = super.getMaximumSize();
				d.width = Integer.MAX_VALUE;
				return d;
			}
		};
		add(labelEndActivities);

		//end activities text area
		{
			textEndActivities = new RSyntaxTextArea();
			textEndActivities.setTabSize(spacesPerTab);
			textEndActivities.setWhitespaceVisible(false);
			textEndActivities.discardAllEdits();
			JScrollPane textScroll = new JScrollPane(textEndActivities);
			add(textScroll);
		}

		//error message
		errorMessage = new JLabel(" ") {
			private static final long serialVersionUID = -119544982967511643L;

			@Override
			public Dimension getMaximumSize() {
				Dimension d = super.getMaximumSize();
				d.width = Integer.MAX_VALUE;
				return d;
			}
		};
		errorMessage.setBackground(errorColour);
		errorMessage.setOpaque(false);
		add(errorMessage);

		if (dfg != null) {
			setDfg(dfg);
		}
		if (message != null) {
			setMessage(message);
		} else {
			try {
				setErrorMessage(-1, null);
			} catch (BadLocationException e1) {
				//can never happen
				e1.printStackTrace();
			}
		}

		// set update timer
		final AtomicReference<Timer> updateTimerR = new AtomicReference<Timer>();
		final Timer updateTimer = new Timer(update_delay, new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//if we're good to go, send an update to the controller
				if (actionListener != null && !contentChangedFromController) {
					try {
						Triple<Dfg, Integer, String> result = DfgParser.parse(textStartActivities.getText(),
								textEdges.getText(), textEndActivities.getText());
						if (result.getA() == null) {
							//set error message
							setErrorMessage(result.getB(), result.getC());
						} else {
							//remove error message
							setErrorMessage(-1, null);

							Dfg newTree = result.getA();
							final ActionEvent e2 = new ActionEvent(newTree, 0, "");
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									actionListener.actionPerformed(e2);
								}
							});
						}
					} catch (UnknownTreeNodeException e1) {
						e1.printStackTrace();
					} catch (BadLocationException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
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
		DocumentListener documentListener = new DocumentListener() {

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
		};

		textStartActivities.getDocument().addDocumentListener(documentListener);
		textEdges.getDocument().addDocumentListener(documentListener);
		textEndActivities.getDocument().addDocumentListener(documentListener);
	}

	/**
	 * If a message is set, editing the model is disabled.
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		textEdges.removeAllLineHighlights();
		textEdges.setEnabled(false);
		textStartActivities.setEnabled(false);
		textEndActivities.setEnabled(false);
		errorMessage.setOpaque(false);
		errorMessage.setText(message);
	}

	private void setErrorMessage(int line, String message) throws BadLocationException {
		if (line >= 0) {
			errorMessage.setText(message);
			errorMessage.setOpaque(true);
			textEdges.addLineHighlight(line, errorColour);
		} else {
			errorMessage.setText("Edge syntax: node -> \"node with spaces\".");
			errorMessage.setOpaque(false);
			textEdges.removeAllLineHighlights();
		}
	}

	/**
	 * Set the editor to the given tree.
	 * 
	 * @param tree
	 */
	public void setDfg(Dfg dfg) {
		assert (dfg != null);
		contentChangedFromController = true;
		textStartActivities.setText(Dfg2StringFields.getStartActivities(dfg));
		textStartActivities.setEnabled(true);
		textEdges.setText(Dfg2StringFields.getEdges(dfg));
		textEdges.setEnabled(true);
		textEndActivities.setText(Dfg2StringFields.getEndActivities(dfg));
		textEndActivities.setEnabled(true);
	}

	protected static void updateGraphOnTimer(Timer updateTimer) {
		updateTimer.restart();
	}

	public void addActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}
}
