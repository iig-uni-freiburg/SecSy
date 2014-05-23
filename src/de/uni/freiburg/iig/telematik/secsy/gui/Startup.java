package de.uni.freiburg.iig.telematik.secsy.gui;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.MessageDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.GeneralProperties;

public class Startup {
	
	public static void main(String[] args) {
		String osType = System.getProperty("os.name");
		if(osType.equals("Mac OS") || osType.equals("Mac OS X")){
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
			System.setProperty("com.apple.macos.useScreenMenuBar", "true");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
		}
		
		//Check if there is a path to a simulation directory.
		if (!checkSimulationDirectory()) {
			// There is no path and it is either not possible to set a path or the user aborted the corresponding dialog.
			System.exit(0);
		}
				
		MessageDialog.getInstance();
		try {
			SwingUtilities.invokeAndWait(new Runnable(){

				@Override
				public void run() {
					SimulationComponents.getInstance();
				}
				
			});
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Cannot launch SecSy", "Exception during startup:<br>Reason: " + e.getMessage(), JOptionPane.ERROR_MESSAGE);
		}
		new Simulator();
	}
	
	private static boolean checkSimulationDirectory(){
		try {
			GeneralProperties.getInstance().getSimulationDirectory();
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Internal exception: Cannot load/create general property file:\n" + e.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (PropertyException e) {
			// There is no recent simulation directory
			// -> Let the user choose a path for the simulation directory
			return chooseSimulationDirectory();
		} catch (ParameterException e) {
			// Value for simulation directory is invalid, possibly due to moved directories
			// -> Remove entry for actual simulation directory
			try {
				GeneralProperties.getInstance().removeSimulationDirectory();
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Internal exception: Cannot fix corrupt property entries:\n" + e1.getMessage(), "Internal Exception", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// -> Let the user choose a path for the simulation directory
			return chooseSimulationDirectory();
		}
	}
	
	private static boolean chooseSimulationDirectory(){
		String simulationDirectory = null;
		try {
			simulationDirectory = SimulationDirectoryDialog.showDialog(null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "<html>Cannot start simulation directory dialog.<br>Reason: "+e.getMessage()+"</html>", "Internal Exception", JOptionPane.ERROR_MESSAGE);
		}
		if(simulationDirectory == null)
			return false;
		try {
			GeneralProperties.getInstance().setSimulationDirectory(simulationDirectory, false);
			return true;
		} catch (ParameterException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "I/O Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (PropertyException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage(), "Property Exception", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

}
