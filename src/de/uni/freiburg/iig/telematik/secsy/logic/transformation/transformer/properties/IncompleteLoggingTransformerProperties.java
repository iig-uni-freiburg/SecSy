package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;


public class IncompleteLoggingTransformerProperties  extends AbstractMultipleTraceTransformerProperties {

	public IncompleteLoggingTransformerProperties() {
		super();
	}

	public IncompleteLoggingTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setSkipActivities(String... activities){
		validateSkipActivities(activities);
		props.setProperty(IncompleteLoggingTransformerProperty.SKIP_ACTIVITIES.toString(), ArrayUtils.toString(activities));
	}
	
	public void setSkipActivities(Set<String> activities){
		validateSkipActivities(activities);
		props.setProperty(IncompleteLoggingTransformerProperty.SKIP_ACTIVITIES.toString(), ArrayUtils.toString(activities.toArray()));
	}
	
	public Set<String> getSkipActivities() throws PropertyException{
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(IncompleteLoggingTransformerProperty.SKIP_ACTIVITIES.toString());
		if(propertyValue == null)
			throw new PropertyException(IncompleteLoggingTransformerProperty.SKIP_ACTIVITIES, propertyValue, "Cannot find skip activities");
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens())
			result.add(activityTokens.nextToken());
		return result;
	}
	
	public static void validateSkipActivities(String... activities){
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
	}
	
	public static void validateSkipActivities(Collection<String> activities){
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
	}

	private enum IncompleteLoggingTransformerProperty {
		SKIP_ACTIVITIES;
	}
	
}
