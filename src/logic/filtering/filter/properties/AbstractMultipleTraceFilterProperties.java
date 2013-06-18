package logic.filtering.filter.properties;

import java.io.IOException;
import java.util.Properties;

import properties.PropertyException;
import validate.ParameterException;
import validate.Validate;

public abstract class AbstractMultipleTraceFilterProperties extends AbstractFilterProperties{
	
	public static final Integer defaultMaxAppliances = 1;

	public AbstractMultipleTraceFilterProperties() {
		super();
	}

	public AbstractMultipleTraceFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setMaxAppliances(Integer maxAppliances) throws ParameterException{
		validateAppliances(maxAppliances);
		props.setProperty(MultipleTraceFilterProperty.MAX_APPLIANCES.toString(), maxAppliances.toString());
	}
	
	public Integer getMaxAppliances() throws ParameterException, PropertyException{
		String propertyValue = props.getProperty(MultipleTraceFilterProperty.MAX_APPLIANCES.toString());
		Integer result = null;
		try{
			result = Integer.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(MultipleTraceFilterProperty.MAX_APPLIANCES, propertyValue);
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
		defaultProperties.setProperty(MultipleTraceFilterProperty.MAX_APPLIANCES.toString(), defaultMaxAppliances.toString());
		return defaultProperties;
	}
	
	private enum MultipleTraceFilterProperty {
		MAX_APPLIANCES;
	}
	
}
