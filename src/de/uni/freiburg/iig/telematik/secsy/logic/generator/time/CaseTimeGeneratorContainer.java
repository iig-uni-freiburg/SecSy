/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator.time;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeGeneratorFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class CaseTimeGeneratorContainer extends AbstractComponentContainer<CaseTimeGenerator>{
    
    public static final String CASE_TIME_GENERATOR_DESCRIPTOR = "Case Time Generator";

    public CaseTimeGeneratorContainer(String serializationPath) {
        super(serializationPath);
    }

    public CaseTimeGeneratorContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }
    
    @Override
    public String getComponentDescriptor() {
        return CASE_TIME_GENERATOR_DESCRIPTOR;
    }

    @Override
    public Set<String> getAcceptedFileEndings() {
        return new HashSet<>(Arrays.asList(""));
    }
    
    @Override
    protected CaseTimeGenerator loadComponentFromFile(String file) throws Exception {
        return TimeGeneratorFactory.createCaseTimeGenerator(new TimeProperties(file));
    }

    @Override
    protected void serializeComponent(CaseTimeGenerator component, String serializationPath, String fileName) throws Exception {
        component.getProperties().store(serializationPath.concat(fileName));
    }
    
}
