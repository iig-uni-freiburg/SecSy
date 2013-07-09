package de.uni.freiburg.iig.telematik.secsy.gui.permission;


import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import de.invation.code.toval.graphic.CircularPointGroup;
import de.invation.code.toval.graphic.PColor;
import de.invation.code.toval.graphic.Position;
import de.invation.code.toval.types.DataUsage;



public class CircularObjectPermissionPanel extends ObjectPermissionPanel {

	private static final long serialVersionUID = -7968207593250068161L;
	
	private int minX = 0;
	private int maxX = 0;
	private int minY = 0;
	private int maxY = 0;

	public CircularObjectPermissionPanel(String name, Set<DataUsage> validDataUsageModes){
		super(name, validDataUsageModes);
	}
	
	@Override
	protected void determineCheckBoxCoordinates(){
		CircularPointGroup pointGroup = new CircularPointGroup(5, CHECKBOX_SIZE);
		pointGroup.addPoints(PColor.black, validDataUsageModes.size());
		List<Position> circularPositions = pointGroup.getCoordinatesFor(PColor.black);
		List<Integer> xCoords = new ArrayList<Integer>();
		List<Integer> yCoords = new ArrayList<Integer>();
		for(Position position: circularPositions){
			xCoords.add(position.getX());
			yCoords.add(position.getY());
		}
		minX = Collections.min(xCoords);
		maxX = Collections.max(xCoords);
		minY = Collections.min(yCoords);
		maxY = Collections.max(yCoords);
		
		Point mid = new Point((int) (getPreferredPanelSize().width/2.0), (int) (getPreferredPanelSize().height/2.0));
		for(Position position: circularPositions){
			positions.add(new Position(mid.x + position.getX() - ((int) (CHECKBOX_SIZE/2.0)) - 3, mid.y + position.getY() - ((int) (CHECKBOX_SIZE/2.0)) - 1));
		}
	}
	
	@Override
	public Dimension getPreferredPanelSize(){
		return new Dimension(Math.abs(minX) + Math.abs(maxX) + CHECKBOX_SIZE + 2*MARGIN, 
		           			(Math.abs(minY) + Math.abs(maxY) + CHECKBOX_SIZE + 2*MARGIN));
	}


	public static void main(String[] args) {
		JFrame frame = new JFrame();
		Set<DataUsage> testset = new HashSet<DataUsage>(Arrays.asList(DataUsage.values()));
//		testset.remove(DataUsage.CREATE);
//		testset.remove(DataUsage.DELETE);
		CircularObjectPermissionPanel panel = new CircularObjectPermissionPanel("eee", testset);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
}
