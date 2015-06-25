package de.uni.freiburg.iig.telematik.secsy.gui;

import de.invation.code.toval.graphic.dialog.MessageDialog;
import java.io.IOException;

import javax.swing.SwingUtilities;

import de.invation.code.toval.graphic.misc.AbstractStartup;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ExceptionDialog;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;

public class Startup extends AbstractStartup {

    private static final String TOOL_NAME = "SECSY";
    private static final String WORKING_DIRECTORY_DESCRIPTOR = "Simulation Directory";

    @Override
    protected String getToolName() {
        return TOOL_NAME;
    }

    @Override
    protected void startApplication() throws Exception {
        // Check if there is a path to a simulation directory.
        if (!checkSimulationDirectory()) {
            // There is no path and it is either not possible to set a path or the user aborted the corresponding dialog.
            System.exit(0);
        }
        MessageDialog.getInstance();
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    try{
                        SimulationComponents.getInstance();
                    } catch(Exception e){
                        MessageDialog.getInstance().message("Exception while accessing simulation components: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            throw new Exception("Exception during startup:<br>Reason: " + e.getMessage(), e);
        }
        new Simulator();
    }

    private boolean checkSimulationDirectory() {
        try {
            SecSyProperties.getInstance().getWorkingDirectory();
            return true;
        } catch (IOException e) {
            ExceptionDialog.showException(null, "Internal Exception", new Exception("Cannot load/create property file", e), true);
            return false;
        } catch (PropertyException e) {
            // There is no recent simulation directory
            // -> Let the user choose a path for the simulation directory
            return chooseWorkingDirectory();
        } catch (ParameterException e) {
            // Value for simulation directory is invalid, possibly due to moved directories
            // -> Remove entry for actual simulation directory
            try {
                SecSyProperties.getInstance().removeWorkingDirectory();
            } catch (IOException e1) {
                ExceptionDialog.showException(null, "Internal Exception", new Exception("Cannot fix corrupt property entries.", e), true);
                return false;
            }
            // -> Let the user choose a path for the simulation directory
            return chooseWorkingDirectory();
        }
    }

    @Override
    protected String getWorkingDirectoryDescriptor() {
        return WORKING_DIRECTORY_DESCRIPTOR;
    }

    @Override
    protected void setWorkingDirectory(String workingDirectory) throws Exception {
        SecSyProperties.getInstance().setWorkingDirectory(workingDirectory, false);
    }

    @Override
    protected String launchWorkingDirectoryDialog() throws Exception{
        return SimulationDirectoryDialog.showDialog(null);
    }
    
    public static void main(String[] args) throws Exception{
        Startup startup = new Startup();
    }
}
