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
import java.awt.Point;
import java.awt.Window;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.sql.DataSource;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.sf.jailer.configuration.DBMS;
import net.sf.jailer.database.Session;
import net.sf.jailer.modelbuilder.JDBCMetaDataBasedModelElementFinder;
import net.sf.jailer.util.CancellationHandler;
import net.sf.jailer.util.LogUtil;
import net.sf.jailer.util.Quoting;

/**
 * Specialized {@link Session} for the UI.
 * 
 * @author Ralf Wisser
 */
public class SessionForUI extends Session {

	/**
	 * Creates a new session.
	 * 
	 * @param dataSource the data source
	 * @param dbms the DBMS
	 */
	public static SessionForUI createSession(DataSource dataSource, DBMS dbms, Integer isolationLevel, boolean shutDownImmediatelly, boolean initInlineViewStyle, final Window w) throws SQLException {
		return createSession(dataSource, dbms, isolationLevel, shutDownImmediatelly, initInlineViewStyle, false, w, null);
	}
	
	/**
	 * Creates a new session.
	 * 
	 * @param dataSource the data source
	 * @param dbms the DBMS
	 */
	public static SessionForUI createSession(DataSource dataSource, DBMS dbms, Integer isolationLevel, boolean shutDownImmediatelly, boolean initInlineViewStyle, boolean testOnly, Window w, Consumer<Session> sessionInitializer) throws SQLException {
		Session.setThreadSharesConnection();
		final SessionForUI session = new SessionForUI(dataSource, dbms, isolationLevel, shutDownImmediatelly, testOnly);
		final AtomicReference<Connection> con = new AtomicReference<Connection>();
		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();
		Window owner = w;
		if (!w.isShowing()) {
			for (Window vw: Window.getWindows()) {
				if (vw.isShowing() && vw.isFocused()) {
					owner = vw;
					break;
				}
			}
		}
		session.connectionDialog = new JDialog(owner, "Connecting");
		session.connectionDialog.setUndecorated(true);
		session.connectionDialog.setModal(true);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Session.setThreadSharesConnection();
				CancellationHandler.reset(null);
				try {
					Connection newCon = session.connectionFactory.getConnection();
					try {
						if (session.dbUrl.matches("jdbc:h2:.*demo-scott")) {
							session.executeQuery("Select 1 where not exists(select 1 From DEPARTMENT) and not exists(select 1 From EMPLOYEE) and not exists(select 1 From PROJECT) and not exists(select 1 From PROJECT_PARTICIPATION) and not exists(select 1 From SALARYGRADE) and not exists(select 1 From ROLE)"
									, new ResultSetReader() {
								@Override
								public void readCurrentRow(ResultSet resultSet) throws SQLException {
									LogUtil.warn(new RuntimeException("empty demo db. " + session.dbUrl));
								}
								@Override
								public void close() throws SQLException {
								}
							});
						}
					} catch (Throwable t) {
						// ignore
					}
					if (testOnly) {
						newCon.close();
					} else {
						Quoting.getQuoting(session);
						if (initInlineViewStyle && !session.cancelled.get()) {
							if (session.getInlineViewStyle() == null) {
								session.setSessionProperty(SessionForUI.class, SUPPORT_WC_EDITOR, false);
							}
						}
						if (sessionInitializer != null) {
							sessionInitializer.accept(session);
						}
						if (!session.cancelled.get()) {
							String defSchema = JDBCMetaDataBasedModelElementFinder.getDefaultSchema(session, session.getSchema());
							session.setSessionProperty(SessionForUI.class, "defSchema", defSchema);
							List<String> schemas = JDBCMetaDataBasedModelElementFinder.getSchemas(session, session.getSchema());
							session.setSessionProperty(SessionForUI.class, "schemas", schemas);
						}
					}
					if (session.cancelled.get()) {
						try {
							newCon.close();
						} catch (SQLException e) {
							// ignore
						}
					} else {
						con.set(newCon);
					}
				} catch (Throwable e) {
					exception.set(e);
				}
				UIUtil.invokeLater(new Runnable() {
					@Override
					public void run() {
						session.connectionDialog.setVisible(false);
					}
				});
			}
		});
		thread.setDaemon(true);
		thread.start();
		Point p;
		if (owner.isShowing()) {
			p = owner.getLocationOnScreen();
		} else {
			p = new Point(0, 0);
		}
		final Point los = p;
		
		if (fadeStart == null || (System.currentTimeMillis() - fadeStart) > 2000) {
			startFadeIn();
		}
		
		if (fadeStart != null && !(w instanceof DataModelManagerDialog) && !(w instanceof Dialog)) {
			int fadeTime = 800;
			if (fadeTimer != null) {
				fadeTimer.stop();
			}
			fadeTimer = new Timer(50, e -> {
				if (fadeStart != null) {
					long dif = System.currentTimeMillis() - fadeStart;
					float h = Math.max(0f, Math.min(1f, -0.5f + dif / (float) fadeTime));
					UIUtil.setOpacity(session.connectionDialog, h);
					if (h < 1) {
						return;
					}
					fadeTimer.stop();
				}
			});
			fadeTimer.setInitialDelay(1);
			fadeTimer.setRepeats(true);
			fadeTimer.start();
			UIUtil.setOpacity(session.connectionDialog, 0f);
		}
		
		session.connectionDialog.getContentPane().add(session.connectingPanel);
		session.connectionDialog.pack();
		session.connectionDialog.setLocation(los.x + owner.getWidth() / 2 - session.connectionDialog.getWidth() / 2, los.y + owner.getHeight() / 2 - session.connectionDialog.getHeight() / 2);
        
		Timer timer = new Timer(500, e -> {
			session.connectingPanel.setBackground(Colors.Color_255_255_255.equals(session.connectingPanel.getBackground())? Colors.Color_255_230_230 : Colors.Color_255_255_255);
        });
        timer.setRepeats(true);
        timer.setInitialDelay(timer.getDelay() * 3);
        timer.start();
        
        session.connectionDialog.setVisible(true);
		session.connectionDialog.dispose();
		timer.stop();
		
		Throwable throwable = exception.get();
		if (throwable  != null) {
			if (throwable instanceof SQLException) {
				throw (SQLException) throwable;
			}
			if (throwable instanceof RuntimeException) {
				throw (RuntimeException) throwable;
			}
			throw new RuntimeException(throwable);
		}
		if (con.get() != null) {
			session.setConnection(con.get());
			return session;
		}
		return null;
	}

	private boolean testOnly;
	private JPanel connectingPanel = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JButton cancelConnectingButton = new JButton();
	private AtomicBoolean cancelled = new AtomicBoolean(false);
	private JDialog connectionDialog;

	private static Long fadeStart;
	private static Timer fadeTimer;
	
	public static void startFadeIn() {
		fadeStart = System.currentTimeMillis();
	}
	
	/**
	 * Constructor.
	 * @param shutDownImmediatelly 
	 */
	private SessionForUI(DataSource dataSource, DBMS dbms, Integer isolationLevel, boolean shutDownImmediatelly, boolean testOnly) throws SQLException {
		super(dataSource, dbms, isolationLevel);
		this.shutDownImmediatelly = shutDownImmediatelly;
		this.testOnly = testOnly;
		connectingPanel.setBackground(Colors.Color_255_255_255);
		connectingPanel.setBorder(BorderFactory.createLineBorder(Colors.Color_64_64_64));
		
//        jLabel1.setForeground(Colors.Color_red);
        jLabel1.setText("Connecting...");
        jLabel1.setFont(jLabel1.getFont().deriveFont((float) (jLabel1.getFont().getSize() * 1.4)));
        connectingPanel.add(jLabel1);
        cancelConnectingButton.setText("Cancel");
        cancelConnectingButton.setIcon(UIUtil.scaleIcon(cancelConnectingButton, cancelIcon));
        cancelConnectingButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	cancelled.set(true);
            	connectionDialog.setVisible(false);
            }
        });
        connectingPanel.add(cancelConnectingButton);
	}

	@Override
	protected void init() throws SQLException {
	}
	
	private final boolean shutDownImmediatelly;

	/**
	 * Closes all connections asynchronously.
	 */
	@Override
	public void shutDown() {
		down.set(true);
		if (!testOnly) {
			if (shutDownImmediatelly) {
				try {
					super.shutDown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Thread thread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							SessionForUI.super.shutDown();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				thread.setDaemon(true);
				thread.start();
			}
		}
	}
	
	private static final String SUPPORT_WC_EDITOR = "supportWCEditor";

    public static boolean isWCEditorSupported(Session session) {
    	return !Boolean.FALSE.equals(session.getSessionProperty(SessionForUI.class, SUPPORT_WC_EDITOR));
    }
	
	private static ImageIcon cancelIcon;
	
	static {
        // load images
        cancelIcon = UIUtil.readImage("/buttoncancel.png");
	}

}
