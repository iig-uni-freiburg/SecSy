package de.uni.freiburg.iig.telematik.secsy.gui.dialog.rbac;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;


public class RoleGraphLayout extends AbstractLayout<String,String> {

	private double radius;
	private List<String> vertex_ordered_list;
	
	Map<String, CircleVertexData> circleVertexDataMap =
			LazyMap.decorate(new HashMap<String,CircleVertexData>(), 
			new Factory<CircleVertexData>() {
				public CircleVertexData create() {
					return new CircleVertexData();
				}});	

	/**
	 * Creates an instance for the specified graph.
	 */
	public RoleGraphLayout(Graph<String,String> g) {
		super(g);
	}

	/**
	 * Returns the radius of the circle.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Sets the radius of the circle.  Must be called before
	 * {@code initialize()} is called.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * Sets the order of the vertices in the layout according to the ordering
	 * specified by {@code comparator}.
	 */
	public void setVertexOrder(Comparator<String> comparator)
	{
	    if (vertex_ordered_list == null)
	        vertex_ordered_list = new ArrayList<String>(getGraph().getVertices());
	    Collections.sort(vertex_ordered_list, comparator);
	}

    /**
     * Sets the order of the vertices in the layout according to the ordering
     * of {@code vertex_list}.
     */
	public void setVertexOrder(List<String> vertex_list)
	{
	    if (!vertex_list.containsAll(getGraph().getVertices())) 
	        throw new IllegalArgumentException("Supplied list must include " +
	        		"all vertices of the graph");
	    this.vertex_ordered_list = vertex_list;
	}
	
	
	public void reset() {
		vertex_ordered_list = null;
		initialize();
	}

	public void initialize() 
	{
		Dimension d = getSize();
		
		if (d != null) 
		{
		    if (vertex_ordered_list == null) 
		        setVertexOrder(new ArrayList<String>(getGraph().getVertices()));

			double height = d.getHeight();
			double width = d.getWidth();

			if (radius <= 0) {
				radius = 0.45 * (height < width ? height : width);
			}

			int i = 0;
			for (String v : vertex_ordered_list)
			{
				Point2D coord = transform(v);

				double angle = (2 * Math.PI * i) / vertex_ordered_list.size();

				coord.setLocation(Math.cos(angle) * radius + width / 2,
						Math.sin(angle) * radius + height / 2);

				CircleVertexData data = getCircleData(v);
				data.setAngle(angle);
				i++;
			}
		}
	}

	protected CircleVertexData getCircleData(String v) {
		return circleVertexDataMap.get(v);
	}

	protected static class CircleVertexData {
		private double angle;

		protected double getAngle() {
			return angle;
		}

		protected void setAngle(double angle) {
			this.angle = angle;
		}

		@Override
		public String toString() {
			return "CircleVertexData: angle=" + angle;
		}
	}
}
