package logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.Properties;

import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import logic.transformation.transformer.TransformerType;

public class AbstractFilterProperties extends AbstractProperties{
	
	public static final Double defaultActivationProbability = 1.0;
	public static final Boolean defaultIncludeStatusMessages = true;
	public static final String defaultFilterName = "no name";
	public static final String defaultFilterType = "no type";

	public AbstractFilterProperties() {
		super();
	}

	public AbstractFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	
	//-- Filter name
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		Validate.notEmpty(name);
		props.setProperty(AbstractFilterProperty.NAME.toString(), name);
	}
	
	public String getFilterName() throws PropertyException{
		String propertyValue = props.getProperty(AbstractFilterProperty.NAME.toString());
		if(propertyValue == null)
			throw new PropertyException(AbstractFilterProperty.NAME, propertyValue);
		return propertyValue;
	}
	
	
	//-- Activation probability
	
	public void setActivationProbability(Double probability) throws ParameterException{
		validateActivationProbability(probability);
		props.setProperty(AbstractFilterProperty.ACTIVATION_PROBABILITY.toString(), probability.toString());
	}
	
	public Double getActivationProbability() throws ParameterException, PropertyException{
		String propertyValue = props.getProperty(AbstractFilterProperty.ACTIVATION_PROBABILITY.toString());
		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AbstractFilterProperty.ACTIVATION_PROBABILITY, propertyValue);
		}
		validateActivationProbability(result);
		return result;
	}
	
	
	//-- Include messages
	
	public void setIncludeMessages(Boolean includeMessages) throws ParameterException{
		Validate.notNull(includeMessages);
		props.setProperty(AbstractFilterProperty.INCLUDE_STATUS_MESSAGES.toString(), includeMessages.toString());
	}
	
	public Boolean getIncludeMessages() throws PropertyException{
		String propertyValue = props.getProperty(AbstractFilterProperty.INCLUDE_STATUS_MESSAGES.toString());
		Boolean result = null;
		try{
			result = Boolean.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AbstractFilterProperty.INCLUDE_STATUS_MESSAGES, propertyValue);
		}
		return result;
	}
	
	
	//-- Filter type
	
	public void setFilterType(TransformerType type) throws ParameterException{
		Validate.notNull(type);
		props.setProperty(AbstractFilterProperty.TYPE.toString(), type.toString());
	}
	
	public TransformerType getFilterType() throws ParameterException{
		String propertyValue = props.getProperty(AbstractFilterProperty.TYPE.toString());
		Validate.notNull(propertyValue);
		
		TransformerType type = null;
		try{
			type = TransformerType.valueOfString(propertyValue);
		}catch(Exception e){
			
		}
		return type;
	}
	
	
	//-- Default properties
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = new Properties();
		defaultProperties.setProperty(AbstractFilterProperty.ACTIVATION_PROBABILITY.toString(), defaultActivationProbability.toString());
		defaultProperties.setProperty(AbstractFilterProperty.INCLUDE_STATUS_MESSAGES.toString(), defaultIncludeStatusMessages.toString());
		defaultProperties.setProperty(AbstractFilterProperty.TYPE.toString(), defaultFilterType.toString());
		defaultProperties.setProperty(AbstractFilterProperty.NAME.toString(), defaultFilterName.toString());
		return defaultProperties;
	}
	
	
	//------- Parameter validation -------------------------------------------------------------------------
	
	protected void validateActivationProbability(Double probability) throws ParameterException{
		Validate.probability(probability);
	}
	
	protected enum AbstractFilterProperty {
		NAME, TYPE, ACTIVATION_PROBABILITY, INCLUDE_STATUS_MESSAGES;
	}

}
