package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.misc.valuegeneration.ValueGenerator;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


/**
 * This class generates values for data attributes.<br>
 * It provides methods to set appropriate value generators for data attributes.
 * 
 * @see ValueGenerator
 * @see ValueGeneratorFactory
 * @author Thomas Stocker
 */
public class AttributeValueGenerator {
	/**
	 * Value generators for attributes.
	 */
	private Map<String, ValueGenerator<?>> valueGenerators = new HashMap<String, ValueGenerator<?>>();
	/**
	 * The default value for attributes without value generator.
	 */
	private Object defaultValue = null;
	private Class defaultValueType = Object.class;
	
	/**
	 * Sets the default value which is returned when new values for attributes are requested
	 * but there is no corresponding value generator.
	 * @param defaultValue Default value for attributes without value generator.
	 */
	public void setDefaultValue(Object defaultValue){
		this.defaultValue = defaultValue;
		if(defaultValue != null){
			this.defaultValueType = defaultValue.getClass();
		} else {
			defaultValueType = Object.class;
		}
	}
	
	public Object getDefaultValue(){
		return defaultValue;
	}
	
	public Map<String, ValueGenerator<?>> getValueGenerators(){
		return Collections.unmodifiableMap(valueGenerators);
	}
	
	/**
	 * Sets the value generator for an attribute.<br>
	 * @param attribute Name of the attribute for which the value generator is set.
	 * @param valueGenerator Value generator for the attribute.
	 * @throws ParameterException 
	 */
	public void setValueGeneration(String attribute, ValueGenerator<?> valueGenerator) throws ParameterException{
		Validate.notNull(attribute);
		Validate.notNull(valueGenerator);
//		if(!valueGenerator.isValid())
//			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "ValueGenerator is not in valid state");
		valueGenerators.put(attribute, valueGenerator);
	}
	
	public void removeValueGenerator(String attribute){
		valueGenerators.remove(attribute);
	}
	
	public ValueGenerator<?> getValueGenerator(String attribute) throws ParameterException{
		Validate.notNull(attribute);
		return valueGenerators.get(attribute);
	}
	
	public Set<String> getAttributes(){
		return Collections.unmodifiableSet(valueGenerators.keySet());
	}
	
	public Class getAttributeValueClass(String attribute) throws ParameterException{
		Validate.notNull(attribute);
		if(!valueGenerators.containsKey(attribute))
			return null;
		return valueGenerators.get(attribute).getValueClass();
	}
	

	/**
	 * Returns a new value for the given attribute.<br>
	 * New values are generated using the value generator which was previously set
	 * for the given attribute. If there is not value generator, it returns the default value.
	 * 
	 * @param attribute Name of the attribute for which a new value is requested.
	 * @return A newly generated value for the given attribute;<br>
	 * or the default value if there is not value generator.
	 * @throws ParameterException If the given attribute is null.
	 * @throws ValueGenerationException 
	 * @throws Exception If the value generator throws an Exception.
	 * @see {@link #setDefaultValue(Object)}
	 */
	public Object getNewValueFor(String attribute) throws ParameterException {
		Validate.notNull(attribute);
		if(!valueGenerators.containsKey(attribute)){
			return defaultValue;
		}
		Object nextValue = null;
		try{
			nextValue = valueGenerators.get(attribute).getNextValue();
		} catch(ValueGenerationException e){
			// Cannot happen, since only valid value generators are accepted
			// and valid value generators cannot enter an invalid state.
		}
		return nextValue;
	}
	
	@Override
	public AttributeValueGenerator clone(){
		AttributeValueGenerator result = new AttributeValueGenerator();
		result.setDefaultValue(defaultValue);
		try{
			for(String attribute: getAttributes()){
				result.setValueGeneration(attribute, getValueGenerator(attribute).clone());
			}
		}catch(ParameterException e){
			return null;
		}
		return result;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("AttributeValueGenerator:");
		builder.append('\n');
		for(String attribute: getAttributes()){
			builder.append("attribute: " + attribute);
			builder.append('\n');
			try {
				builder.append(getValueGenerator(attribute));
			} catch (ParameterException e) {}
		}
		return builder.toString();
	}

}
