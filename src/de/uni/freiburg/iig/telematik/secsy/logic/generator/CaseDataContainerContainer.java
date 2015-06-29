/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.invation.code.toval.debug.SimpleDebugger;
import de.invation.code.toval.misc.wd.AbstractComponentContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.properties.CaseDataContainerProperties;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author stocker
 */
public class CaseDataContainerContainer extends AbstractComponentContainer<CaseDataContainer>{
    
    public static final String CASE_DATA_CONTAINER_DESCRIPTOR = "Case Data Container";

    public CaseDataContainerContainer(String serializationPath) {
        super(serializationPath);
    }

    public CaseDataContainerContainer(String serializationPath, SimpleDebugger debugger) {
        super(serializationPath, debugger);
    }
    
    @Override
    public String getComponentDescriptor() {
        return CASE_DATA_CONTAINER_DESCRIPTOR;
    }

    @Override
    protected CaseDataContainer loadComponentFromFile(String file) throws Exception {
        CaseDataContainerProperties properties = new CaseDataContainerProperties();
        try {
            properties.load(file);
        } catch (IOException e) {
            throw new IOException("Cannot load properties file: " + file + ".");
        }

//		String contextName = properties.getContextName();
//		Context referencedContext = getContext(contextName);
//		if(referencedContext == null)
//			throw new PropertyException(CaseDataContainerProperty.CONTEXT_NAME, referencedContext, "Unknown context.");
        AttributeValueGenerator valueGenerator = properties.getAttributeValueGenerator();
        CaseDataContainer result = new CaseDataContainer(valueGenerator);
        result.setName(properties.getName());
        return result;
    }

    @Override
    protected void serializeComponent(CaseDataContainer component, String serializationPath, String fileName) throws Exception {
        component.getProperties().store(serializationPath.concat(fileName));
    }
    
    
}
