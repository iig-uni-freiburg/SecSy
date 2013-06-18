package logic.filtering.filter.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import log.LogEntry;
import logic.filtering.AbstractFilterResult;
import logic.filtering.TraceFilterEvent;
import logic.filtering.TraceFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AGPropertyEnforcementFilterProperties;
import logic.filtering.filter.properties.AbstractFilterProperties;
import properties.PropertyException;
import validate.ParameterException;
import validate.ParameterException.ErrorCode;
import validate.Validate;

/**
 * This class defined abstract behaviour for filters that apply to groups of activities within a trace, e.g. SoD or BoD constraints.
 * The activation probability is set to 1 to guarantee that on each trace,
 * the property is either ensured or violated.
 * 
 * @author Thomas Stocker
 */
public abstract class ActivityGroupPropertyEnforcementFilter extends AbstractTraceFilter {
	
	protected final String SUCCESSFULF_ENFORCEMENT = "Successful enforcements: %s";
	protected final String SUCCESSFULF_VIOLATION = "Successful violations: %s";
	protected final String UNSUCCESSFULF_ENFORCEMENT = "Unsuccessful enforcements: %s";
	protected final String UNSUCCESSFULF_VIOLATION = "Unsuccessful violations: %s";
	protected final String NONEEDF_ACTIVITIES_NOT_PRESENT = "No enforcement necessary - trace does not contain activity group %s";
	protected final String NONEEDF_ENFORCEMENT = "Property already ensured in group %s.";
	protected final String NONEEDF_VIOLATION = "Property already violated in group %s";
	protected final String ERRORF_CUSTOM = "[FILTER ERROR] %s Cannot enforce property on %%s";
	protected final String NOTICEF_ENFORCE = "Trying to %s property on group %s";
	protected final String NOTICEF_TRACE_RESULT = "Trace after transformation: %s";
	protected final String NOTICEF_TRACE = "Trace before transformation: %s";
	
	private double violationProbability = AGPropertyEnforcementFilterProperties.defaultViolationProbability;
	private List<Set<String>> activityGroups = new ArrayList<Set<String>>();
	
	public enum FilterAction {ENSURE, VIOLATE};

	public ActivityGroupPropertyEnforcementFilter(AGPropertyEnforcementFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
		violationProbability = properties.getViolationProbability();
		activityGroups = properties.getActivityGroups();
	}

	/**
	 * Creates a new ActivityGroupFilter according to the given parameters.<br>
	 * Generating a filter in this way will force the filter to ensure the property on all traces.
	 * @param filterType A String-description of the filter.
	 * @param includeMessages Indicates if the filter result should include status messages.
	 * @param activityGroups The activity groups for which the property should be applied.
	 * @throws ParameterException 
	 */
	public ActivityGroupPropertyEnforcementFilter(FilterType filterType, Set<String>... activityGroups) throws ParameterException {
		super(filterType, 1.0);
		Validate.notNull(activityGroups);
		for(Set<String> activityGroup: activityGroups){
			if(activityGroup == null){
				throw new ParameterException(ErrorCode.NULLPOINTER);
			}
			this.activityGroups.add(activityGroup);
		}
	}
	
	/**
	 * Creates a new ActivityGroupFilter according to the given parameters.<br>
	 * Generating a filter in this way allows to specify the probability with which the property is violated on a given trace.
	 * In all other cases the property will be enforced on the given trace.<br>
	 * Note that in case the filter decides to violate the property if tries to violate it for all activity groups.
	 * @param filterType A String-description of the filter.
	 * @param violationProbability The probability with which the property should be violated on a given trace.
	 * @param includeMessages Indicates if the filter result should include status messages.
	 * @param activityGroups The activity groups for which the property should be applied.
	 * @throws ParameterException 
	 */
	public ActivityGroupPropertyEnforcementFilter(FilterType filterType, double violationProbability, Set<String>... activityGroups) throws ParameterException {
		this(filterType, activityGroups);
	}
	
	public void setViolationProbability(double probability){
		this.violationProbability = probability;
	}
	
	public List<Set<String>> getActivityGroups(){
		return Collections.unmodifiableList(activityGroups);
	}
	
	public void setActivityGroups(Set<String>... activityGroups) throws ParameterException{
		Validate.notNull(activityGroups);
		Validate.noNullElements(activityGroups);
		this.activityGroups.clear();
		for(Set<String> activityGroup: activityGroups){
			this.activityGroups.add(activityGroup);
		}
	}
	
	public Double getViolationProbability() {
		return violationProbability;
	}

	/**
	 * Applies the transformation on the log trace.<br>
	 * The property is ensured or violated for ALL activity groups.
	 * @throws ParameterException 
	 */
	@Override
	protected TraceFilterResult applyTransformation(TraceFilterEvent event) throws ParameterException {
		Validate.notNull(event);
		TraceFilterResult result = null;
		try {
			result = new TraceFilterResult(event.logTrace, true);
		} catch (ParameterException e) {
			// Cannot happen, since TraceFilterEvent ensures non-null values for log traces.
			e.printStackTrace();
		}
		boolean ensureProperty = violationProbability==0.0 || rand.nextDouble()>violationProbability;
		String successfulFormat = SUCCESSFULF_ENFORCEMENT;
		String notSuccessfulFormat = UNSUCCESSFULF_ENFORCEMENT;
		if(!ensureProperty){
			successfulFormat = SUCCESSFULF_VIOLATION;
			notSuccessfulFormat = UNSUCCESSFULF_VIOLATION;
		}
		addMessageToResult(getNoticeMessage(String.format(NOTICEF_TRACE, event.logTrace)), result);
		//Ensure or violate the property for all activity groups.
		int counter = 0;
		EnforcementResult success;
		for(Set<String> activityGroup: activityGroups){
			addMessageToResult(getNoticeMessage(String.format(NOTICEF_ENFORCE, (ensureProperty ? "ensure" : "violate"),activityGroup)), result);
			List<LogEntry> correspondingEntries = event.logTrace.getEntriesForActivities(activityGroup);
			if(!correspondingEntries.isEmpty()){
				//Remove all traces that are not relevant for the transformation (e.g. do not contain enough information)
				removeIrrelevantEntries(correspondingEntries, result);
				if(!correspondingEntries.isEmpty()){
					//Ensure or violate the property in the entries that correspond to the actual activity group.
					Collections.shuffle(correspondingEntries);
					if(ensureProperty){
						success = ensureProperty(activityGroup, correspondingEntries, result);
					} else {
						success = violateProperty(activityGroup, correspondingEntries, result);
					}
					if(!success.equals(EnforcementResult.UNSUCCESSFUL))
						counter++;
				} else {
					//There are no traces left on which the transformation can be applied.
					addMessageToResult(getNoticeMessage(String.format(NONEEDF_ACTIVITIES_NOT_PRESENT, activityGroup)), result);
				}
			} else {
				addMessageToResult(getNoticeMessage(String.format(NONEEDF_ACTIVITIES_NOT_PRESENT, activityGroup)), result);
				counter++;
			}
		}
		addMessageToResult(getNoticeMessage(String.format(NOTICEF_TRACE_RESULT, event.logTrace)), result);
		if(counter<activityGroups.size()){
			//The property could not be ensured/violated for all activity groups.
			result.setFilterSuccess(false);
			addMessageToResult(getErrorMessage(String.format(notSuccessfulFormat, activityGroups.size()-counter)), result);
		} else {
			//The property could be ensured/violated for all activity groups.
			result.setFilterSuccess(true);
			addMessageToResult(getSuccessMessage(String.format(successfulFormat, counter)), result);
		}
		return result;
	}
	
	/**
	 * Removes entries from the trace that are irrelevant for the property, i.e. traces where specific fields are empty.
	 * @param entries Complete list of entries within the trace that.
	 * @param result The TraceFilterResult to be used for adding messages.
	 * @return A list containing relevant entries only.
	 * @throws ParameterException 
	 */
	protected List<LogEntry> removeIrrelevantEntries(List<LogEntry> entries, TraceFilterResult result) throws ParameterException{
		Validate.notNull(entries);
		Validate.noNullElements(entries);
		return entries;
	}
	
	/**
	 * Enforces the property on a given set of log entries.
	 * @param activityGroup The group of activities for which the property must hold.
	 * @param entries The entries that relate to one of the activities in the activity group.
	 * @param filterResult The FilterResult to be used for adding messages.
	 * @return Outcome of the enforcement procedure.
	 * @throws ParameterException 
	 * @see EnforcementResult
	 */
	protected EnforcementResult ensureProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractFilterResult filterResult) throws ParameterException{
		Validate.notNull(activityGroup);
		Validate.notEmpty(activityGroup);
		Validate.noNullElements(activityGroup);
		Validate.notNull(entries);
		Validate.notEmpty(entries);
		Validate.noNullElements(entries);
		Validate.notNull(filterResult);
		return EnforcementResult.SUCCESSFUL;
	}
	
	/**
	 * Violates the property on a given set of log entries.
	 * @param activityGroup The group of activities for which the property must not hold.
	 * @param entries The entries that relate to one of the activities in the activity group.
	 * @param filterResult The FilterResult to be used for adding messages.
	 * @return Outcome of the violation procedure.
	 * @throws ParameterException 
	 * @see EnforcementResult
	 */
	protected EnforcementResult violateProperty(Set<String> activityGroup, List<LogEntry> entries, AbstractFilterResult filterResult) throws ParameterException{
		Validate.notNull(activityGroup);
		Validate.notEmpty(activityGroup);
		Validate.noNullElements(activityGroup);
		Validate.notNull(entries);
		Validate.notEmpty(entries);
		Validate.noNullElements(entries);
		Validate.notNull(filterResult);
		return EnforcementResult.SUCCESSFUL;
	}
	
	
	@Override
	protected void fillProperties(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((AGPropertyEnforcementFilterProperties) properties).setViolationProbability(getViolationProbability());
		for(Set<String> activityGroup: activityGroups){
			((AGPropertyEnforcementFilterProperties) properties).addActivityGroup(activityGroup);
		}
	}



	/**
	 * Enumeration for possible enforcement results.<br>
	 * SUCCESSFUL: The property could be successfully enforced.<br>
	 * UNSUCCESSFUL: The property could not be enforced.<br>
	 * NOTNECESSARY: Either the (enforcement|violation) is not necessary, because the property trivially (holds|does not hold) in the trace.<br>
	 */
	protected enum EnforcementResult {SUCCESSFUL, UNSUCCESSFUL, NOTNECESSARY};

}
