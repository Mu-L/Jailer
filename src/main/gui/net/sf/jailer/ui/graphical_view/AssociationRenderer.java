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
package net.sf.jailer.ui.graphical_view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import net.sf.jailer.datamodel.AggregationSchema;
import net.sf.jailer.datamodel.Association;
import net.sf.jailer.datamodel.Cardinality;
import net.sf.jailer.datamodel.DataModel;
import net.sf.jailer.subsetting.ScriptFormat;
import net.sf.jailer.ui.Colors;
import net.sf.jailer.ui.UIUtil;
import prefuse.Constants;
import prefuse.render.EdgeRenderer;
import prefuse.util.GraphicsLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

/**
 * Renderer for {@link Association}s.
 *
 * @author Ralf Wisser
 */
public class AssociationRenderer extends EdgeRenderer {

	// color setting
	public static Color COLOR_IGNORED;
	public static Color COLOR_ASSOCIATION;
	public static Color COLOR_DEPENDENCY;
	public static Color COLOR_REVERSE_DEPENDENCY;

	/**
	 * <code>true</code> for reversed rendering.
	 */
	boolean reversed;

	/**
	 * <code>true</code> for full rendering (for setting bounds).
	 */
	boolean full = false;

	private final DataModel dataModel;

	/**
	 * Constructor.
	 *
	 * @param reversed <code>true</code> for reversed rendering
	 */
	public AssociationRenderer(DataModel dataModel, boolean reversed) {
		super(Constants.EDGE_TYPE_LINE, reversed? Constants.EDGE_ARROW_REVERSE : Constants.EDGE_ARROW_FORWARD);
		this.dataModel = dataModel;
		this.reversed = reversed;
	}

	/**
	 * Constructor.
	 */
	public AssociationRenderer(DataModel dataModel) {
		this.dataModel = dataModel;
		full = true;
	}

	/**
	 * Temporary used in getRawShape.
	 */
	private Point2D m_isctPoints2[] = new Point2D[2];
	private Point2D starPosition = null;
	private Point2D midPosition = null;
	private Point2D pendingPosition = null;
	private double starTheta;

	/**
	 * Return a non-transformed shape for the visual representation of the
	 * {@link Association}.
	 *
	 * @param item the VisualItem being drawn
	 * @return the "raw", untransformed shape
	 */
	@Override
	protected Shape getRawShape(VisualItem item) {
		EdgeItem   edge = (EdgeItem)item;
		VisualItem item1 = edge.getSourceItem();
		VisualItem item2 = edge.getTargetItem();

		int type = m_edgeType;
		boolean reversedCurve = false;
		Association association = (Association) item.get("association");
		if (association != null && association.source == association.destination) {
			type = Constants.EDGE_TYPE_CURVE;
			reversedCurve = association.reversed;
		}

		getAlignedPoint(m_tmpPoints[0], item1.getBounds(),
						m_xAlign1, m_yAlign1);
		getAlignedPoint(m_tmpPoints[1], item2.getBounds(),
						m_xAlign2, m_yAlign2);
		m_curWidth = (float)(m_width * getLineWidth(item));
		EdgeItem e = (EdgeItem)item;

		boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);

		// get starting and ending edge endpoints
		Point2D start = null, end = null;
		start = m_tmpPoints[forward?0:1];
		end   = m_tmpPoints[forward?1:0];

		if (!full) {
			double midX;
			double midY;
			Point2D sp = start, ep = end;

			VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
			int i = GraphicsLib.intersectLineRectangle(start, end,
					dest.getBounds(), m_isctPoints);
			if ( i > 0 ) ep = m_isctPoints[0];

			VisualItem src = !forward ? e.getTargetItem() : e.getSourceItem();
			i = GraphicsLib.intersectLineRectangle(start, end,
					src.getBounds(), m_isctPoints2);
			if ( i > 0 ) sp = m_isctPoints2[0];

			midX = (sp.getX() + ep.getX()) / 2;
			midY = (sp.getY() + ep.getY()) / 2;
			m_tmpPoints[reversed? 1 : 0].setLocation(midX, midY);
		}

		// create the arrow head, if needed
		if ( e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE) {
			if (type == Constants.EDGE_TYPE_CURVE) {
				AffineTransform t = new AffineTransform();
				t.setToRotation(Math.PI/4 * (reversedCurve? 1 : -1));
				Point2D p = new Point2D.Double(), shift = new Point2D.Double();
				double d = start.distance(end) / 5.0;
				p.setLocation((end.getX() - start.getX()) / d, (end.getY() - start.getY()) / d);
				t.transform(p, shift);
				start.setLocation(start.getX() + shift.getX(), start.getY() + shift.getY());
				end.setLocation(end.getX() + shift.getX(), end.getY() + shift.getY());
			}

			// compute the intersection with the target bounding box
			VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
			int i = GraphicsLib.intersectLineRectangle(start, end,
					dest.getBounds(), m_isctPoints);
			if ( i > 0 ) end = m_isctPoints[0];

			// create the arrow head shape
			AffineTransform at = getArrowTrans(start, end, m_curWidth);
			m_curArrow = at.createTransformedShape(m_arrowHead);

			// update the endpoints for the edge shape
			// need to bias this by arrow head size
			if (type == Constants.EDGE_TYPE_CURVE) {
				if (association == null || !isAggregation(association) || !isObjectNatationFormat(association)) {
					m_curArrow = null;
				}
			}
			Point2D lineEnd = m_tmpPoints[forward?1:0];
			lineEnd.setLocation(0, type == Constants.EDGE_TYPE_CURVE? 0 : -m_arrowHeight);
			at.transform(lineEnd, lineEnd);
		} else {
			m_curArrow = null;
		}

		// create the edge shape
		Shape shape = null;
		double n1x = m_tmpPoints[0].getX();
		double n1y = m_tmpPoints[0].getY();
		double n2x = m_tmpPoints[1].getX();
		double n2y = m_tmpPoints[1].getY();
		m_line.setLine(n1x, n1y, n2x, n2y);
		shape = m_line;
		
		if (association == null) {
			return shape;
		}

		starBounds = null;
		starPosition = null;
		starTheta = 0;
		midPosition = new Point2D.Double((n1x + n2x) / 2, (n1y + n2y) / 2);

		if (!forward && (Cardinality.MANY_TO_MANY.equals(association.getCardinality()) || Cardinality.MANY_TO_ONE.equals(association.getCardinality()))
		||   forward && (Cardinality.MANY_TO_MANY.equals(association.getCardinality()) || Cardinality.ONE_TO_MANY.equals(association.getCardinality()))) {
			starPosition = new Point2D.Double(m_tmpPoints[forward? 1:0].getX(), m_tmpPoints[forward? 1:0].getY());
			start = starPosition;
			end = m_tmpPoints[forward? 0:1];
			AffineTransform t = new AffineTransform();
			t.setToRotation(-Math.PI/4.5);
			Point2D p = new Point2D.Double(), shift = new Point2D.Double();
			double d = m_tmpPoints[0].distance(m_tmpPoints[1]) / 9.0;
			p.setLocation((end.getX() - start.getX()) / d, (end.getY() - start.getY()) / d);
			t.transform(p, shift);
			starTheta = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
			starPosition.setLocation(starPosition.getX() + shift.getX(), starPosition.getY() + shift.getY());
			starBounds = new Rectangle2D.Double(starPosition.getX() - STAR_SIZE * (starWidth / 2), starPosition.getY() - STAR_SIZE * (starHeight / 2), starWidth * STAR_SIZE, starHeight * STAR_SIZE);
		}

		pendingBounds = null;
		pendingPosition = null;

		if (!forward && association.getDataModel().decisionPending.contains(association.getName())
		||   forward && association.getDataModel().decisionPending.contains(association.reversalAssociation.getName())) {
			pendingPosition = new Point2D.Double(m_tmpPoints[forward? 1:0].getX(), m_tmpPoints[forward? 1:0].getY());
			start = pendingPosition;
			end = m_tmpPoints[forward? 0:1];
			Point2D p = new Point2D.Double(), shift = new Point2D.Double();
			double d = 1.11;
			p.setLocation((end.getX() - start.getX()) / d, (end.getY() - start.getY()) / d);
			shift = p;
			pendingPosition.setLocation(pendingPosition.getX() + shift.getX(), pendingPosition.getY() + shift.getY());
			pendingBounds = new Rectangle2D.Double(pendingPosition.getX() - PENDING_SIZE * (pendingWidth / 2), pendingPosition.getY() - PENDING_SIZE * (pendingHeight / 2), pendingWidth * PENDING_SIZE, pendingHeight * PENDING_SIZE);
		}

		return shape;
	}

	/**
	 * Returns an affine transformation that maps the arrowhead shape
	 * to the position and orientation specified by the provided
	 * line segment end points.
	 */
	@Override
	protected AffineTransform getArrowTrans(Point2D p1, Point2D p2,
											double width)
	{
		m_arrowTrans.setToTranslation(p2.getX(), p2.getY());
		m_arrowTrans.rotate(-HALF_PI +
			Math.atan2(p2.getY()-p1.getY(), p2.getX()-p1.getX()));
		if ( width > 1 ) {
			double scalar = width/2;
			m_arrowTrans.scale(scalar, scalar);
		}
		return m_arrowTrans;
	}

	/**
	 * Renders an {@link Association}.
	 *
	 * @param g the 2D graphics
	 * @param item visual item for the association
	 * @param isSelected <code>true</code> for selected association
	 */
	public void render(Graphics2D g, VisualItem item, boolean isSelected) {
		Association association = (Association) item.get("association");
		item.setSize(isSelected? 3 : 1);
		int color = 0;
		if (!Boolean.TRUE.equals(item.get("full"))) {
			if (!full) {
				return;
			}
			if (association != null) {
				color = associationColor(association);
			}
		} else {
			if (full) {
				return;
			}
			if (association != null) {
				color = reversed? associationColor(association.reversalAssociation) : associationColor(association);
			}
		}
		boolean restricted = false;
		BasicStroke stroke = item.getStroke();
		if (stroke != null) {
			if (reversed) {
				if (association != null) {
					association = association.reversalAssociation;
				}
			}
			if (association != null && association.isRestricted() && !association.isIgnored()) {
				item.setStroke(new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(),
					new float[] { 8f, 6f }, 1.0f));
				restricted = true;
			} else {
				item.setStroke(new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit()));
			}
		}
		if (isSelected) {
			item.setStrokeColor(Colors.Color_0_0_0.getRGB());
			stroke = item.getStroke();
			if (stroke != null) {
				BasicStroke itemStroke;
				long animationstep = System.currentTimeMillis();
				if (restricted) {
					int length = 20 * 100;
					itemStroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_ROUND, stroke.getLineJoin(), stroke.getMiterLimit(), new float[] { 7f, 6f, 1f, 6f },
							(reversed? animationstep % length : length - animationstep % length) / 100.0f);
				} else {
					int length = 12 * 100;
					itemStroke = new BasicStroke(stroke.getLineWidth(), BasicStroke.CAP_ROUND, stroke.getLineJoin(), stroke.getMiterLimit(), new float[] { 7f, 5f },
						(reversed? animationstep % length : length - animationstep % length) / 100.0f);
				}
				item.setStroke(itemStroke);
			}
		}
		item.setFillColor(color);
		item.setStrokeColor(color);
		if (association != null && isObjectNatationFormat(association)) {
			m_arrowHead = updateArrowHead(m_arrowWidth, m_arrowHeight, association, isSelected);
			arrowIsPotAggregation = true;
		} else {
			if (arrowIsPotAggregation) {
				m_arrowHead = updateArrowHead(m_arrowWidth, m_arrowHeight);
			}
			arrowIsPotAggregation = false;
		}
		starPosition = null;
		pendingPosition = null;
		midPosition = null;
		render(g, item);
		if (starPosition != null && starImage != null) {
			double size = STAR_SIZE;
			AffineTransform t2 = new AffineTransform();
			t2.translate(starWidth / 2, starHeight / 2);
			t2.rotate(starTheta - Math.PI / 8.0);
			t2.translate(-starWidth / 2, -starHeight / 2);
			transform.setTransform(size, 0, 0, size, starPosition.getX() - size * (starWidth / 2), starPosition.getY() - size * (starHeight / 2));
			transform.concatenate(t2);
			g.drawImage(starImage, transform, null);
			starPosition = null;
		}
		if (pendingPosition != null && pendingImage != null) {
			double size = PENDING_SIZE;
			transform.setTransform(size, 0, 0, size, pendingPosition.getX() - size * (pendingWidth / 2), pendingPosition.getY() - size * (pendingHeight / 2));
			g.drawImage(pendingImage, transform, null);
			pendingPosition = null;
		}
		if (midPosition != null) {
			if (dataModel.version != lastDataModelVersion) {
				withNullFK.clear();
				lastDataModelVersion = dataModel.version;
			}
			Boolean isFKNull = withNullFK.get(association);
			if (isFKNull == null) {
				isFKNull = association.isRestrictedDependencyWithNulledFK() && !association.fkHasExcludeFilter();
				withNullFK.put(association, isFKNull);
			}
			if (isFKNull) {
				int r = 5;
				g.setStroke(new BasicStroke(1.5f));
				g.setColor(new Color(color));
				g.drawOval((int) midPosition.getX() - r, (int) midPosition.getY() - r, 2 * r, 2 * r);
			}
		}
	}

	/**
	 * @see prefuse.render.Renderer#setBounds(prefuse.visual.VisualItem)
	 */
	@Override
	public void setBounds(VisualItem item) {
		super.setBounds(item);
		if (starBounds != null ) {
			Rectangle2D bbox = (Rectangle2D)item.get(VisualItem.BOUNDS);
			if (bbox != null) {
				Rectangle2D.union(bbox, starBounds, bbox);
			}
		}
		if (pendingBounds != null ) {
			Rectangle2D bbox = (Rectangle2D)item.get(VisualItem.BOUNDS);
			if (bbox != null) {
				Rectangle2D.union(bbox, pendingBounds, bbox);
			}
		}
	}

	private boolean arrowIsPotAggregation = false;
	private AffineTransform transform = new AffineTransform();
	private Rectangle2D starBounds = null;
	private Rectangle2D pendingBounds = null;
	private long lastDataModelVersion = -1;
	private Map<Association, Boolean> withNullFK = new HashMap<Association, Boolean>();

	/**
	 * Gets color for association.
	 *
	 * @param association the association
	 * @return the color for the association
	 */
	private int associationColor(Association association) {
		if (association.isIgnored()) {
			return COLOR_IGNORED.getRGB();
		}
		if (association.isInsertDestinationBeforeSource()) {
			return COLOR_DEPENDENCY.getRGB();
		}
		if (association.isInsertSourceBeforeDestination()) {
			return COLOR_REVERSE_DEPENDENCY.getRGB();
		}
		return COLOR_ASSOCIATION.getRGB();
	}

	 /**
	 * Returns true if the Point is located inside the extents of the item.
	 * This calculation matches against the exact item shape, and so is more
	 * sensitive than just checking within a bounding box.
	 *
	 * @param p the point to test for containment
	 * @param item the item to test containment against
	 * @return true if the point is contained within the the item, else false
	 */
	@Override
	public boolean locatePoint(Point2D p, VisualItem item) {
		Shape s = getShape(item);
		if ( s == null ) {
			return false;
		} else {
			double width = Math.max(14, getLineWidth(item));
			double halfWidth = width/2.0;
			return s.intersects(p.getX()-halfWidth,
								p.getY()-halfWidth,
								width,width);
		}
	}

	/**
	 * Render aggregation symbols.
	 */
	protected Polygon updateArrowHead(int w, int h, Association association, boolean isSelected) {
		if (isAggregation(association)) {
			if ( m_arrowHead == null ) {
				m_arrowHead = new Polygon();
			} else {
				m_arrowHead.reset();
			}
			double ws = 0.9;
			double hs = 2.0/3.0;
			if (isSelected) {
				ws /= 1.3;
				hs /= 1.3;
			}
			m_arrowHead.addPoint(0, 0);
			m_arrowHead.addPoint((int) (ws*-w), (int) (hs*(-h)));
			m_arrowHead.addPoint( 0, (int) (hs*(-2*h)));
			m_arrowHead.addPoint((int) (ws*w), (int) (hs*(-h)));
			m_arrowHead.addPoint(0, 0);
			return m_arrowHead;
		} else {
			return updateArrowHead(w, h);
		}
	}

	/**
	 * Checks whether association must be rendered as aggregation.
	 *
	 * @param association the association to check
	 * @return <code>true</code> if association must be rendered as aggregation
	 */
	private boolean isAggregation(Association association) {
		return association.reversalAssociation.getAggregationSchema() != AggregationSchema.NONE;
	}

	private boolean isObjectNatationFormat(Association association) {
		try {
			return ScriptFormat.valueOf(association.getDataModel().getExportModus()).isObjectNotation();
		} catch (Exception e) {
			return false;
		}
	}
	
	private Image starImage = null;
	private double starWidth = 0;
	private double starHeight = 0;
	private final double STAR_SIZE = 0.25;
	private Image pendingImage = null;
	private double pendingWidth = 0;
	private double pendingHeight = 0;
	private final double PENDING_SIZE = 0.32;
	{
		// load images
		try {
			starImage = UIUtil.readImage("/star.png").getImage();
			starWidth = starImage.getWidth(null);
			starHeight = starImage.getHeight(null);
		} catch (Throwable t) {
			// ignore
		}
		try {
			pendingImage = UIUtil.readImage("/wanr.png").getImage();
			pendingWidth = pendingImage.getWidth(null);
			pendingHeight = pendingImage.getHeight(null);
		} catch (Throwable t) {
			// ignore
		}
	}

}
