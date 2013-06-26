package gui.dialog;

import gui.properties.GeneralProperties;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

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
 
            addKnownSimulationDirectory(simulationDirectory, true);
        }
	}

}
