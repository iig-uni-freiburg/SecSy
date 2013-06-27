package logic.filtering.filter.trace.multiple;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

import logic.filtering.TraceFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.SkipActivitiesFilterProperties;

public class SkipActivitiesFilter extends AbstractMultipleTraceFilter {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry \"%s\" skipped";
	private Set<String> skipActivities = new HashSet<String>();
	
	public SkipActivitiesFilter(SkipActivitiesFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
		skipActivities = properties.getSkipActivities();
	}

	public SkipActivitiesFilter(double activationProbability, int maxAppliances, Set<String> skipActivities) throws ParameterException {
		super(FilterType.SKIP_ACTIVITIES_FILTER, activationProbability, maxAppliances);
		setSkipActivities(skipActivities);
	}
	
	public Set<String> getSkipActivities(){
		return Collections.unmodifiableSet(skipActivities);
	}
	
	public void setSkipActivities(Set<String> skipActivities) throws ParameterException{
		SkipActivitiesFilterProperties.validateSkipActivities(skipActivities);
		this.skipActivities.clear();
		this.skipActivities = skipActivities;
	}
	
	@Override
	protected boolean applyEntryTransformation(LogEntry entry, TraceFilterResult filterResult) throws ParameterException {
		super.applyEntryTransformation(entry, filterResult);
		if(skipAllowed(entry.getActivity())){
			addMessageToResult(getCustomSuccessMessage(entry.getActivity()), filterResult);
			return true;
		}
		return false;
	}
	
	protected boolean skipAllowed(String activity) throws ParameterException{
		Validate.notNull(activity);
		return skipActivities.contains(activity);
	}
	
	@Override
	protected void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryFilterSuccess) throws ParameterException{
		Validate.notNull(logTrace);
		Validate.notNull(logEntry);
		if(entryFilterSuccess){
			logTrace.removeAllEntries(logTrace.getEntriesForGroup(logEntry.getGroup()), true);
		}
	}
	
	protected String getCustomSuccessMessage(String activity) throws ParameterException{
		Validate.notNull(activity);
		return getNoticeMessage(String.format(CUSTOM_SUCCESS_FORMAT, activity));
	}
	
	@Override
	public List<EntryField> requiredContextInformation() {
		return Arrays.asList();
	}

	@Override
	protected void fillProperties(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((SkipActivitiesFilterProperties) properties).setSkipActivities(skipActivities);
	}

	@Override
	public AbstractFilterProperties getProperties() throws ParameterException, PropertyException {
		SkipActivitiesFilterProperties properties = new SkipActivitiesFilterProperties();
		fillProperties(properties);
		return properties;
	}
	
	

}
