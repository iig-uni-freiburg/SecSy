package gui.dialog;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

public class OpenSimulationDirectoryAction extends AbstractSimulationDirectoryAction {

	private static final long serialVersionUID = 3421975574956233676L;

	public OpenSimulationDirectoryAction(Window parentWindow){
		super(parentWindow, "Existing Directory");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("sdsdsd");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Choose existing simulation directory");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = fileChooser.showOpenDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String simulationDirectory = file.getAbsolutePath()+"/";
            
            addKnownSimulationDirectory(simulationDirectory, false);
        }
	}

}
