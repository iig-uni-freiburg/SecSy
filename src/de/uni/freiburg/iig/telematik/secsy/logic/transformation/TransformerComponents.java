/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.graphic.dialog.MessageDialog;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.invation.code.toval.misc.wd.AbstractProjectComponents;
import de.invation.code.toval.misc.wd.ProjectComponentException;
import de.uni.freiburg.iig.telematik.secsy.gui.SimulationComponents;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.gui.properties.SecSyProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author stocker
 */
public class TransformerComponents extends AbstractProjectComponents {

    private static TransformerComponents instance = null;

    private TraceTransformerContainer containerRegularTraceTransformers;
    private CustomTraceTransformerContainer containerCustomTraceTransformers;

    public TransformerComponents() throws ProjectComponentException {
        super(MessageDialog.getInstance());
    }

    public static TransformerComponents getInstance() throws ProjectComponentException {
        if (instance == null) {
            instance = new TransformerComponents();
        }
        return instance;
    }

    public TraceTransformerContainer getContainerRegularTraceTransformers() {
        return containerRegularTraceTransformers;
    }

    public CustomTraceTransformerContainer getContainerCustomTraceTransformers() {
        return containerCustomTraceTransformers;
    }

    public List<TransformerType> getAllTransformerTypes() {
        List<TransformerType> result = new ArrayList<>();
        result.addAll(getContainerRegularTraceTransformers().getTransformerTypes());
        result.addAll(getContainerCustomTraceTransformers().getCustomTransformerTypes());
        Collections.sort(result);
        return result;
    }

    public List<String> getAllTransformerNames() {
        List<String> result = new ArrayList<>();
        result.addAll(getContainerRegularTraceTransformers().getComponentNames());
        result.addAll(getContainerCustomTraceTransformers().getComponentNames());
        Collections.sort(result);
        return result;
    }

    public boolean containsTransformers() {
        if (getContainerRegularTraceTransformers().containsComponents()) {
            return true;
        }
        if (getContainerCustomTraceTransformers().containsComponents()) {
            return true;
        }
        return false;
    }

    public AbstractTraceTransformer getTransformer(String transformerName) throws ProjectComponentException {
        if (getContainerRegularTraceTransformers().containsComponent(transformerName)) {
            return getContainerRegularTraceTransformers().getComponent(transformerName);
        }
        if (getContainerCustomTraceTransformers().containsComponent(transformerName)) {
            return getContainerCustomTraceTransformers().getComponent(transformerName);
        }
        return null;
    }

    public boolean isCustomTransformer(AbstractTraceTransformer transformer) {
        return getContainerCustomTraceTransformers().isCustomTransformer(transformer);
    }

    public void addTransformer(AbstractTraceTransformer transformer, boolean storeToFile) throws ProjectComponentException {
        if (isCustomTransformer(transformer)) {
            getContainerCustomTraceTransformers().addComponent(transformer, storeToFile);
        } else {
            getContainerRegularTraceTransformers().addComponent(transformer, storeToFile);
        }
    }

    public boolean containsTransformer(String transformerName) {
        if (getContainerCustomTraceTransformers().containsComponent(transformerName)) {
            return true;
        }
        if (getContainerRegularTraceTransformers().containsComponent(transformerName)) {
            return true;
        }
        return false;
    }

    public void removeTransformer(String transformerName, boolean removeFromDisk) throws ProjectComponentException {
        if (!containsTransformer(transformerName)) {
            return;
        }
        if (isCustomTransformer(getTransformer(transformerName))) {
            getContainerCustomTraceTransformers().removeComponent(transformerName, removeFromDisk);
        } else {
            getContainerRegularTraceTransformers().removeComponent(transformerName, removeFromDisk);
        }
    }

    @Override
    protected void addComponentContainers() throws ProjectComponentException {
        try {
            containerRegularTraceTransformers = new TraceTransformerContainer(SecSyProperties.getInstance().getPathForTransformers(), MessageDialog.getInstance());
            addComponentContainer(containerRegularTraceTransformers);
            containerCustomTraceTransformers = new CustomTraceTransformerContainer(SecSyProperties.getInstance(), MessageDialog.getInstance());
            addComponentContainer(containerCustomTraceTransformers);
        } catch (Exception e) {
            throw new ProjectComponentException("Exception while creating component containers", e);
        }
    }

}
