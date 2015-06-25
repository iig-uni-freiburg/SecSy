/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator.context;

import de.invation.code.toval.misc.soabase.SOABase;
import static de.invation.code.toval.misc.soabase.SOABase.createFromProperties;
import de.invation.code.toval.misc.soabase.SOABaseProperties;
import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContext;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContextProperties;
import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContext;
import de.uni.freiburg.iig.telematik.sewol.context.process.ProcessContextProperties;
import java.awt.Window;
import java.io.File;

/**
 *
 * @author stocker
 */
public class SynthesisContext extends ConstraintContext {

    public SynthesisContext() {
    }

    public SynthesisContext(String name) {
        super(name);
    }

    public SynthesisContext(ConstraintContextProperties properties) throws PropertyException {
        super(properties);
    }
    
    public static SynthesisContext createFromFile(File file) throws Exception {
        SOABaseProperties properties = ConstraintContextProperties.loadPropertiesFromFile(file);
        if(!(properties instanceof ConstraintContextProperties))
           throw new Exception("Loaded properties are not compatible with synthesis context");
        SOABase newContext = createFromProperties(properties);
        if(!(newContext instanceof SynthesisContext))
            throw new Exception("Created context of wrong type, expected \"SynthesisContext\" but was \"" + newContext.getClass().getSimpleName() + "\"");
        return (SynthesisContext) newContext;
    }
    
    @Override
    public boolean showDialog(Window parent) throws Exception{
        return SynthesisContextDialog.showDialog(parent, this);
    }
    
}
