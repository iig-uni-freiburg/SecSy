package de.uni.freiburg.iig.telematik.secsy.gui.dialog.rbac;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.apache.commons.collections15.Transformer;


public class RoleVertexSizeTransformer implements Transformer<String, Shape>{
	
	public static final int diameter = 40;

	@Override
	 public Shape transform(String s){
        return new Ellipse2D.Double(-15, -15, diameter, diameter);
    }

}
