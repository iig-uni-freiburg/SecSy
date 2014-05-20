package de.uni.freiburg.iig.telematik.secsy.logic.generator.properties;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.misc.valuegeneration.StochasticValueGenerator;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.AttributeValueGenerator;


public class CaseDataContainerProperties extends AbstractProperties {
	
	private final String VALUE_GENERATOR_FORMAT = CaseDataContainerProperty.VALUE_GENERATOR + "_%s";
	private final String VALUE_GENERATOR_VALUE_FORMAT = "%s %s"; //attribute + type
	private final String VALUE_GENERATOR_PROBABILITY_FORMAT = CaseDataContainerProperty.VALUE_GENERATOR_PROBABILITY + "_%s_%s";
	private final String VALUE_GENERATOR_PROBABILITY_VALUE_FORMAT = "%s %s"; //value + probability
	private final String NUMBER_OF_PROBABILITIES_FORMAT = CaseDataContainerProperty.NUMBER_OF_PROBABILITIES + "_%s";
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(CaseDataContainerProperty containerProperty, Object value){
		props.setProperty(containerProperty.toString(), value.toString());
	}
	
	private String getProperty(CaseDataContainerProperty containerProperty){
		return props.getProperty(containerProperty.toString());
	}
	
		
	//-- Container name
		
	public void setName(String name) {
		validateStringValue(name);
		setProperty(CaseDataContainerProperty.CONTAINER_NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(CaseDataContainerProperty.CONTAINER_NAME);
		if(propertyValue == null)
			throw new PropertyException(CaseDataContainerProperty.CONTAINER_NAME, propertyValue);
		return propertyValue;
	}
	
	
//	//-- Context name
//
//	public void setContextName(String contextName) {
//		Validate.notNull(contextName);
//		Validate.notEmpty(contextName);
//		setProperty(CaseDataContainerProperty.CONTEXT_NAME, contextName);
//	}
//	
//	public String getContextName() throws PropertyException, ParameterException {
//		String propertyValue = getProperty(CaseDataContainerProperty.CONTEXT_NAME);
//		if(propertyValue == null)
//			throw new PropertyException(CaseDataContainerProperty.CONTEXT_NAME, propertyValue);
//		
//		validateStringValue(propertyValue);
//		
//		return propertyValue;
//	}
	
	
	//-- Attribute value generator
	
	public void setAttributeValueGenerator(AttributeValueGenerator generator) {
		Validate.notNull(generator);
		
		setDefaultValue(generator.getDefaultValue());
		for(String attribute: generator.getAttributes()){
			//TODO: Note, that this cast fails, if other value generators are permitted!!!
			addValueGenerator(attribute, (StochasticValueGenerator<?>) generator.getValueGenerator(attribute));
		}
	}
	
	public AttributeValueGenerator getAttributeValueGenerator() throws PropertyException{
		AttributeValueGenerator result = new AttributeValueGenerator();
		
		result.setDefaultValue(getDefaultValue());
		for(String valueGeneratorName: getValueGeneratorNames()){
			String attribute = null;
			try{
				attribute = valueGeneratorName.substring(CaseDataContainerProperty.VALUE_GENERATOR.toString().length()+1, valueGeneratorName.length());
			} catch(Exception e) {
				throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR, valueGeneratorName, "Invalid property value, cannot extract attribute name.");
			}
			result.setValueGeneration(attribute, getValueGenerator(valueGeneratorName));
		}
		
		return result;
	}
	
	
	//-- Default value
	
	private void setDefaultValue(Object defaultValue) {
		if(defaultValue == null){
			setProperty(CaseDataContainerProperty.DEFAULT_VALUE, "null");
			setProperty(CaseDataContainerProperty.DEFAULT_VALUE_TYPE, Object.class.getName().toString());
		} else {
			setProperty(CaseDataContainerProperty.DEFAULT_VALUE, defaultValue.toString());
			setProperty(CaseDataContainerProperty.DEFAULT_VALUE_TYPE, defaultValue.getClass().getName().toString());
		}
	}
	
	private Object getDefaultValue() throws PropertyException {
		String propertyValue = getProperty(CaseDataContainerProperty.DEFAULT_VALUE);
		String propertyValueType = getProperty(CaseDataContainerProperty.DEFAULT_VALUE_TYPE);
		if(propertyValue == null)
			return propertyValue;
		if(propertyValueType == null)
			throw new PropertyException(CaseDataContainerProperty.DEFAULT_VALUE_TYPE, propertyValue, "No type information for default value");
		
		if(propertyValue.equals("null"))
			return null;
		
		try {
			Class valueClass = Class.forName(propertyValueType);
			return valueClass.getDeclaredConstructor(String.class).newInstance(propertyValue);
		} catch (ClassNotFoundException e) {
			throw new PropertyException(CaseDataContainerProperty.DEFAULT_VALUE_TYPE, propertyValue, "Invalid value type (cannot load class): " + propertyValueType);
		} catch(Exception e){
			throw new PropertyException(CaseDataContainerProperty.DEFAULT_VALUE_TYPE, propertyValue, "Cannot cast value to class " + propertyValueType);
		}
	}

	
	//-- Value generator
	
	private void addValueGenerator(String attribute, StochasticValueGenerator<?> valueGenerator) {
		Validate.notNull(attribute);
		Validate.notNull(valueGenerator);
		if(!valueGenerator.isValid())
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Cannot add invalid value generator.");
		
		//1. Add the generator itself
		String propertyNameForNewGenerator = String.format(VALUE_GENERATOR_FORMAT, attribute);
		props.setProperty(propertyNameForNewGenerator, String.format(VALUE_GENERATOR_VALUE_FORMAT, '"'+attribute+'"', valueGenerator.getValueClass().getName()));
		Integer countProbabilities = 0;
		for(Object element: valueGenerator.getElements()){
			props.setProperty(String.format(VALUE_GENERATOR_PROBABILITY_FORMAT, attribute, ++countProbabilities),
							  String.format(VALUE_GENERATOR_PROBABILITY_VALUE_FORMAT, element, valueGenerator.getProbability(element).toString()));
		}
		props.setProperty(String.format(NUMBER_OF_PROBABILITIES_FORMAT, attribute), countProbabilities.toString());
		
		//2. Save a link in the list of generators
		Set<String> currentGenerators = getValueGeneratorNames();
		currentGenerators.add(propertyNameForNewGenerator);
		setProperty(CaseDataContainerProperty.VALUE_GENERATORS, ArrayUtils.toString(currentGenerators.toArray()));
	}
	
	private Set<String> getValueGeneratorNames(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(CaseDataContainerProperty.VALUE_GENERATORS);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	private StochasticValueGenerator<?> getValueGenerator(String valueGeneratorName) throws PropertyException{
		Validate.notNull(valueGeneratorName);
		
		String propertyValue = props.getProperty(valueGeneratorName);
		String attribute = null;
		String className = null;
		try{
			attribute = propertyValue.substring(1, propertyValue.lastIndexOf(' ')-1);
			className = propertyValue.substring(propertyValue.lastIndexOf(' ')+1);
		} catch(Exception e) {
			throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR, propertyValue, "Invalid property value, cannot extract attribute and class name.");
		}
		
		Class valueClass = null;
		try {
			valueClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR, propertyValue, "Invalid class name value.");
		}
	    
		StochasticValueGenerator valueGenerator = new StochasticValueGenerator();
		String countProbabilitiesPropertyValue = props.getProperty(String.format(NUMBER_OF_PROBABILITIES_FORMAT, attribute));
		if(countProbabilitiesPropertyValue == null)
			throw new PropertyException(CaseDataContainerProperty.NUMBER_OF_PROBABILITIES, propertyValue, "Cannot extract number of probabilities for attribute: " + attribute);
		
		Integer countProbabilities = null;
		try{
			countProbabilities = Integer.parseInt(countProbabilitiesPropertyValue);
		} catch(Exception e){
			throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR, propertyValue, "Cannot extract number of probabilities for attribute: " + attribute);
		}
		
		for(int i=1; i<=countProbabilities; i++){
			String probabilityPropertyValue = props.getProperty(String.format(VALUE_GENERATOR_PROBABILITY_FORMAT, attribute, i));
			if(probabilityPropertyValue == null)
				throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR_PROBABILITY, propertyValue, "Cannot extract probability for attribute: " + attribute);
			
			String keyString = null;
			String probabilityString = null;
			try{
				keyString = probabilityPropertyValue.substring(0, probabilityPropertyValue.indexOf(' '));
				probabilityString = probabilityPropertyValue.substring(probabilityPropertyValue.indexOf(' ')+1);
			}catch(Exception e){
				throw new PropertyException(CaseDataContainerProperty.VALUE_GENERATOR_PROBABILITY, propertyValue, "Invalid property value: Cannot extract probability and value information for attribute "+attribute);
			}
			
			Object key = null;
			Double probability = null;
			
			try{
				key = valueClass.getDeclaredConstructor(String.class).newInstance(keyString);
			}catch(Exception e){
				throw new PropertyException(CaseDataContainerProperty.NUMBER_OF_PROBABILITIES, propertyValue, "Invalid property value for attribute "+attribute+": value \""+keyString+"\"does not seem to be of type \""+valueClass.getName()+"\"");
			}
			try{
				probability = Double.parseDouble(probabilityString);
				Validate.probability(probability);
			}catch(Exception e){
				throw new PropertyException(CaseDataContainerProperty.NUMBER_OF_PROBABILITIES, propertyValue, "Invalid property value for attribute "+attribute+": value \""+probabilityString+"\"does not seem to be a probability");
			}
			valueGenerator.addProbability(key, probability);
		}
		return valueGenerator;
	}

	
}
