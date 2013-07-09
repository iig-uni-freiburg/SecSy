package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.Properties;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public abstract class AbstractMultipleTraceTransformerProperties extends AbstractTransformerProperties{
	
	public static final Integer defaultMaxAppliances = 1;

	public AbstractMultipleTraceTransformerProperties() {
		super();
	}

	public AbstractMultipleTraceTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setMaxAppliances(Integer maxAppliances) throws ParameterException{
		validateAppliances(maxAppliances);
		props.setProperty(MultipleTraceTransformerProperty.MAX_APPLIANCES.toString(), maxAppliances.toString());
	}
	
	public Integer getMaxAppliances() throws ParameterException, PropertyException{
		String propertyValue = props.getProperty(MultipleTraceTransformerProperty.MAX_APPLIANCES.toString());
		Integer result = null;
		try{
			result = Integer.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(MultipleTraceTransformerProperty.MAX_APPLIANCES, propertyValue);
		}
		validateAppliances(result);
		return result;
	}
	
	public static void validateAppliances(Integer appliances) throws ParameterException{
		Validate.notNull(appliances);
		Validate.notNegative(appliances);
		Validate.bigger(appliances, 0);
	}
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(MultipleTraceTransformerProperty.MAX_APPLIANCES.toString(), defaultMaxAppliances.toString());
		return defaultProperties;
	}
	
	private enum MultipleTraceTransformerProperty {
		MAX_APPLIANCES;
	}
	
}
