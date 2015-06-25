/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import de.invation.code.toval.properties.PropertyException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractMultipleTraceTransformerProperties;

/**
 *
 * @author stocker
 * @param <T>
 */
public abstract class AbstractCustomMultipleTraceTransformer<T extends AbstractCustomMultipleTraceTransformer<T>> extends AbstractMultipleTraceTransformer<AbstractMultipleTraceTransformerProperties,T>{

    public AbstractCustomMultipleTraceTransformer(Double activationProbability, Integer maxAppliances) {
        super(activationProbability, maxAppliances);
    }

    public AbstractCustomMultipleTraceTransformer() {
        super();
    }

    @Override
    protected T newInstance(AbstractMultipleTraceTransformerProperties properties) throws Exception {
        throw new UnsupportedOperationException("Not supported in custom transformers.");
    }

    @Override
    protected AbstractMultipleTraceTransformerProperties newProperties(){
        throw new UnsupportedOperationException("Not supported in custom transformers.");
    }
    
}
