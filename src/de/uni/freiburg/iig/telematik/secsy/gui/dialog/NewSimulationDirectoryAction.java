package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import gui.properties.GeneralProperties;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.invation.code.toval.properties.PropertyException;

public class NewSimulationDirectoryAction extends AbstractSimulationDirectoryAction {

	private static final long serialVersionUID = 3421975574956233676L;
	
	public NewSimulationDirectoryAction(Window parentWindow){
		super(parentWindow, "New Directory");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose location for new simulation directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String simulationDirectoryLocation = file.getAbsolutePath();
            File dir = new File(simulationDirectoryLocation + "/" + GeneralProperties.defaultSimulationDirectoryName);
            if(dir.exists()){
            	int count = 1;
            	while((dir = new File(simulationDirectoryLocation + "/" + GeneralProperties.defaultSimulationDirectoryName + count)).exists()){
            		count++;
            	}
            } 
            dir.mkdir();
            String simulationDirectory = dir.getAbsolutePath() + "/";
 
            try {
				addKnownSimulationDirectory(simulationDirectory, true);
			} catch (PropertyException e1) {
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Property Exception", JOptionPane.ERROR_MESSAGE);
			}
        }
	}

}