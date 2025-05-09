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
package net.sf.jailer.ui.databrowser;

import java.awt.GridBagConstraints;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sf.jailer.ExecutionContext;
import net.sf.jailer.database.Session;
import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.datamodel.Table;
import net.sf.jailer.modelbuilder.JDBCMetaDataBasedModelElementFinder;
import net.sf.jailer.ui.DbConnectionDialog;
import net.sf.jailer.ui.Environment;
import net.sf.jailer.ui.JComboBox2;
import net.sf.jailer.ui.SessionForUI;
import net.sf.jailer.ui.UIUtil;
import net.sf.jailer.util.Quoting;

/**
 * Schema Mapping Dialog.
 *
 * @author Ralf Wisser
 */
public class SchemaMappingDialog extends javax.swing.JDialog {
	private static final long serialVersionUID = 1L;
	private String defaultSchemaName = "<default>";
	private boolean ok = false;
	private Map<String, JComboBox2> comboboxes = new TreeMap<String, JComboBox2>();
	
	/** Creates new form SchemaMappingDialog 
	 * @param mapping 
	 * @throws Exception */
	public SchemaMappingDialog(java.awt.Frame parent, DataModel dataModel, DbConnectionDialog connectionDialog, Session session, Map<String, String> mapping, ExecutionContext executionContext) throws Exception {
		super(parent, true);
		initComponents(); UIUtil.initComponents(this);
		
		okButton.setIcon(UIUtil.scaleIcon(okButton, okIcon));
		jButton2.setIcon(UIUtil.scaleIcon(jButton2, cancelIcon));
		
		Window windowAncestor = parent; // SwingUtilities.getWindowAncestor(parent);
		try {
			if (windowAncestor != null) {
				UIUtil.setWaitCursor(windowAncestor);
			}
			String defSchema = (String) session.getSessionProperty(SessionForUI.class, "defSchema");						
			if (defSchema == null) {
				defSchema = JDBCMetaDataBasedModelElementFinder.getDefaultSchema(session, session.getSchema());
			}
			if (!defSchema.isEmpty()) {
				defaultSchemaName = defSchema;
			}
			SortedSet<String> modelSchemas = new TreeSet<String>();
			boolean hasDS = false;
			for (Table table: new DataModel(executionContext).getTables()) {
				final String schema = table.getSchema(defaultSchemaName);
				modelSchemas.add(schema);
				if (Quoting.equalsIgnoreQuotingAndCase(schema, defSchema) || schema.equals(defaultSchemaName)) {
					hasDS = true;
				}
			}
			if (!hasDS) {
				modelSchemas.add(defaultSchemaName);
			}
			SortedSet<String> dbSchemas = new TreeSet<String>(JDBCMetaDataBasedModelElementFinder.getSchemas(
					session, connectionDialog.currentConnection.user));
			dbSchemas.add(defaultSchemaName);
			int y = 1;
			for (String schema: modelSchemas) {
				GridBagConstraints gridBagConstraints;
				JComboBox2 comboBox = new JComboBox2();
				comboBox.setModel(new javax.swing.DefaultComboBoxModel(dbSchemas.toArray()));
				gridBagConstraints = new java.awt.GridBagConstraints();
				gridBagConstraints.gridx = 2;
				gridBagConstraints.gridy = y;
				getContentPane().add(comboBox, gridBagConstraints);
				Quoting q = Quoting.getQuoting(session);
				String v = mapping.containsKey(q.unquote(schema))? mapping.get(q.unquote(schema)) : mapping.containsKey(q.requote(schema))? mapping.get(q.requote(schema)) : schema;
				if (schema.equals(defaultSchemaName)) {
					v = mapping.containsKey("")? mapping.get("") : mapping.containsKey(q.unquote(defSchema))? mapping.get(q.unquote(defSchema)) : mapping.containsKey(q.requote(defSchema))? mapping.get(q.requote(defSchema)) : schema;
				}
				if (dbSchemas.contains(v)) {
					comboBox.setSelectedItem(v);
				} else if (dbSchemas.contains(q.requote(v))) {
					comboBox.setSelectedItem(q.requote(v));
				} else if (dbSchemas.contains(q.unquote(v))) {
					comboBox.setSelectedItem(q.unquote(v));
				} else if (dbSchemas.contains(v.toUpperCase(Locale.ENGLISH))) {
					comboBox.setSelectedItem(v.toUpperCase(Locale.ENGLISH));
				} else if (dbSchemas.contains(v.toLowerCase(Locale.ENGLISH))) {
					comboBox.setSelectedItem(v.toLowerCase(Locale.ENGLISH));
				} else {
					comboBox.setSelectedItem(defaultSchemaName);
				}
				
				comboboxes.put(schema, comboBox);
				
				JLabel label = new JLabel();
				label.setFont(label.getFont().deriveFont(label.getFont().getSize()+2f));
		        label.setText(Quoting.getQuoting(session).unquote(schema) + (schema.equals(defaultSchemaName)? " (default)" : ""));
				gridBagConstraints = new java.awt.GridBagConstraints();
				gridBagConstraints.gridx = 4;
				gridBagConstraints.gridy = y;
				gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
				gridBagConstraints.weightx = 1.0;
				getContentPane().add(label, gridBagConstraints);
				++y;
			}
		} finally {
			if (windowAncestor != null) {
				UIUtil.resetWaitCursor(windowAncestor);
			}
		}
		pack();
		UIUtil.setInitialWindowLocation(this, parent, 300, 100);
		setVisible(true);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {
		java.awt.GridBagConstraints gridBagConstraints;

		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		jPanel1 = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		jButton2 = new javax.swing.JButton();
		jLabel4 = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Schema Mapping");
		getContentPane().setLayout(new java.awt.GridBagLayout());

		jLabel1.setText("  Read from  ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;
		getContentPane().add(jLabel1, gridBagConstraints);

		jLabel2.setText("  instead of  ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 3;
		gridBagConstraints.gridy = 1;
		getContentPane().add(jLabel2, gridBagConstraints);

		jPanel1.setLayout(new java.awt.GridBagLayout());

		okButton.setText(" Ok ");
		okButton.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				okButtonActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 0);
		jPanel1.add(okButton, gridBagConstraints);

		jButton2.setText(" Cancel ");
		jButton2.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton2ActionPerformed(evt);
			}
		});
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
		jPanel1.add(jButton2, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 200;
		gridBagConstraints.gridwidth = 4;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		getContentPane().add(jPanel1, gridBagConstraints);

		jLabel4.setText("   ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		getContentPane().add(jLabel4, gridBagConstraints);

		jLabel5.setText("   ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 199;
		getContentPane().add(jLabel5, gridBagConstraints);

		jLabel3.setText("  ");
		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 5;
		gridBagConstraints.gridy = 1;
		getContentPane().add(jLabel3, gridBagConstraints);

		pack();
	}// </editor-fold>//GEN-END:initComponents

	private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		setVisible(false);
	}//GEN-LAST:event_jButton2ActionPerformed

	private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
		ok = true;
		setVisible(false);
	}//GEN-LAST:event_okButtonActionPerformed

	/**
	 * Gets current mapping, or <code>null</code> if dialog was canceled.
	 * 
	 * @return current mapping, or <code>null</code> if dialog was canceled
	 */
	public Map<String, String> getMapping() {
		if (ok) {
			Map<String, String> mapping = new TreeMap<String, String>();
			for (Map.Entry<String, JComboBox2> e: comboboxes.entrySet()) {
				String selection = (String) e.getValue().getSelectedItem();
				if (defaultSchemaName.equals(selection)) {
					selection = "";
				}
				mapping.put(defaultSchemaName.equals(e.getKey())? "" : e.getKey(), selection);
			}
			return mapping;
		}
		return null;
	}
	
	private final static String MAPPINGS_FILE = ".browserschemamaps.ser";

	/**
	 * Restores all persisted schema mappings.
	 * 
	 * @return all persisted schema mappings
	 */
	private static Map<String, Map<String, String>> restore() {
		// temporarily disabled
//		try {
//			File file = Environment.newFile(MAPPINGS_FILE);
//			ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
//			@SuppressWarnings("unchecked")
//			Map<String, Map<String, String>> mappings = (Map<String, Map<String, String>>) in.readObject();
//			in.close();
//			return mappings;
//		} catch (Exception e) {
			return new TreeMap<String, Map<String,String>>();
//		}
	}

	/**
	 * Restores all persisted schema mappings.
	 * 
	 * @param connectionDialog to get mapping key
	 * @return all persisted schema mappings
	 */
	public static Map<String, String> restore(DbConnectionDialog connectionDialog) {
		if (connectionDialog.currentConnection == null) {
			return new TreeMap<String, String>();
		}
		Map<String, String> mapping = restore().get(connectionDialog.currentConnection.user + "@" + connectionDialog.currentConnection.url);
		if (mapping == null) {
			return new TreeMap<String, String>();
		}
		return mapping;
	}

	/**
	 * Stores a schema mapping.
	 * 
	 * @param mapping the mapping
	 * @param connectionDialog to get mapping key
	 */
	public static void store(Map<String, String> mapping, DbConnectionDialog connectionDialog) {
		try {
			Map<String, Map<String, String>> mappings = restore();
			File file = Environment.newFile(MAPPINGS_FILE);
			file.delete();
			mappings.put(connectionDialog.currentConnection.user + "@" + connectionDialog.currentConnection.url, mapping);
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file)); // lgtm [java/output-resource-leak]
			out.writeObject(mappings);
			out.close();
		} catch (Exception e) {
			// ignore
		}
	}
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButton2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JPanel jPanel1;
	private javax.swing.JButton okButton;
	// End of variables declaration//GEN-END:variables
	
	private static ImageIcon okIcon;
	private static ImageIcon cancelIcon;
	
	static {
        // load images
        okIcon = UIUtil.readImage("/buttonok.png");
        cancelIcon = UIUtil.readImage("/buttoncancel.png");
	}
}
