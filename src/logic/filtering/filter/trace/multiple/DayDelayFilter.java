package logic.filtering.filter.trace.multiple;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import log.EntryField;
import log.LogEntry;
import log.LogTrace;
import logic.filtering.TraceFilterEvent;
import logic.filtering.TraceFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.DayDelayFilterProperties;
import misc.RandomUtils;
import properties.PropertyException;
import validate.ParameterException;

public class DayDelayFilter extends AbstractMultipleTraceFilter{
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: added delay of %s days";
	
	private int minDays;
	private int maxDays;
	
	private long delayInMilliseconds = 0;

	public DayDelayFilter(DayDelayFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
		setDayBounds(properties.getMinDays(), properties.getMaxDays());
	}
	
	public DayDelayFilter(double activationProbability, int maxAppliances, int minDays, int maxDays) throws ParameterException {
		super(FilterType.DAY_DELAY_FILTER, activationProbability, maxAppliances);
		setDayBounds(minDays, maxDays);
	}


	public void setDayBounds(int minDays, int maxDays) throws ParameterException{
		DayDelayFilterProperties.validateDayBounds(minDays, maxDays);
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
	protected TraceFilterResult applyTransformation(TraceFilterEvent event) throws ParameterException {
		// TODO Auto-generated method stub
		return super.applyTransformation(event);
	}

	@Override
	protected boolean applyEntryTransformation(LogEntry entry, TraceFilterResult filterResult) throws ParameterException {
		super.applyEntryTransformation(entry, filterResult);
		
		// Check, if timestamps can be altered for the entry itself and all its successors within the trace
		if(entry.isFieldLocked(EntryField.TIME)){
			addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field"), filterResult);
			return false;
		}
		for(LogEntry affectedEntry: filterResult.getLogTrace().getSucceedingEntries(entry)){
			if(affectedEntry.isFieldLocked(EntryField.TIME)){
				addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field in sucessing entry ("+affectedEntry.getActivity()+")"), filterResult);
				return false;
			}
		}
		
		int extraDays = RandomUtils.randomIntBetween(minDays, maxDays+1);
		delayInMilliseconds = 86400000 * extraDays;
		if(addTimeToEntry(entry, delayInMilliseconds)){
			addMessageToResult(getSuccessMessage(entry.getActivity(), extraDays), filterResult);
			return true;
		} else {
			// Should not happen, locking property is checked before
			addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due t olocked time-field"), filterResult);
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
	protected void fillProperties(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((DayDelayFilterProperties) properties).setDayBounds(getMinDays(), getMaxDays());
	}

	@Override
	public AbstractFilterProperties getProperties() throws ParameterException, PropertyException {
		DayDelayFilterProperties properties = new DayDelayFilterProperties();
		fillProperties(properties);
		return properties;
	}

	@Override
	protected void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryFilterSuccess) throws ParameterException {
		// This method is called when the start time of an entry is postponed by a day-delay
		// -> Adjust the start times of all following entries.
		if(entryFilterSuccess){
			// Start time for entry has been postponed.
			// Since locking properties are checked before in applyEntryTransformation, there should not occur any errors
			for(LogEntry succeedingEntry: logTrace.getSucceedingEntries(logEntry)){
				addTimeToEntry(succeedingEntry, delayInMilliseconds);
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		DayDelayFilter f = new DayDelayFilter(1, 1, 10, 20);
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
		TraceFilterEvent event = new TraceFilterEvent(t, new Integer(10));
		TraceFilterResult result = f.applyTransformation(event);
		System.out.println(t);
		System.out.println(result.getFilterMessages());
	}

}
