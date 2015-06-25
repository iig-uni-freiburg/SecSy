/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.BoDPropertyTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.DayDelayTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.IncompleteLoggingTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.ObfuscationTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.SkipActivitiesTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.SoDPropertyTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.transformer.panel.UnauthorizedExecutionTransformerPanel;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.BoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.DayDelayTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.IncompleteLoggingTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SkipActivitiesTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.UnauthorizedExecutionTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class TraceTransformerContainer extends AbstractComponentContainer<AbstractTraceTransformer> {

    public static final String TRANSFORMER_DESCRIPTOR = "Transformer";

    private static Set<TransformerType> transformerTypes = new HashSet<TransformerType>();

    static {
        transformerTypes.add(new TransformerType(DayDelayTransformer.class, DayDelayTransformerPanel.class));
        transformerTypes.add(new TransformerType(IncompleteLoggingTransformer.class, IncompleteLoggingTransformerPanel.class));
        transformerTypes.add(new TransformerType(SkipActivitiesTransformer.class, SkipActivitiesTransformerPanel.class));
        transformerTypes.add(new TransformerType(ObfuscationTransformer.class, ObfuscationTransformerPanel.class));
        transformerTypes.add(new TransformerType(UnauthorizedExecutionTransformer.class, UnauthorizedExecutionTransformerPanel.class));
        transformerTypes.add(new TransformerType(SoDPropertyTransformer.class, SoDPropertyTransformerPanel.class));
        transformerTypes.add(new TransformerType(BoDPropertyTransformer.class, BoDPropertyTransformerPanel.class));
    }

    public TraceTransformerContainer(String serializationPath) {
        super(serializationPath);
    }

    public TraceTransformerContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }

    @Override
    public String getComponentDescriptor() {
        return TRANSFORMER_DESCRIPTOR;
    }

    @Override
    public Set<String> getAcceptedFileEndings() {
        return new HashSet<>(Arrays.asList(""));
    }

    public Set<TransformerType> getTransformerTypes() {
        return Collections.unmodifiableSet(transformerTypes);
    }

    @Override
    protected AbstractTraceTransformer loadComponentFromFile(String file) throws Exception {
        return TransformerFactory.loadTransformer(file);
    }

    @Override
    protected void serializeComponent(AbstractTraceTransformer component, String serializationPath, String fileName) throws Exception {
        component.getProperties().store(serializationPath.concat(fileName));
    }

}
