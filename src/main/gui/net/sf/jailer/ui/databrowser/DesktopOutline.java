/*
 * Copyright 2007 - 2021 Ralf Wisser.
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import net.sf.jailer.ui.UIUtil;
import net.sf.jailer.ui.databrowser.Desktop.RowBrowser;

/**
 * Desktop outline.
 * 
 * @author Ralf Wisser
 */
@SuppressWarnings("serial")
public class DesktopOutline extends JPanel {
	
	private final JPanel outLinePanel;
	private final Desktop desktop;
	private final JScrollPane scrollPane;
	private Point draggingStart = null;
	private Point draggingViewPosition = null;
	public Rectangle visibleRectInOutline = null;
	
	public DesktopOutline(JPanel outLinePanel, JScrollPane scrollPane, Desktop desktop) {
		this.outLinePanel = outLinePanel;
		this.scrollPane = scrollPane;
		this.desktop = desktop;
		setOpaque(false);
		addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				stopDragging();
				RowBrowser browser = findBrowser(e);
				if (browser == null) {
					setToolTipText(null);
				} else {
					setToolTipText(browser.internalFrame.getTitle());
				}
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (draggingStart == null) {
					startDragging(e);
				}
				setDragViewPosition(scrollPane, desktop, e);
			}
		});
		
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				stopDragging();
				RowBrowser browser = findBrowser(e);
				if (e.getButton() == MouseEvent.BUTTON3) {
					if (browser != null) {
							try {
							browser.internalFrame.setSelected(true);
						} catch (PropertyVetoException e1) {
							// ignore
						}
					}
				    showPopupMenu(desktop, e, browser);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				RowBrowser browser = findBrowser(e);
				if (e.getButton() != MouseEvent.BUTTON3 || browser != null) {
					centeredDragging(e);
				}
				stopDragging();
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				RowBrowser browser = findBrowser(e);
				if (e.getButton() != MouseEvent.BUTTON3 || browser != null) {
					centeredDragging(e);
				}
				stopDragging();
				if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
					if (browser != null) {
						try {
							browser.internalFrame.setSelected(true);
						} catch (PropertyVetoException e1) {
							// ignore
						}
					}
					if (e.getButton() == MouseEvent.BUTTON3) {
	                    showPopupMenu(desktop, e, browser);
					}
				}
			}

			private void showPopupMenu(Desktop desktop, MouseEvent e, RowBrowser browser) {
				if (browser == null) {
					desktop.openGlobalPopup(e);
					return;
				}
				JPopupMenu popup = browser.browserContentPane.createPopupMenu(null, -1, 0, 0, false);
				if (popup != null) {
				    JPopupMenu popup2 = browser.browserContentPane.createSqlPopupMenu(-1, 0, 0, true, desktop);
				    if (popup2.getComponentCount() > 0 && popup.getComponentCount() > 0) {
				         popup.add(new JSeparator());
				    }
				    for (Component c : popup2.getComponents()) {
				        popup.add(c);
				    }
				    UIUtil.fit(popup);
				    UIUtil.showPopup(e.getComponent(), e.getX(), e.getY(), popup);
				}
			}
		});
	}
	
	private void startDragging(MouseEvent e) {
		draggingStart = e.getPoint();
		draggingViewPosition = scrollPane.getViewport().getViewPosition();
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	private void stopDragging() {
		draggingStart = null;
		draggingViewPosition = null;
		setCursor(null);
	}
	
	private void centeredDragging(MouseEvent e) {
		if (visibleRectInOutline != null) {
			double wb = Math.min(visibleRectInOutline.width / 2 - 2, Desktop.BROWSERTABLE_DEFAULT_WIDTH * desktop.layoutMode.factor * scale / 2);
			double wh = Math.min(visibleRectInOutline.height / 2 - 2, Desktop.BROWSERTABLE_DEFAULT_HEIGHT * desktop.layoutMode.factor * scale / 2);
			Rectangle r = new Rectangle(visibleRectInOutline.x + (int) wb, visibleRectInOutline.y + (int) wh, visibleRectInOutline.width - (int) (2 * wb), visibleRectInOutline.height - (int) (2 * wh));
			if (draggingStart == null && !r.contains(e.getPoint())) {
				startDragging(e);
				draggingStart = new Point((int) r.getCenterX(), (int) r.getCenterY());
				setDragViewPosition(scrollPane, desktop, e);
				stopDragging();
			}
		}
	}

	private double scale;
	private double offX;
	private double offY;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (g instanceof Graphics2D) {
			if (desktop.getWidth() == 0 || desktop.getHeight() == 0) {
				return;
			}
			Graphics2D g2d = (Graphics2D) g;
			FontMetrics fontMetrics = g2d.getFontMetrics();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			double r = 6;
			scale = Math.min(((double) outLinePanel.getWidth() - r * 2.0) / (double) desktop.getWidth(), ((double) (outLinePanel.getHeight() - r * 2.0) / (double) desktop.getHeight()));
			offX = r;
			offY = r - 1; // Math.max(0, (outLinePanel.getHeight() - outlineSize.getHeight()) / 2);
			BasicStroke stroke = new BasicStroke();
			double border = 4 / scale;
			double x = -border;
			double y = -border;
			double width = desktop.getWidth() + 2 * border;
			double height = desktop.getHeight() + 2 * border;
			Color borderColor = Color.GRAY;
			Color backgroundColor = new Color(236, 236, 255);
			g2d.setColor(backgroundColor);
			int gx = (int) (offX + scale * x + 0.5);
			int gy = (int)(offY + scale * y + 0.5);
			int gw = snap((int) (outLinePanel.getWidth() - (offX + scale * x + 0.5) - 1), (int)(scale * width + 0.5), 32);
			int gh = (int)(scale * height + 0.5);
			GradientPaint paint = new GradientPaint(
					0, 0, backgroundColor,
					gw, gh, backgroundColor.brighter());
			g2d.setPaint(paint);
			g2d.fillRoundRect(gx, gy, gw, gh, 2, 2);
			g2d.setColor(borderColor);
			g2d.setStroke(stroke);
			g2d.drawRoundRect(gx, gy, gw, gh, 2, 2);
			
			g2d.setStroke(new BasicStroke(2));
			for (RowBrowser browser: getBrowsers()) {
				if (!browser.isHidden()) {
					RowBrowser parentBrowser = browser.parent;
					boolean hiddenParent = false;
					while (parentBrowser != null && parentBrowser.isHidden()) {
						parentBrowser = parentBrowser.parent;
						hiddenParent = true;
					}
					if (parentBrowser != null && !parentBrowser.isHidden()) {
						if (browser.association == null) {
							g2d.setColor(Color.GRAY);
						} else if (hiddenParent) {
							g2d.setColor(Color.yellow.darker());
						} else {
							g2d.setColor(desktop.getAssociationColor1(browser.association));
						}
						g2d.drawLine((int) (offX + scale * browser.internalFrame.getBounds().getCenterX() + 0.5), (int)(offY + scale * browser.internalFrame.getBounds().getCenterY() + 0.5), (int) (offX + scale * parentBrowser.internalFrame.getBounds().getCenterX() + 0.5), (int)(offY + scale * parentBrowser.internalFrame.getBounds().getCenterY() + 0.5));
					}
				}
			}
			for (RowBrowser browser: getBrowsers()) {
				if (!browser.isHidden()) {
					Rectangle rectangle = subBorder(browser.internalFrame.getBounds());
					Color backgroundColor1 = new Color(160, 200, 255);
					int sx = (int)(offX + scale * (double) rectangle.x + 0.5);
					int sy = (int)(offY + scale * (double) rectangle.y + 0.5);
					int sw = (int)(scale * (double) rectangle.width + 0.5);
					int sh = (int)(scale * (double) rectangle.height + 0.5);
					if (backgroundColor1 != null) {
						g2d.setColor(backgroundColor1);
						paint = new GradientPaint(
								sx, sy, backgroundColor1,
								sx + sw, sy + sh, backgroundColor1.brighter());
						g2d.setPaint(paint);
						g2d.fillRoundRect(sx, sy, sw, sh, 8, 8);
					}
					g2d.setColor(Color.black);
					Rectangle clip = g2d.getClipBounds();
					g2d.setClip(sx, sy, sw, sh);
					String title = " " + browser.internalFrame.getTitle();
					Rectangle2D stringBounds = fontMetrics.getStringBounds(title, g2d);
					g2d.drawString(title, (int)(offX + scale * rectangle.x + Math.max(0, (sw - stringBounds.getWidth()) / 2)), (int)(offY + scale * rectangle.y + stringBounds.getHeight() * 1.2));
					g2d.setClip(clip);
				}
				if (!browser.isHidden() && browser.internalFrame.isSelected()) {
					Rectangle rectangle = subBorder(browser.internalFrame.getBounds());
					paintRect(g2d, rectangle.x, rectangle.y, rectangle.width, rectangle.height, new Color(0, 100, 255), null, new BasicStroke(2));
				}
			}
			Rectangle rectangle = desktop.getVisibleRect();
			Color borderColor1 = new Color(0, 0, 200);
			int sx = (int)(offX + scale * (double) rectangle.x + 0.5);
			int sy = (int)(offY + scale * (double) rectangle.y + 0.5);
			int sw = (int)(scale * (double) (rectangle.width + 6) + 0.5);
			int sh = (int)(scale * (double) rectangle.height + 0.5);
			visibleRectInOutline = new Rectangle(sx, sy, sw, sh);
			if (borderColor1 != null) {
				g2d.setColor(borderColor1);
				g2d.setStroke(new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), new float[] { 11f, 5f }, (float) (System.currentTimeMillis() / 50.0 % 16)));
				g2d.drawRoundRect(sx, sy, sw, sh, 8, 8);
			}
		}
	}

	private List<RowBrowser> getBrowsers() {
		List<RowBrowser> browsers = desktop.getBrowsers();
		for (RowBrowser b: browsers) {
			if (!b.isHidden() && b.internalFrame.isSelected()) {
				if (b.internalFrame.isMaximum()) {
					browsers.clear();
				}
				browsers.remove(b);
				browsers.add(b);
				break;
			}
		}
		return browsers;
	}

	private int snap(int a, int b, int minDist) {
		if (Math.abs(a - b) < minDist) {
			return a;
		}
		return b;
	}

	@Override
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	@Override
	public Dimension getMinimumSize() {
		if (desktop.getWidth() == 0 || desktop.getHeight() == 0) {
			return new Dimension(1, 220);
		}
		double r = 6;
		return new Dimension(1, (int) (Math.min(220, (((double) outLinePanel.getWidth() - r * 2.0) / (double) desktop.getWidth() * (double) desktop.getHeight())) + r * 2.0));
	}

	private Rectangle subBorder(Rectangle rect) {
		return subBorder(rect, 32);
	}
	
	private Rectangle subBorder(Rectangle rect, int border) {
		return new Rectangle(rect.x + border, rect.y + border, rect.width - 2 * border, rect.height - 2 * border);
	}

	private void paintRect(Graphics2D g, double x, double y, double width, double height, Color borderColor, Color backgroundColor, BasicStroke stroke) {
		int sx = (int)(offX + scale * x + 0.5);
		int sy = (int)(offY + scale * y + 0.5);
		int sw = (int)(scale * width + 0.5);
		int sh = (int)(scale * height + 0.5);
		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			GradientPaint paint = new GradientPaint(
					sx, sy, backgroundColor,
					sx + sw, sy + sh, backgroundColor.brighter());
			g.setPaint(paint);
			g.fillRoundRect(sx, sy, sw, sh, 8, 8);
		}
		if (borderColor != null) {
			g.setColor(borderColor);
			g.setStroke(stroke);
			g.drawRoundRect(sx, sy, sw, sh, 8, 8);
		}
	}

	private RowBrowser findBrowser(MouseEvent e) {
		RowBrowser browser = null;
		for (RowBrowser b: getBrowsers()) {
			if (!b.isHidden()) {
				Rectangle rectangle = subBorder(b.internalFrame.getBounds());
				int sx = (int)(offX + scale * (double) rectangle.x + 0.5);
				int sy = (int)(offY + scale * (double) rectangle.y + 0.5);
				int sw = (int)(scale * (double) rectangle.width + 0.5);
				int sh = (int)(scale * (double) rectangle.height + 0.5);
				if (e.getPoint().getX() < sx + sw && e.getPoint().getX() >= sx) {
					if (e.getPoint().getY() < sy + sh && e.getPoint().getY() >= sy) {
						browser = b;
					}
				}
			}
		}
		return browser;
	}

	public void setDragViewPosition(JScrollPane scrollPane, Desktop desktop, MouseEvent e) {
		int minDist = 40;
		Point p = new Point(
				(int) Math.max(0, Math.min(desktop.getWidth() - desktop.getVisibleRect().width, draggingViewPosition.x + (e.getPoint().x - draggingStart.x) / scale)), 
				(int) Math.max(0, Math.min(desktop.getHeight() - desktop.getVisibleRect().height, draggingViewPosition.y + (e.getPoint().y - draggingStart.y) / scale)));
		scrollPane.getViewport().setViewPosition(
				new Point(
						snap(desktop.getWidth() - desktop.getVisibleRect().width, snap(0, p.x, minDist), minDist),
						snap(desktop.getHeight() - desktop.getVisibleRect().height, snap(0, p.y, minDist), minDist))
				);
	}

}