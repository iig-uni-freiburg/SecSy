package de.uni.freiburg.iig.telematik.secsy.gui.dialog;

import de.invation.code.toval.debug.SimpleDebugger;
import java.awt.Window;
import java.beans.PropertyChangeListener;
import javax.swing.border.Border;

import de.invation.code.toval.misc.wd.AbstractWorkingDirectoryDialog;
import de.uni.freiburg.iig.telematik.secsy.gui.GUIProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperty;

public class SimulationDirectoryDialog extends AbstractWorkingDirectoryDialog<SecSyProperty> implements PropertyChangeListener {

    private static final long serialVersionUID = 2306027725394345926L;

    public SimulationDirectoryDialog(Window owner) throws Exception {
        super(owner, SecSyProperties.getInstance());
    }

    public SimulationDirectoryDialog(Window owner, SimpleDebugger debugger) throws Exception {
        super(owner, SecSyProperties.getInstance(), debugger);
    }

    @Override
    protected Border getBorder() {
        return GUIProperties.DEFAULT_DIALOG_BORDER;
    }
    
    public static String showDialog(Window owner) throws Exception {
        return showDialog(owner, null);
    }
    
    public static String showDialog(Window owner, SimpleDebugger debugger) throws Exception {
        SimulationDirectoryDialog dialog = new SimulationDirectoryDialog(owner, debugger);
        dialog.setUpGUI();
        return dialog.getDialogObject();
    }

}
