package de.uni.freiburg.iig.telematik.secsy.gui.action;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.invation.code.toval.properties.PropertyException;

public class OpenSimulationDirectoryAction extends AbstractSimulationDirectoryAction {

	private static final long serialVersionUID = 3421975574956233676L;

	public OpenSimulationDirectoryAction(Window parentWindow){
		super(parentWindow, "Existing...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose existing simulation directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String simulationDirectory = file.getAbsolutePath()+"/";
            
            try {
				addKnownSimulationDirectory(simulationDirectory, false);
			} catch (PropertyException e1) {
				Window sourceWindow = null;
				if(e.getSource() instanceof Window){
					sourceWindow = (Window) e.getSource();
				} else if(e.getSource() instanceof Component){
					sourceWindow = SwingUtilities.getWindowAncestor((Component) e.getSource());
				}
				JOptionPane.showMessageDialog(sourceWindow, e1.getMessage(), "Property Exception", JOptionPane.ERROR_MESSAGE);
			}
        }
	}

}
