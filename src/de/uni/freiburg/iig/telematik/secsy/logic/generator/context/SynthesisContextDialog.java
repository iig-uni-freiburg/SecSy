/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator.context;

import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContextDialog;
import java.awt.Window;

/**
 *
 * @author stocker
 */
public class SynthesisContextDialog extends ConstraintContextDialog{
    
    private static final long serialVersionUID = -9033530199377155670L;

    public SynthesisContextDialog(Window owner) throws Exception {
        super(owner);
    }

    public SynthesisContextDialog(Window owner, SynthesisContext context) throws Exception {
        super(owner, context);
    }
    
    @Override
    public SynthesisContext getDialogObject(){
        return (SynthesisContext) super.getDialogObject();
    }
    
    //------- STARTUP ---------------------------------------------------------------------------------------------------------------
    
    public static SynthesisContext showDialog(Window parentWindow) throws Exception {
        SynthesisContextDialog contextDialog = new SynthesisContextDialog(parentWindow);
        contextDialog.setUpGUI();
        return contextDialog.getDialogObject();
    }

    public static boolean showDialog(Window parentWindow, SynthesisContext context) throws Exception {
        SynthesisContextDialog contextDialog = new SynthesisContextDialog(parentWindow, context);
        contextDialog.setUpGUI();
        return contextDialog.getDialogObject() != null;
    }
    
}
