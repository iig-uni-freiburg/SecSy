package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.ActivityGroupPropertyEnforcementTransformer.TransformerAction;


public abstract class AGPropertyEnforcementTransformerProperties extends AbstractTransformerProperties {
	
	private final String ACTIVITY_GROUP_FORMAT = AGPEProperty.ACTIVITY_GROUP + "_%s";
	
	public static final Double defaultViolationProbability = 0.0;
	public static final Integer defaultNumberOfGroups = 0;
	public static final TransformerAction defaultTransformerAction = TransformerAction.ENSURE;
	

	public AGPropertyEnforcementTransformerProperties() {
		super();
	}

	public AGPropertyEnforcementTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setViolationProbability(Double probability) {
		Validate.probability(probability);
		props.setProperty(AGPEProperty.VIOLATION_PROBABILITY.toString(), probability.toString());
	}
	
	public Double getViolationProbability() throws PropertyException{
		String propertyValue = props.getProperty(AGPEProperty.VIOLATION_PROBABILITY.toString());
		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AGPEProperty.VIOLATION_PROBABILITY, propertyValue);
		}
		Validate.probability(result);
		return result;
	}
	
	public void addActivityGroup(String... group) throws PropertyException{
		validateActivityGroup(group);
		props.setProperty(String.format(ACTIVITY_GROUP_FORMAT, incNumberOfGroups()), ArrayUtils.toString(group));
	}
	
	public void addActivityGroup(Set<String> group) throws PropertyException{
		validateActivityGroup(group);
		props.setProperty(String.format(ACTIVITY_GROUP_FORMAT, incNumberOfGroups()), ArrayUtils.toString(group.toArray()));
	}
	
	private Integer incNumberOfGroups() throws PropertyException{
		Integer currentNumberOfGroups = getNumberOfGroups();
		setNumberOfGroups(currentNumberOfGroups + 1);
		return currentNumberOfGroups + 1;
	}
	
	private void setNumberOfGroups(Integer number){
		Validate.notNull(number);
		Validate.notNegative(number);
		props.setProperty(AGPEProperty.NUMBER_OF_GROUPS.toString(), number.toString());
	}
	
	private Integer getNumberOfGroups() throws PropertyException{
		String propertyValue = props.getProperty(AGPEProperty.NUMBER_OF_GROUPS.toString());
		Integer result = null;
		try{
			result = Integer.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(AGPEProperty.NUMBER_OF_GROUPS, propertyValue);
		}
		Validate.notNegative(result);
		return result;
	}
	
	public List<Set<String>> getActivityGroups() throws PropertyException{
		List<Set<String>> groups = new ArrayList<Set<String>>();
		Integer numberOfGroups = getNumberOfGroups();
		for(int i=1; i<= numberOfGroups; i++){
			groups.add(getActivityGroup(i));
		}
		return groups;
	}
	
	private Set<String> getActivityGroup(Integer number) throws PropertyException{
		String propertyValue = props.getProperty(String.format(ACTIVITY_GROUP_FORMAT, number));
		if(propertyValue == null)
			throw new PropertyException(AGPEProperty.ACTIVITY_GROUP, propertyValue, "Inconsistent property file");
		
		Set<String> result = new HashSet<String>();
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens())
			result.add(activityTokens.nextToken());
		return result;
	}
	
	@Override
	protected void validateActivationProbability(Double probability){
		super.validateActivationProbability(probability);
		if(probability != 1.0)
			throw new ParameterException(ErrorCode.RANGEVIOLATION, "Activation probability has to be 1.0");
	}
	
	public static void validateActivityGroup(Set<String> group){
		Validate.notNull(group);
		Validate.notEmpty(group);
		Validate.noNullElements(group);
	}
	
	public static void validateActivityGroup(String[] group){
		Validate.notNull(group);
		Validate.notEmpty(group);
		Validate.noNullElements(group);
	}
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(AGPEProperty.VIOLATION_PROBABILITY.toString(), defaultViolationProbability.toString());
		defaultProperties.setProperty(AGPEProperty.NUMBER_OF_GROUPS.toString(), defaultNumberOfGroups.toString());
		return defaultProperties;
	}

	private enum AGPEProperty {
		VIOLATION_PROBABILITY, NUMBER_OF_GROUPS, ACTIVITY_GROUP;
	}
	
}
