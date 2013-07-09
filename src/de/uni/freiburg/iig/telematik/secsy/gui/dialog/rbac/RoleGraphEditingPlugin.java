package de.uni.freiburg.iig.telematik.secsy.gui.dialog.rbac;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.util.ArrowFactory;


public class RoleGraphEditingPlugin extends AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {

protected String startVertex;
protected Point2D down;

protected CubicCurve2D rawEdge = new CubicCurve2D.Float();
protected Shape edgeShape;
protected Shape rawArrowShape;
protected Shape arrowShape;
protected VisualizationServer.Paintable edgePaintable;
protected VisualizationServer.Paintable arrowPaintable;

protected Set<EdgeAddedListener> listeners = new HashSet<EdgeAddedListener>();

public RoleGraphEditingPlugin() {
    this(MouseEvent.BUTTON1_MASK);
}

/**
 * create instance and prepare shapes for visual effects
 * @param modifiers
 */
public RoleGraphEditingPlugin(int modifiers) {
    super(modifiers);
    rawEdge.setCurve(0.0f, 0.0f, 0.33f, 100, .66f, -50,
            1.0f, 0.0f);
    rawArrowShape = ArrowFactory.getNotchedArrow(20, 16, 8);
    edgePaintable = new EdgePaintable();
    arrowPaintable = new ArrowPaintable();
	this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
}

public void addEdgeAddedListener(EdgeAddedListener listener){
	listeners.add(listener);
}

/**
 * Overridden to be more flexible, and pass events with
 * key combinations. The default responds to both ButtonOne
 * and ButtonOne+Shift
 */
@Override
public boolean checkModifiers(MouseEvent e) {
    return (e.getModifiers() & modifiers) != 0;
}

/**
 * If the mouse is pressed in an empty area, create a new vertex there.
 * If the mouse is pressed on an existing vertex, prepare to create
 * an edge from that vertex to another
 */
@SuppressWarnings("unchecked")
public void mousePressed(MouseEvent e) {
    if(checkModifiers(e)) {
        final VisualizationViewer<String,String> vv =
            (VisualizationViewer<String,String>)e.getSource();
        final Point2D p = e.getPoint();
        GraphElementAccessor<String,String> pickSupport = vv.getPickSupport();
        if(pickSupport != null) {
        	
            final String vertex = pickSupport.getVertex(vv.getModel().getGraphLayout(), p.getX(), p.getY());
            if(vertex != null) { // get ready to make an edge
                startVertex = vertex;
                down = e.getPoint();
                transformEdgeShape(down, down);
                vv.addPostRenderPaintable(edgePaintable);
                transformArrowShape(down, e.getPoint());
                vv.addPostRenderPaintable(arrowPaintable);
            }
        }
        vv.repaint();
    }
}

/**
 * If startVertex is non-null, and the mouse is released over an
 * existing vertex, create an undirected edge from startVertex to
 * the vertex under the mouse pointer. If shift was also pressed,
 * create a directed edge instead.
 */
@SuppressWarnings("unchecked")
public void mouseReleased(MouseEvent e) {
    if(checkModifiers(e)) {
        final VisualizationViewer<String,String> vv = (VisualizationViewer<String,String>)e.getSource();
        final Point2D p = e.getPoint();
        Layout<String,String> layout = vv.getModel().getGraphLayout();
        GraphElementAccessor<String,String> pickSupport = vv.getPickSupport();
        if(pickSupport != null) {
            final String vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
            if(vertex != null && startVertex != null) {
                Graph<String,String> graph = vv.getGraphLayout().getGraph();
                try{
                	if(graph.addEdge(startVertex+"-"+vertex, startVertex, vertex, EdgeType.DIRECTED)){
                		notifyListeners(startVertex, vertex);
                	}
                } catch(Exception ex){
                	ex.printStackTrace();
                }
            }
            vv.repaint();
        }
        startVertex = null;
        down = null;
        vv.removePostRenderPaintable(edgePaintable);
        vv.removePostRenderPaintable(arrowPaintable);
    }
}

private void notifyListeners(String sourceVertex, String targetVertex){
	for(EdgeAddedListener listener: listeners){
		listener.edgeAdded(sourceVertex, targetVertex);
	}
}

/**
 * If startVertex is non-null, stretch an edge shape between
 * startVertex and the mouse pointer to simulate edge creation
 */
@SuppressWarnings("unchecked")
public void mouseDragged(MouseEvent e) {
    if(checkModifiers(e)) {
        if(startVertex != null) {
            transformEdgeShape(down, e.getPoint());
            transformArrowShape(down, e.getPoint());
        }
        VisualizationViewer<String,String> vv =
            (VisualizationViewer<String,String>)e.getSource();
        vv.repaint();
    }
}

/**
 * code lifted from PluggableRenderer to move an edge shape into an
 * arbitrary position
 */
private void transformEdgeShape(Point2D down, Point2D out) {
    float x1 = (float) down.getX();
    float y1 = (float) down.getY();
    float x2 = (float) out.getX();
    float y2 = (float) out.getY();

    AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
    
    float dx = x2-x1;
    float dy = y2-y1;
    float thetaRadians = (float) Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    float dist = (float) Math.sqrt(dx*dx + dy*dy);
    xform.scale(dist / rawEdge.getBounds().getWidth(), 1.0);
    edgeShape = xform.createTransformedShape(rawEdge);
}

private void transformArrowShape(Point2D down, Point2D out) {
    float x1 = (float) down.getX();
    float y1 = (float) down.getY();
    float x2 = (float) out.getX();
    float y2 = (float) out.getY();

    AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);
    
    float dx = x2-x1;
    float dy = y2-y1;
    float thetaRadians = (float) Math.atan2(dy, dx);
    xform.rotate(thetaRadians);
    arrowShape = xform.createTransformedShape(rawArrowShape);
}

/**
 * Used for the edge creation visual effect during mouse drag
 */
class EdgePaintable implements VisualizationServer.Paintable {
    
    public void paint(Graphics g) {
        if(edgeShape != null) {
            Color oldColor = g.getColor();
            g.setColor(Color.black);
            ((Graphics2D)g).draw(edgeShape);
            g.setColor(oldColor);
        }
    }
    
    public boolean useTransform() {
        return false;
    }
}

/**
 * Used for the directed edge creation visual effect during mouse drag
 */
class ArrowPaintable implements VisualizationServer.Paintable {
    
    public void paint(Graphics g) {
        if(arrowShape != null) {
            Color oldColor = g.getColor();
            g.setColor(Color.black);
            ((Graphics2D)g).fill(arrowShape);
            g.setColor(oldColor);
        }
    }
    
    public boolean useTransform() {
        return false;
    }
}
public void mouseClicked(MouseEvent e) {}
public void mouseEntered(MouseEvent e) {
    JComponent c = (JComponent)e.getSource();
    c.setCursor(cursor);
}
public void mouseExited(MouseEvent e) {
    JComponent c = (JComponent)e.getSource();
    c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
}
public void mouseMoved(MouseEvent e) {}
}
