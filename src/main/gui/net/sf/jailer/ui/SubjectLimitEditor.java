/*
 * Copyright 2007 - 2025 Ralf Wisser.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.jailer.ui;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Rectangle2D;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.EscapableDialog;

import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.datamodel.Table;
import net.sf.jailer.extractionmodel.SubjectLimitDefinition;
import net.sf.jailer.ui.syntaxtextarea.DataModelBasedSQLCompletionProvider;
import net.sf.jailer.ui.syntaxtextarea.RSyntaxTextAreaWithSQLSyntaxStyle;
import net.sf.jailer.ui.syntaxtextarea.SQLAutoCompletion;
import net.sf.jailer.ui.syntaxtextarea.SQLCompletionProvider;
import net.sf.jailer.ui.util.SizeGrip;

/**
 * Editor for {@link SubjectLimitDefinition}.
 * 
 * @author Ralf Wisser
 */
public abstract class SubjectLimitEditor extends EscapableDialog {

	private boolean ok;
	private boolean escaped;
	private DataModelBasedSQLCompletionProvider provider;

	/** Creates new form */
	public SubjectLimitEditor(java.awt.Frame parent, DataModel dataModel) {
		super(parent, false);
		init(dataModel, false);
	}

	/** Creates new form 
	 * @param modal */
	public SubjectLimitEditor(Dialog parent, DataModel dataModel, boolean modal) {
		super(parent, modal);
		init(dataModel, modal);
	}

	@SuppressWarnings("serial")
	private void init(DataModel dataModel, boolean modal) {
		if (!modal) {
			setUndecorated(true);
		}
		initComponents(); UIUtil.initComponents(this);

		okButton.setIcon(UIUtil.scaleIcon(okButton, okIcon));
		cancelButton.setIcon(UIUtil.scaleIcon(cancelButton, cancelIcon));
		
		addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				ok = !escaped;
				setVisible(false);
			}
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}
		});
		
		addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {
				limitTextField.grabFocus();
			}
			@Override
			public void componentResized(ComponentEvent e) {
			}
			@Override
			public void componentMoved(ComponentEvent e) {
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				consume();
			}
		});

		this.editorPane = new RSyntaxTextAreaWithSQLSyntaxStyle(false, false) {
			@Override
			protected void runBlock() {
				super.runBlock();
				okButtonActionPerformed(null);
			}
			@Override
			protected boolean withFindAndReplace() {
				return false;
			}
		};
		JScrollPane jScrollPane2 = new JScrollPane();
		jScrollPane2.setViewportView(editorPane);
		
		JPanel corner = new SizeGrip();
		gripPanel.add(corner);

		GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 10;
		gridBagConstraints.gridy = 10;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		jPanel5.add(jScrollPane2, gridBagConstraints);
		jScrollPane2.setViewportView(editorPane);
		
		if (dataModel != null) {
			provider = new DataModelBasedSQLCompletionProvider(null, dataModel);
			provider.setDefaultClause(SQLCompletionProvider.Clause.WHERE);
			sqlAutoCompletion = new SQLAutoCompletion(provider, editorPane);
		}
		
		setLocation(400, 150);
		pack();
		setSize(Math.max(getWidth(), 600), Math.max(getHeight(), 200));
	}
	
	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        limitTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        orderByCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        hintLabel = new javax.swing.JLabel();
        gripPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(" Export only the first ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel1, gridBagConstraints);

        jPanel5.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel2.add(jPanel5, gridBagConstraints);

        limitTextField.setColumns(6);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel2.add(limitTextField, gridBagConstraints);

        jLabel2.setText(" rows");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel2.add(jLabel2, gridBagConstraints);

        orderByCheckBox.setText("according to the order:");
        orderByCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orderByCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(orderByCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 20;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jPanel2, gridBagConstraints);

        jPanel3.setLayout(new java.awt.GridBagLayout());

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanel3.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 16);
        jPanel3.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 30;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel3, gridBagConstraints);

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel4.setLayout(new java.awt.GridBagLayout());

        hintLabel.setText("<html>  <i>Ctrl+Space</i> for code completion. <i>Ctrl+Enter</i> for Ok.<i>Esc</i> for Cancel.</html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        jPanel4.add(hintLabel, gridBagConstraints);

        gripPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
        jPanel4.add(gripPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 30;
        gridBagConstraints.gridwidth = 30;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		ok = true;
		setVisible(false);
	}//GEN-LAST:event_okButtonActionPerformed

	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
		escaped = true;
		setVisible(false);
	}//GEN-LAST:event_cancelButtonActionPerformed

    private void orderByCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orderByCheckBoxActionPerformed
    	if (orderByCheckBox.isSelected()) {
    		editorPane.setText("order by ");
    		editorPane.setEnabled(true);
    		editorPane.grabFocus();
    	} else {
    		editorPane.setText("");
    		editorPane.setEnabled(false);
    	}
    }//GEN-LAST:event_orderByCheckBoxActionPerformed

	private SubjectLimitDefinition subjectLimitDefinition;
	
	/**
	 * Edits a given definition.
	 */
	public void edit(JComponent locator, Table table, SubjectLimitDefinition subjectLimitDefinition) {
		this.subjectLimitDefinition = subjectLimitDefinition;
		ok = false;
		escaped = false;
		limitTextField.setText(subjectLimitDefinition.limit == null? "" : subjectLimitDefinition.limit.toString());
		if (subjectLimitDefinition.orderBy != null) {
			orderByCheckBox.setSelected(true);
			editorPane.setText("order by " + subjectLimitDefinition.orderBy);
			editorPane.setEnabled(true);
		} else {
			orderByCheckBox.setSelected(false);
			editorPane.setText("");
			editorPane.setEnabled(false);
		}
		editorPane.setCaretPosition(0);
		editorPane.discardAllEdits();

		if (provider != null) {
			provider.removeAliases();
			if (table != null) {
				provider.addAlias("T", table);
			}
		}
		UIUtil.invokeLater(2, new Runnable() {
			@Override
			public void run() {
				editorPane.grabFocus();
			}
		});
		
		if (locator != null) {
			Point locationLocation = locator.getLocationOnScreen();
        	Point location;
        	location = new Point(locationLocation.x - 4, locationLocation.y + locator.getHeight() + 4);
        	setLocationAndFit(location);
		}
		
		UIUtil.invokeLater(() -> requestFocus());
		setVisible(true);
	}

	@Override
	protected void escapePressed() {
		escaped = true;
		super.escapePressed();
	}

	/**
	 * Removes single line comments.
	 * 
	 * @param statement
	 *            the statement
	 * 
	 * @return statement the statement without comments and literals
	 */
	private String removeSingleLineComments(String statement) {
		Pattern pattern = Pattern.compile("('(?:[^']*'))|(/\\*.*?\\*/)|(\\-\\-.*?(?=\n|$))", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(statement);
		boolean result = matcher.find();
		StringBuffer sb = new StringBuffer();
		if (result) {
			do {
				if (matcher.group(3) == null) {
					matcher.appendReplacement(sb, "$0");
					result = matcher.find();
					continue;
				}
				int l = matcher.group(0).length();
				matcher.appendReplacement(sb, "");
				if (matcher.group(1) != null) {
					l -= 2;
					sb.append("'");
				}
				while (l > 0) {
					--l;
					sb.append(' ');
				}
				if (matcher.group(1) != null) {
					sb.append("'");
				}
				result = matcher.find();
			} while (result);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public void setLocationAndFit(Point pos) {
		setLocation(pos);
		UIUtil.fit(this);
        try {
            // Get the size of the screen
            Rectangle2D dim = UIUtil.getScreenBounds();
            int hd = (int) (getY() - (dim.getHeight() - 80));
            if (hd > 0) {
                setLocation(getX(), Math.max(getY() - hd, (int) dim.getY()));
            }
        } catch (Throwable t) {
            // ignore
        }
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel gripPanel;
    private javax.swing.JLabel hintLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTextField limitTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox orderByCheckBox;
    // End of variables declaration//GEN-END:variables
	
	private Icon dropDownIcon;
	{
		// load images
		dropDownIcon = UIUtil.readImage("/dropdown.png");
	}

	public RSyntaxTextAreaWithSQLSyntaxStyle editorPane;
	private SQLAutoCompletion sqlAutoCompletion;

	public void observe(final JTextField textfield, final Runnable open) {
		InputMap im = textfield.getInputMap();
		@SuppressWarnings("serial")
		Action a = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String origText = textfield.getText();
				String caretMarker;
				for (int suffix = 0; ; suffix++) {
					caretMarker = "CARET" + suffix;
					if (!origText.contains(caretMarker)) {
						break;
					}
				}
				try {
					textfield.getDocument().insertString(textfield.getCaretPosition(), caretMarker, null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				open.run();
				textfield.setText(origText);
				String text = editorPane.getText();
				int i = text.indexOf(caretMarker);
				if (i >= 0) {
					editorPane.setText(text.substring(0, i) + text.substring(i + caretMarker.length()));
					editorPane.setCaretPosition(i);
				}
				UIUtil.invokeLater(1, new Runnable() {
					@Override
					public void run() {
						sqlAutoCompletion.doCompletion();
					}
				});
			}
		};
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK);
		im.put(ks, a);
		ActionMap am = textfield.getActionMap();
		am.put(a, a);
	}

	private void consume() {
		Long limit = null;
		try {
			limit = Long.parseLong(limitTextField.getText().trim());
		} catch (Exception e) {
			limit = null;
		}
		String orderBy = removeSingleLineComments(editorPane.getText())
			.replaceAll("\\n(\\r?) *", " ")
			.replace('\n', ' ')
			.replace('\r', ' ')
			.replaceFirst("(?is)^order\\s+by\\s*", "")
			.replaceAll(";\\s*$", "");
		SubjectLimitDefinition def = new SubjectLimitDefinition(limit, limit != null && orderBy.length() > 0? orderBy : null);
		if (ok && def.equals(subjectLimitDefinition)) {
			ok = false;
		}
		consume(ok? def : null);
	}

	public static String subjectLimitDefinitionRender(SubjectLimitDefinition limitDefinition) {
		if (limitDefinition.limit == null) {
			return "<html><i>no limit</i></html>";
		} else {
			return "<html><b>" + limitDefinition.limit + "</b> rows" + (limitDefinition.orderBy != null ? " (ordered)" : "") + "</html>";
		}
	}

	protected abstract void consume(SubjectLimitDefinition subjectLimitDefinition);
	
	private static final long serialVersionUID = -5169934807182707970L;
	
	private static ImageIcon okIcon;
	private static ImageIcon cancelIcon;
	
	static {
        // load images
        okIcon = UIUtil.readImage("/buttonok.png");
        cancelIcon = UIUtil.readImage("/buttoncancel.png");
	}
	
}
