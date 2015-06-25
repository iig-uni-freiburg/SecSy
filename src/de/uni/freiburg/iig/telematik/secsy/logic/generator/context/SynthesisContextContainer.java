/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator.context;

import de.invation.code.toval.debug.SimpleDebugger;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.parser.ACModelContainer;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.AbstractConstraintContextContainer;
import de.uni.freiburg.iig.telematik.sewol.context.constraint.ConstraintContextProperties;
import java.util.Map;

/**
 *
 * @author stocker
 */
public class SynthesisContextContainer extends AbstractConstraintContextContainer<SynthesisContext, ConstraintContextProperties>{
    
    private static final String SYNTHESIS_CONTEXT_DESCRIPTOR = "Synthesis Context";

    public SynthesisContextContainer(String serializationPath, ACModelContainer availableACModels) {
        super(serializationPath, availableACModels);
    }

    public SynthesisContextContainer(String serializationPath, ACModelContainer availableACModels, SimpleDebugger debugger) {
        super(serializationPath, availableACModels, debugger);
    }
    
    @Override
    public String getComponentDescriptor() {
        return SYNTHESIS_CONTEXT_DESCRIPTOR;
    }

    @Override
    protected ConstraintContextProperties crearteNewProperties() throws Exception {
        return new ConstraintContextProperties();
    }

    @Override
    protected SynthesisContext createSOABaseFromProperties(ConstraintContextProperties properties) throws Exception {
        return new SynthesisContext(properties);
    }

}