/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;

/**
 *
 * @author stocker
 * @param <T>
 */
public abstract class AbstractCustomTraceTransformer<T extends AbstractCustomTraceTransformer<T>> extends AbstractTraceTransformer<AbstractTransformerProperties,T> {

    public AbstractCustomTraceTransformer(Double activationProbability) {
        super(activationProbability);
    }

    public AbstractCustomTraceTransformer() {
        super();
    }

    @Override
    protected final AbstractTransformerProperties newProperties() {
        throw new UnsupportedOperationException("Not supported in custom transformers.");
    }

    @Override
    protected T newInstance(AbstractTransformerProperties properties) {
        throw new UnsupportedOperationException("Not supported in custom transformers.");
    }

}
