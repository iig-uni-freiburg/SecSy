package logic.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.invation.code.toval.misc.CollectionUtils;
import de.invation.code.toval.misc.FormatUtils;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

import logic.filtering.filter.exception.MissingRequirementException;
import logic.filtering.filter.trace.AbstractTraceFilter;
import logic.generator.LogEntryGenerator;

public class TraceFilterManager {
	
	private static final String filterSummaryFormat = "------------ Filter: %s\n" +
													  "Triggered in %s traces (%s%%)\n" +
													  "  successful: %s:\n%s\n" +
													  "unsuccessful: %s:\n%s\n";
	
	protected List<AbstractTraceFilter> traceFilters = new ArrayList<AbstractTraceFilter>();
	protected Map<AbstractTraceFilter, List<Integer>> succesfulAppliances = new HashMap<AbstractTraceFilter, List<Integer>>();
	protected Map<AbstractTraceFilter, List<Integer>> unsuccesfulAppliances = new HashMap<AbstractTraceFilter, List<Integer>>();
	
	protected LogEntryGenerator source;
	private Integer traces = 0;
	
	private FilterListenerSupport filterListenerSupport = new FilterListenerSupport();
	
	public void registerFilterListener(FilterListener listener) throws ParameterException{
		filterListenerSupport.addFilterListener(listener);
	}
	
	public void removeFilterListener(FilterListener listener){
		filterListenerSupport.removeFilterListener(listener);
	}
	
	public void setSource(LogEntryGenerator source) throws MissingRequirementException, ParameterException {
		Validate.notNull(source);
		try{
			for (AbstractTraceFilter filter : traceFilters) {
				for (EntryField contextType : filter.requiredContextInformation()) {
					if (!source.providesLogInformation(contextType))
						throw new MissingRequirementException(contextType);
				}
			}
		} catch(ParameterException e){
			// Cannot happen, since filters are required to never return null-values
			// in the method filter.requiredContextInformation().
			e.printStackTrace();
		}
		this.source = source;
	}
	
	public List<AbstractTraceFilter> getTraceFilters(){
		return Collections.unmodifiableList(traceFilters);
	}
	
	public boolean isEmpty(){
		return traceFilters.isEmpty();
	}
	
	/**
	 * Adds the given distortion filters to the set of managed filters.
	 * @param traceFilter Set of distortion filters.
	 * @throws MissingRequirementException 
	 * @throws ParameterException 
	 */
	public void addFilter(AbstractTraceFilter traceFilter) throws MissingRequirementException, ParameterException{
		Validate.notNull(traceFilter);
		try{
			if(source!=null)
				for(EntryField contextType: traceFilter.requiredContextInformation())
					if(!source.providesLogInformation(contextType))
						throw new MissingRequirementException(contextType);
		} catch(ParameterException e){
			// Cannot happen, since filters are required to never return null-values
			// in the method filter.requiredContextInformation().
			e.printStackTrace();
		}
		traceFilters.add(traceFilter);
	}
	
	public void applyFilters(LogTrace logTrace) throws ParameterException{
		if(!traceFilters.isEmpty()){
			TraceFilterEvent event = new TraceFilterEvent(logTrace, source);
			for(AbstractTraceFilter traceFilter: traceFilters){
				AbstractFilterResult filterResult = traceFilter.filterLogTrace(event);
				if(filterResult.wasFilterApplied()){
					if(filterResult.containsMessages()){
						filterListenerSupport.fireFilterMessage(filterResult.getFilterMessages()+"\n\n");
					}
				
					if(filterResult.isSuccess()){
						incSuccessfulAppliances(traceFilter, logTrace.getCaseNumber());
						filterListenerSupport.fireFilterSuccess(traceFilter);
					} else {
						incUnsuccessfulAppliances(traceFilter, logTrace.getCaseNumber());
						filterListenerSupport.fireFilterFailure(traceFilter);
					}
				} else {
					//Filter not applied
				}
			}
		}
		traces++;
	}
	
	private void incSuccessfulAppliances(AbstractTraceFilter filter, Integer caseID){
		if(!succesfulAppliances.containsKey(filter)){
			succesfulAppliances.put(filter, new ArrayList<Integer>());
		}
		succesfulAppliances.get(filter).add(caseID);
	}
	
	private void incUnsuccessfulAppliances(AbstractTraceFilter filter, Integer caseID){
		if(!unsuccesfulAppliances.containsKey(filter)){
			unsuccesfulAppliances.put(filter, new ArrayList<Integer>());
		}
		unsuccesfulAppliances.get(filter).add(caseID);
	}
	
	private Integer getSuccessfulAppliances(AbstractTraceFilter filter){
		if(!succesfulAppliances.containsKey(filter)){
			return 0;
		}
		return  succesfulAppliances.get(filter).size();
	}
	
	private Integer getUnsuccessfulAppliances(AbstractTraceFilter filter){
		if(!unsuccesfulAppliances.containsKey(filter)){
			return 0;
		}
		return  unsuccesfulAppliances.get(filter).size();
	}
	
	private Integer getAppliances(AbstractTraceFilter filter){
		return getSuccessfulAppliances(filter) + getUnsuccessfulAppliances(filter);
	}
	
	public String getFilterSummary(){
		StringBuilder builder = new StringBuilder();
		builder.append("Filter Summary:\n\n");
		for(AbstractTraceFilter filter: getTraceFilters()){
			builder.append(getFilterSummary(filter));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	private String getFilterSummary(AbstractTraceFilter filter){
		return String.format(filterSummaryFormat, filter.getName(),
												  getAppliances(filter), FormatUtils.format((getAppliances(filter)/traces.doubleValue())*100.0, 2),
												  getSuccessfulAppliances(filter), succesfulAppliances.containsKey(filter) ? CollectionUtils.toString(succesfulAppliances.get(filter)) : "[]",
												  getUnsuccessfulAppliances(filter), unsuccesfulAppliances.containsKey(filter) ? CollectionUtils.toString(unsuccesfulAppliances.get(filter)) : "[]");
	}
	
	
	public void reset(){
		traces = 0;
		succesfulAppliances.clear();
		unsuccesfulAppliances.clear();
	}
	
	public void clear(){
		traceFilters.clear();
	}

}
