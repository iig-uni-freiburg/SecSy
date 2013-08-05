package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple;

import java.util.Arrays;
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
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;


public class SkipActivitiesTransformer extends AbstractMultipleTraceTransformer {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry \"%s\" skipped";
	private Set<String> skipActivities = new HashSet<String>();
	
	private CaseTimeGenerator timeGenerator = null;
	
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
	
	public void setTimeGenerator(CaseTimeGenerator timeGenerator) throws ParameterException{
		Validate.notNull(timeGenerator);
		this.timeGenerator = timeGenerator;
	}
	
	public boolean isValid(){
		return timeGenerator != null;
	}
	
	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException {
		TraceTransformerResult result = super.applyTransformation(event);
		if(result.isSuccess()){
			for(LogEntry transformedEntry: transformedEntries){
				transformedEntry.lockField(EntryField.TIME, "Transformer-Enforcement: SkipActivities");
			}
		}
		return result;
	}
	
	@Override
	protected boolean applyEntryTransformation(LogTrace trace, LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		super.applyEntryTransformation(trace, entry, transformerResult);
		
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
					timeCorrection = diff - timeGenerator.getDelayFor(predecessor.getActivity()).getValueInMilliseconds() - timeGenerator.getDurationFor(succecessor.getActivity()).getValueInMilliseconds();
				} else {
					addMessageToResult(getErrorMessage("Unexpected event type: " + entry.getEventType()), transformerResult);
					return false;
				}
				
			} else {
				// The log entry belongs to a group.
				// The log entry either reports on the START of an activity and there is also 
				// a log entry reporting on the COMPLETE of the activity or the other way round.
				List<LogEntry> entryGroup = trace.getEntriesForGroup(entry.getGroup());
				
				// The log entry belongs to a group of events reporting on an activity
				// -> Check if there are both, START and END event
				LogEntry startEntry = null;
				LogEntry endEntry = null;
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
	
	protected void correctSuccessorTime(LogTrace trace, LogEntry entry, long timeCorrection) throws NullPointerException, LockingException{
		System.out.println("correct time");
		
		if(timeCorrection == 0)
			return;
		if(timeCorrection < 0){
			for(LogEntry successor: trace.getSucceedingEntries(entry)){
				successor.addTime(timeCorrection);
			}
		} else if(timeCorrection > 0){
			for(LogEntry successor: trace.getSucceedingEntries(entry)){
				successor.subTime(timeCorrection);
			}
		}
	}
	
	protected boolean skipAllowed(String activity) throws ParameterException{
		Validate.notNull(activity);
		return skipActivities.contains(activity);
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
