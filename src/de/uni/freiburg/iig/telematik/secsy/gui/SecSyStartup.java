package de.uni.freiburg.iig.telematik.secsy.gui;


import de.invation.code.toval.graphic.misc.AbstractWorkingDirectoryStartup;
import de.invation.code.toval.misc.wd.AbstractWorkingDirectoryProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.SimulationDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;

public class SecSyStartup extends AbstractWorkingDirectoryStartup {

    private static final String TOOL_NAME = "SECSY";
    private static final String WORKING_DIRECTORY_DESCRIPTOR = "Simulation Directory";

    @Override
    protected String getToolName() {
        return TOOL_NAME;
    }

    @Override
    protected String getWorkingDirectoryDescriptor() {
        return WORKING_DIRECTORY_DESCRIPTOR;
    }

    @Override
    protected String launchWorkingDirectoryDialog() throws Exception{
        return SimulationDirectoryDialog.showDialog(null);
    }

    @Override
    protected AbstractWorkingDirectoryProperties getWorkingDirectoryProperties() throws Exception {
        return SecSyProperties.getInstance();
    }

    @Override
    protected void initializeComponentContainer() throws Exception {
        SimulationComponents.getInstance();
    }

    @Override
    protected void createMainClass() throws Exception {
        new Simulator();
    }
    
    public static void main(String[] args) throws Exception{
        SecSyStartup startup = new SecSyStartup();
    }
}
