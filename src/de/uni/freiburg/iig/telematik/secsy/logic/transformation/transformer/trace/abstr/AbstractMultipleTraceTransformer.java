package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.AbstractTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractMultipleTraceTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;


/**
 * 
 * 
 * @author Thomas Stocker
 */
public abstract class AbstractMultipleTraceTransformer extends AbstractTraceTransformer {

	private static final long serialVersionUID = 5882420755943878025L;
	
	protected final String TARGET_APPLIANCES_FORMAT = "target appliances: %s";
	protected final String UNSUCCESSFUL_APPLIANCES_FORMAT = "unsuccessful appliances: %s";
	protected final String SUCCESSFUL_APPLIANCES_FORMAT = "successful appliances: %s";
	
	/**
	 * General maximum number of appliances on a given trace.
	 */
	protected int maxAppliances = AbstractMultipleTraceTransformerProperties.defaultMaxAppliances;
	
	/**
	 * target transformer appliances for the actual trace.
	 */
	protected int targetAppliances;
	
	protected Set<LogEntry> transformedEntries = new HashSet<LogEntry>();
	
	public AbstractMultipleTraceTransformer(AbstractMultipleTraceTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		maxAppliances = properties.getMaxAppliances();
	}

	public AbstractMultipleTraceTransformer(Double activationProbability, Integer maxAppliances) throws ParameterException {
		super(activationProbability);
		setMaxAppliances(maxAppliances);
	}
	
	public AbstractMultipleTraceTransformer() {
		super();
	}
	
	public Integer getMaxAppliances(){
		return maxAppliances;
	}
	
	public void setMaxAppliances(Integer maxAppliances) throws ParameterException{
		Validate.bigger(maxAppliances, 0);
		this.maxAppliances = maxAppliances;
	}

	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException {
		transformedEntries.clear();
		TraceTransformerResult result = new TraceTransformerResult(event.logTrace, true);
		
		//Decide on how many log entries, the filter is applied.
		determineAppliances(event.logTrace.size(), result);
		
		int successfulAppliances = 0;
		List<LogEntry> entries = new ArrayList<LogEntry>(event.logTrace.getEntries());
		Collections.shuffle(entries);
		Iterator<LogEntry> iter = entries.iterator();
		LogEntry nextEntry;
		while(successfulAppliances<targetAppliances && iter.hasNext()){
			nextEntry = iter.next();
			if(applyEntryTransformation(event.logTrace, nextEntry, result)){
				successfulAppliances++;
				transformedEntries.add(nextEntry);
			}
		}
		if(successfulAppliances==0){
			result.setTransformerSuccess(false);
			addMessageToResult(getErrorMessage(""), result);
		} else {
			result.setTransformerSuccess(true);
			addMessageToResult(getSuccessMessage(String.format(SUCCESSFUL_APPLIANCES_FORMAT, successfulAppliances)), result);
			if(successfulAppliances<targetAppliances)
				addMessageToResult(getNoticeMessage(String.format(UNSUCCESSFUL_APPLIANCES_FORMAT, targetAppliances-successfulAppliances)), result);
		}
		return result;
	}
	
	protected void determineAppliances(int logEntries, AbstractTransformerResult result) throws ParameterException{
		Validate.notNull(logEntries);
		while((targetAppliances=rand.nextInt(maxAppliances)+1)>logEntries){}
		addMessageToResult(getNoticeMessage(String.format(TARGET_APPLIANCES_FORMAT, targetAppliances)), result);
	}
	
	protected abstract boolean applyEntryTransformation(LogTrace trace, LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException;

	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((AbstractMultipleTraceTransformerProperties) properties).setMaxAppliances(maxAppliances);
	}
	
	
	
}
