package de.uni.freiburg.iig.telematik.secsy.gui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uni.freiburg.iig.telematik.secsy.gui.dialog.MessageDialog;

public class Startup {
	
	public static void main(String[] args) {
		String osType = System.getProperty("os.name");
		if(osType.equals("Mac OS") || osType.equals("Mac OS X")){
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
			System.setProperty("com.apple.macos.useScreenMenuBar", "true");
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "SecSy");
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

}
