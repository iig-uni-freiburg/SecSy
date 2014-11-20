package de.uni.freiburg.iig.telematik.secsy.gui.action;


import java.awt.Window;
import java.io.IOException;

import javax.swing.AbstractAction;

import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.GeneralProperties;


public abstract class AbstractSimulationDirectoryAction extends AbstractAction {

	private static final long serialVersionUID = 6658565129248580915L;

	public static final String PROPERTY_NAME_SIMULATION_DIRECTORY = "simulationDirectory";
	public static final String PROPERTY_NAME_SUCCESS = "success";
	
	protected Window parent = null;
	
	public AbstractSimulationDirectoryAction(Window parentWindow, String name){
		super(name);
		this.parent = parentWindow;
	}
	
	protected void addKnownSimulationDirectory(String simulationDirectory, boolean createSubfolders) throws PropertyException{
		try {
			GeneralProperties.getInstance().addKnownSimulationDirectory(simulationDirectory, createSubfolders);
			GeneralProperties.getInstance().setSimulationDirectory(simulationDirectory, true);
			GeneralProperties.getInstance().store();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        putValue(PROPERTY_NAME_SIMULATION_DIRECTORY, simulationDirectory);
        putValue(PROPERTY_NAME_SUCCESS, true);
	}

}
