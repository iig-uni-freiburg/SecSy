package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import de.invation.code.toval.misc.RandomUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.DayDelayTransformerProperties;


public class DayDelayTransformer extends AbstractMultipleTraceTransformer{
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: added delay of %s days";
	
	private int minDays;
	private int maxDays;
	
	private long delayInMilliseconds = 0;

	public DayDelayTransformer(DayDelayTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		setDayBounds(properties.getMinDays(), properties.getMaxDays());
	}
	
	public DayDelayTransformer(double activationProbability, int maxAppliances, int minDays, int maxDays) throws ParameterException {
		super(TransformerType.DAY_DELAY, activationProbability, maxAppliances);
		setDayBounds(minDays, maxDays);
	}


	public void setDayBounds(int minDays, int maxDays) throws ParameterException{
		DayDelayTransformerProperties.validateDayBounds(minDays, maxDays);
		this.minDays = minDays;
		this.maxDays = maxDays;
	}
	
	public Integer getMinDays() {
		return minDays;
	}

	public Integer getMaxDays() {
		return maxDays;
	}
	
	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException {
		// TODO Auto-generated method stub
		return super.applyTransformation(event);
	}

	@Override
	protected boolean applyEntryTransformation(LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		super.applyEntryTransformation(entry, transformerResult);
		
		// Check, if timestamps can be altered for the entry itself and all its successors within the trace
		if(entry.isFieldLocked(EntryField.TIME)){
			addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field"), transformerResult);
			return false;
		}
		for(LogEntry affectedEntry: transformerResult.getLogTrace().getSucceedingEntries(entry)){
			if(affectedEntry.isFieldLocked(EntryField.TIME)){
				addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field in sucessing entry ("+affectedEntry.getActivity()+")"), transformerResult);
				return false;
			}
		}
		
		int extraDays = RandomUtils.randomIntBetween(minDays, maxDays+1);
		delayInMilliseconds = 86400000 * extraDays;
		if(addTimeToEntry(entry, delayInMilliseconds)){
			addMessageToResult(getSuccessMessage(entry.getActivity(), extraDays), transformerResult);
			return true;
		} else {
			// Should not happen, locking property is checked before
			addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due t olocked time-field"), transformerResult);
			return false;
		}
	}
	
	private boolean addTimeToEntry(LogEntry entry, long millseconds){
		try {
			entry.setTimestamp(new Date(entry.getTimestamp().getTime()+delayInMilliseconds));
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private String getSuccessMessage(String activityName, int extraDays){
		return getSuccessMessage(String.format(CUSTOM_SUCCESS_FORMAT, activityName, extraDays));
	}

	@Override
	public List<EntryField> requiredContextInformation() {
		return Arrays.asList(EntryField.TIME);
	}
	
	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((DayDelayTransformerProperties) properties).setDayBounds(getMinDays(), getMaxDays());
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		DayDelayTransformerProperties properties = new DayDelayTransformerProperties();
		fillProperties(properties);
		return properties;
	}

	@Override
	protected void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryTransformerSuccess) throws ParameterException {
		// This method is called when the start time of an entry is postponed by a day-delay
		// -> Adjust the start times of all following entries.
		if(entryTransformerSuccess){
			// Start time for entry has been postponed.
			// Since locking properties are checked before in applyEntryTransformation, there should not occur any errors
			for(LogEntry succeedingEntry: logTrace.getSucceedingEntries(logEntry)){
				addTimeToEntry(succeedingEntry, delayInMilliseconds);
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		DayDelayTransformer f = new DayDelayTransformer(1, 1, 10, 20);
		LogEntry e1 = new LogEntry("a1");
		e1.setTimestamp(new Date(System.currentTimeMillis()));
		LogEntry e2 = new LogEntry("a2");
		e2.setTimestamp(new Date(System.currentTimeMillis()+60000));
		LogEntry e3 = new LogEntry("a3");
		e3.setTimestamp(new Date(System.currentTimeMillis()+120000));
		LogTrace t = new LogTrace(1);
		t.addEntry(e1);
		t.addEntry(e2);
		t.addEntry(e3);
		System.out.println(t);
		TraceTransformerEvent event = new TraceTransformerEvent(t, new Integer(10));
		TraceTransformerResult result = f.applyTransformation(event);
		System.out.println(t);
		System.out.println(result.getTransformerMessages());
	}

}
