package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.PropertyAwareTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractMultipleTraceTransformer;


public class SkipActivitiesTransformer extends AbstractMultipleTraceTransformer implements PropertyAwareTransformer{
	
	private static final long serialVersionUID = 4830613531108345650L;

	private final String CUSTOM_SUCCESS_FORMAT = "entry \"%s\" skipped";
	
	public static final String hint = "<html><p>A skip activities transformer removes single events from a" +
									  "process trace. To simulate skipping, timestamps of" +
									  "succeeding events are adjusted. transformer parameterization" +
									  "allows to specify a set of activities that may be skipped." +
									  "The number of appliances per trace is randomly chosen with" +
									  "an adjustable upper bound.</p></html>";

	private Set<String> skipActivities = new HashSet<String>();
	
	public SkipActivitiesTransformer(SkipActivitiesTransformerProperties properties) throws PropertyException {
		super(properties);
		skipActivities = properties.getSkipActivities();
	}

	public SkipActivitiesTransformer(Double activationProbability, Integer maxAppliances){
		super(activationProbability, maxAppliances);
	}
	
	/**
	 * Sets the transformer-specific properties and requires the following values:<br>
	 * <ul>
	 * <li><code>Set&ltString&gt</code>: Skip Activities.<br>
	 * Set of activities for which skipping is allowed.<br></li>
	 * </ul>
	 * @see #setSkipActivities(Set)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setProperties(Object[] properties) throws Exception {
		Validate.notNull(properties);
		Validate.notEmpty(properties);
		if(properties.length != 1)
			throw new ParameterException("Wrong number of parameters. Expected 1, but got " + properties.length);
		Validate.noNullElements(properties);
		Validate.type(properties[0], Set.class);
		Set<String> skipActivities = null;
		try {
			skipActivities = (Set<String>) properties[0];
		} catch(Exception e){
			throw new ParameterException("Wrong parameter type: " + e.getMessage());
		}
		setSkipActivities(skipActivities);
	}

	public Set<String> getSkipActivities(){
		return Collections.unmodifiableSet(skipActivities);
	}
	
	public void setSkipActivities(Set<String> skipActivities){
		SkipActivitiesTransformerProperties.validateSkipActivities(skipActivities);
		this.skipActivities.clear();
		this.skipActivities.addAll(skipActivities);
	}
	
	public boolean isValid(){
		return getTimeGenerator() != null;
	}
	
	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event){
		TraceTransformerResult result = super.applyTransformation(event);
		if(result.isSuccess()){
			for(LogEntry transformedEntry: transformedEntries){
				transformedEntry.lockField(EntryField.TIME, "Transformer-Enforcement: SkipActivities");
			}
		}
		return result;
	}
	
	@Override
	protected boolean applyEntryTransformation(LogTrace<SimulationLogEntry> trace, SimulationLogEntry entry, TraceTransformerResult transformerResult){
		if(!isValid()){
			addMessageToResult(getErrorMessage("Cannot apply transformer in invalid state: No time generator reference."), transformerResult);
			return false;
		}
		
		// Check, if timestamps can be altered for the successors of the entry within the trace
		for(LogEntry affectedEntry: transformerResult.getLogTrace().getSucceedingEntries(entry)){
			if(affectedEntry.isFieldLocked(EntryField.TIME)){
				addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot skip activity due to locked time-field in sucessing entry ("+affectedEntry.getActivity()+")"), transformerResult);
				return false;
			}
		}
		
		if(skipAllowed(entry.getActivity())){
			
			long timeCorrection = 0;
			
			if(entry.getGroup() == null){
				// The log entry does not belong to a group.
				// The log entry is the only event reporting on an activity
				// -> check if it is a START or END event.
				if(entry.getEventType() == EventType.start){
					timeCorrection = (trace.getDirectSuccessor(entry).getTimestamp().getTime() - entry.getTimestamp().getTime());
				} else if(entry.getEventType() == EventType.complete) {
					LogEntry predecessor = trace.getDirectPredecessor(entry);
					LogEntry succecessor = trace.getDirectSuccessor(entry);
					long diff = (predecessor.getTimestamp().getTime() - predecessor.getTimestamp().getTime());
					timeCorrection = diff - getTimeGenerator().getDelayFor(predecessor.getActivity()).getValueInMilliseconds() - getTimeGenerator().getDurationFor(succecessor.getActivity()).getValueInMilliseconds();
				} else {
					addMessageToResult(getErrorMessage("Unexpected event type: " + entry.getEventType()), transformerResult);
					return false;
				}
				
			} else {
				// The log entry belongs to a group.
				// The log entry either reports on the START of an activity and there is also 
				// a log entry reporting on the COMPLETE of the activity or the other way round.
				List<SimulationLogEntry> entryGroup = trace.getEntriesForGroup(entry.getGroup());
				
				// The log entry belongs to a group of events reporting on an activity
				// -> Check if there are both, START and END event
				SimulationLogEntry startEntry = null;
				SimulationLogEntry endEntry = null;
				switch(entryGroup.get(0).getEventType()){
				case start: 
					startEntry = entryGroup.get(0);
					break;
				case complete:
					endEntry = entryGroup.get(0);
					break;
					default:
						addMessageToResult(getErrorMessage("Unexpected event type: " + entryGroup.get(0).getEventType()), transformerResult);
						return false;	
				}
				
				switch(entryGroup.get(1).getEventType()){
				case start: 
					if(startEntry != null){
						addMessageToResult(getErrorMessage("Unexpected event type: " + entryGroup.get(1).getEventType()), transformerResult);
						return false;	
					}
					startEntry = entryGroup.get(1);
					break;
				case complete:
					if(endEntry != null){
						addMessageToResult(getErrorMessage("Unexpected event type: " + entryGroup.get(1).getEventType()), transformerResult);
						return false;	
					}
					endEntry = entryGroup.get(1);
					break;
					default:
						addMessageToResult(getErrorMessage("Unexpected event type: " + entryGroup.get(1).getEventType()), transformerResult);
						return false;	
				}
				
				timeCorrection = (trace.getDirectSuccessor(endEntry).getTimestamp().getTime() - startEntry.getTimestamp().getTime());
			}	
			
			try {
				correctSuccessorTime(trace, entry, timeCorrection);
			} catch (Exception e) {
				// Should not happen, since locking properties have been checked before.
				e.printStackTrace();
				return false;
			}
			
			addMessageToResult(getCustomSuccessMessage(entry.getActivity()), transformerResult);
			return true;
		}
		return false;
	}
	
	protected void correctSuccessorTime(LogTrace<SimulationLogEntry> trace, SimulationLogEntry entry, long timeCorrection) throws LockingException{
		
		if(timeCorrection == 0)
			return;
		if(timeCorrection < 0){
			for(SimulationLogEntry successor: trace.getSucceedingEntries(entry)){
				successor.addTime(timeCorrection);
			}
		} else if(timeCorrection > 0){
			for(SimulationLogEntry successor: trace.getSucceedingEntries(entry)){
				successor.subTime(timeCorrection);
			}
		}
	}
	
	protected boolean skipAllowed(String activity){
		Validate.notNull(activity);
		return skipActivities.contains(activity);
	}
	
	protected String getCustomSuccessMessage(String activity){
		Validate.notNull(activity);
		return getNoticeMessage(String.format(CUSTOM_SUCCESS_FORMAT, activity));
	}
	
	@Override
	public List<EntryField> requiredEntryFields() {
		return new ArrayList<EntryField>();
	}

	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws PropertyException {
		super.fillProperties(properties);
		((SkipActivitiesTransformerProperties) properties).setSkipActivities(skipActivities);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws PropertyException {
		SkipActivitiesTransformerProperties properties = new SkipActivitiesTransformerProperties();
		fillProperties(properties);
		return properties;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public boolean requiresTimeGenerator() {
		return true;
	}

	@Override
	public boolean requiresContext() {
		return false;
	}
	
	

}
