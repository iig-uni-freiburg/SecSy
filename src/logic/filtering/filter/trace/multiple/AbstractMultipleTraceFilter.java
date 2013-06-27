package logic.filtering.filter.trace.multiple;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

import logic.filtering.AbstractFilterResult;
import logic.filtering.TraceFilterEvent;
import logic.filtering.TraceFilterResult;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.AbstractMultipleTraceFilterProperties;
import logic.filtering.filter.trace.AbstractTraceFilter;

/**
 * 
 * 
 * @author Thomas Stocker
 */
public abstract class AbstractMultipleTraceFilter extends AbstractTraceFilter {

	protected final String TARGET_APPLIANCES_FORMAT = "target appliances: %s";
	protected final String UNSUCCESSFUL_APPLIANCES_FORMAT = "unsuccessful appliances: %s";
	protected final String SUCCESSFUL_APPLIANCES_FORMAT = "successful appliances: %s";
	/**
	 * General maximum number of appliances on a given trace.
	 */
	protected int maxAppliances = AbstractMultipleTraceFilterProperties.defaultMaxAppliances;
	
	/**
	 * target filter appliances for the actual trace.
	 */
	protected int targetAppliances;
	
	public AbstractMultipleTraceFilter(AbstractMultipleTraceFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
		maxAppliances = properties.getMaxAppliances();
	}

	public AbstractMultipleTraceFilter(FilterType filterType, double activationProbability, int maxAppliances) throws ParameterException {
		super(filterType, activationProbability);
		setMaxAppliances(maxAppliances);
	}
	
	public Integer getMaxAppliances(){
		return maxAppliances;
	}
	
	public void setMaxAppliances(Integer maxAppliances) throws ParameterException{
		Validate.bigger(maxAppliances, 0);
		this.maxAppliances = maxAppliances;
	}

	@Override
	protected TraceFilterResult applyTransformation(TraceFilterEvent event) throws ParameterException {
		TraceFilterResult result = new TraceFilterResult(event.logTrace, true);
		//Decide how many unauthorized executions are inserted
		determineAppliances(event.logTrace.size(), result);
		
		int successfulAppliances = 0;
		List<LogEntry> entries = new ArrayList<LogEntry>(event.logTrace.getEntries());
		Collections.shuffle(entries);
		Iterator<LogEntry> iter = entries.iterator();
		LogEntry nextEntry;
		while(successfulAppliances<targetAppliances && iter.hasNext()){
			nextEntry = iter.next();
			if(applyEntryTransformation(nextEntry, result)){
				traceFeedback(result.getLogTrace(), nextEntry, true);
				successfulAppliances++;
			} else traceFeedback(result.getLogTrace(), nextEntry, false);
		}
		if(successfulAppliances==0){
			result.setFilterSuccess(false);
			addMessageToResult(getErrorMessage(""), result);
		} else {
			result.setFilterSuccess(true);
			addMessageToResult(getSuccessMessage(String.format(SUCCESSFUL_APPLIANCES_FORMAT, successfulAppliances)), result);
			if(successfulAppliances<targetAppliances)
				addMessageToResult(getNoticeMessage(String.format(UNSUCCESSFUL_APPLIANCES_FORMAT, targetAppliances-successfulAppliances)), result);
		}
		return result;
	}
	
	protected abstract void traceFeedback(LogTrace logTrace, LogEntry logEntry, boolean entryFilterSuccess) throws ParameterException;
	
	protected void determineAppliances(int logEntries, AbstractFilterResult result) throws ParameterException{
		Validate.notNull(logEntries);
		while((targetAppliances=rand.nextInt(maxAppliances)+1)>logEntries){}
		addMessageToResult(getNoticeMessage(String.format(TARGET_APPLIANCES_FORMAT, targetAppliances)), result);
	}
	
	protected boolean applyEntryTransformation(LogEntry entry, TraceFilterResult filterResult) throws ParameterException {
		Validate.notNull(entry);
		Validate.notNull(filterResult);
		return true;
	}

	@Override
	protected void fillProperties(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((AbstractMultipleTraceFilterProperties) properties).setMaxAppliances(maxAppliances);
	}
	
	
	
	
}
