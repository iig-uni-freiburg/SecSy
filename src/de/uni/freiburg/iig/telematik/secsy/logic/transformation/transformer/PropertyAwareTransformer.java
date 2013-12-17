package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;

public interface PropertyAwareTransformer {
	
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException;

}
