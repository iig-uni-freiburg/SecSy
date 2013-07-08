package logic.transformation.transformer.trace.multiple;

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

import logic.transformation.TraceTransformerResult;
import logic.transformation.transformer.TransformerType;
import logic.transformation.transformer.properties.AbstractTransformerProperties;
import logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;

public class SkipActivitiesTransformer extends AbstractMultipleTraceTransformer {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry \"%s\" skipped";
	private Set<String> skipActivities = new HashSet<String>();
	
	public SkipActivitiesTransformer(SkipActivitiesTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		skipActivities = properties.getSkipActivities();
	}

	public SkipActivitiesTransformer(double activationProbability, int maxAppliances, Set<String> skipActivities) throws ParameterException {
		super(TransformerType.SKIP_ACTIVITIES, activationProbability, maxAppliances);
		setSkipActivities(skipActivities);
	}
	
	public Set<String> getSkipActivities(){
		return Collections.unmodifiableSet(skipActivities);
	}
	
	public void setSkipActivities(Set<String> skipActivities) throws ParameterException{
		SkipActivitiesTransformerProperties.validateSkipActivities(skipActivities);
		this.skipActivities.clear();
		this.skipActivities = skipActivities;
	}
	
	@Override
	protected boolean applyEntryTransformation(LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		super.applyEntryTransformation(entry, transformerResult);
		if(skipAllowed(entry.getActivity())){
			addMessageToResult(getCustomSuccessMessage(entry.getActivity()), transformerResult);
			return true;
		}
		return false;
	}
	
	protected boolean skipAllowed(String activity) throws ParameterException{
		Validate.notNull(activity);
		return skipActivities.contains(activity);
	}
	
	@Override
	protected void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryTransformerSuccess) throws ParameterException{
		Validate.notNull(logTrace);
		Validate.notNull(logEntry);
		if(entryTransformerSuccess){
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
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((SkipActivitiesTransformerProperties) properties).setSkipActivities(skipActivities);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		SkipActivitiesTransformerProperties properties = new SkipActivitiesTransformerProperties();
		fillProperties(properties);
		return properties;
	}
	
	

}
