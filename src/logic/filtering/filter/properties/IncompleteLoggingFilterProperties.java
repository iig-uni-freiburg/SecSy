package logic.filtering.filter.properties;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import misc.ArrayUtils;
import misc.StringUtils;
import properties.PropertyException;
import validate.ParameterException;
import validate.Validate;

public class IncompleteLoggingFilterProperties  extends AbstractMultipleTraceFilterProperties {

	public IncompleteLoggingFilterProperties() {
		super();
	}

	public IncompleteLoggingFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setSkipActivities(String... activities) throws ParameterException {
		validateSkipActivities(activities);
		props.setProperty(IncompleteLoggingFilterProperty.SKIP_ACTIVITIES.toString(), ArrayUtils.toString(activities));
	}
	
	public void setSkipActivities(Set<String> activities) throws ParameterException {
		validateSkipActivities(activities);
		props.setProperty(IncompleteLoggingFilterProperty.SKIP_ACTIVITIES.toString(), ArrayUtils.toString(activities.toArray()));
	}
	
	public Set<String> getSkipActivities() throws PropertyException{
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(IncompleteLoggingFilterProperty.SKIP_ACTIVITIES.toString());
		if(propertyValue == null)
			throw new PropertyException(IncompleteLoggingFilterProperty.SKIP_ACTIVITIES, propertyValue, "Cannot find skip activities");
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens())
			result.add(activityTokens.nextToken());
		return result;
	}
	
	public static void validateSkipActivities(String... activities) throws ParameterException{
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
	}
	
	public static void validateSkipActivities(Collection<String> activities) throws ParameterException{
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		Validate.noNullElements(activities);
	}

	private enum IncompleteLoggingFilterProperty {
		SKIP_ACTIVITIES;
	}
	
}
