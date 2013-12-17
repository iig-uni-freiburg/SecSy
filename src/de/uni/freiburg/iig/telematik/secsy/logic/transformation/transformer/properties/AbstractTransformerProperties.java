package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.Properties;

import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public class AbstractTransformerProperties extends AbstractProperties{
	
	public static final Double defaultActivationProbability = 1.0;
	public static final Boolean defaultIncludeStatusMessages = true;
	public static final String defaultName = "no name";
	public static final String defaultType = "no type";

	public AbstractTransformerProperties() {
		super();
	}

	public AbstractTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	
	//-- Name
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		Validate.notEmpty(name);
		props.setProperty(AbstractTransformerProperty.NAME.toString(), name);
	}
	
	public String getName() throws PropertyException{
		String propertyValue = props.getProperty(AbstractTransformerProperty.NAME.toString());
		if(propertyValue == null)
			throw new PropertyException(AbstractTransformerProperty.NAME, propertyValue);
		return propertyValue;
	}
	
	
	//-- Activation probability
	
	public void setActivationProbability(Double probability) throws ParameterException{
		validateActivationProbability(probability);
		props.setProperty(AbstractTransformerProperty.ACTIVATION_PROBABILITY.toString(), probability.toString());
	}
	
	public Double getActivationProbability() throws ParameterException, PropertyException{
		String propertyValue = props.getProperty(AbstractTransformerProperty.ACTIVATION_PROBABILITY.toString());
		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AbstractTransformerProperty.ACTIVATION_PROBABILITY, propertyValue);
		}
		validateActivationProbability(result);
		return result;
	}
	
	
	//-- Include messages
	
	public void setIncludeMessages(Boolean includeMessages) throws ParameterException{
		Validate.notNull(includeMessages);
		props.setProperty(AbstractTransformerProperty.INCLUDE_STATUS_MESSAGES.toString(), includeMessages.toString());
	}
	
	public Boolean getIncludeMessages() throws PropertyException{
		String propertyValue = props.getProperty(AbstractTransformerProperty.INCLUDE_STATUS_MESSAGES.toString());
		Boolean result = null;
		try{
			result = Boolean.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AbstractTransformerProperty.INCLUDE_STATUS_MESSAGES, propertyValue);
		}
		return result;
	}
	
	
	//-- Type
	
	public void setType(String className) throws ParameterException{
		Validate.notNull(className);
		props.setProperty(AbstractTransformerProperty.TYPE.toString(), className);
	}
	
	public String getType() throws ParameterException{
		String propertyValue = props.getProperty(AbstractTransformerProperty.TYPE.toString());
		Validate.notNull(propertyValue);
		return propertyValue;
	}
	
	
	//-- Default properties
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = new Properties();
		defaultProperties.setProperty(AbstractTransformerProperty.ACTIVATION_PROBABILITY.toString(), defaultActivationProbability.toString());
		defaultProperties.setProperty(AbstractTransformerProperty.INCLUDE_STATUS_MESSAGES.toString(), defaultIncludeStatusMessages.toString());
		defaultProperties.setProperty(AbstractTransformerProperty.TYPE.toString(), defaultType.toString());
		defaultProperties.setProperty(AbstractTransformerProperty.NAME.toString(), defaultName.toString());
		return defaultProperties;
	}
	
	
	//------- Parameter validation -------------------------------------------------------------------------
	
	protected void validateActivationProbability(Double probability) throws ParameterException{
		Validate.probability(probability);
	}
	
	protected enum AbstractTransformerProperty {
		NAME, TYPE, ACTIVATION_PROBABILITY, INCLUDE_STATUS_MESSAGES;
	}

}
