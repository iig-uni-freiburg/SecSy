package logic.filtering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import log.EntryField;
import log.LogEntry;
import logic.filtering.filter.entry.AbstractEntryFilter;
import logic.filtering.filter.exception.MissingRequirementException;
import logic.generator.DetailedLogEntryGenerator;
import logic.generator.LogEntryGenerator;
import validate.ParameterException;
import validate.Validate;

/**
 * This class manages log filters which operate on log entries.<br>
 * Entry filter managers are used in log entry generators to apply
 * filters to log entries after their creation.<br>
 * The compatibility of filters with the corresponding log entry
 * generator is checked when the filter manager source (= log entry generator)
 * is set and when filters are added. Filters are compatible with a log entry 
 * generator when the generator provides all information required by the filter.
 * <br>
 * Filters are applied in the order they are added to the filter manager.
 * 
 * @author Thomas Stocker
 */
public class EntryFilterManager {
	/**
	 * The list of managed log entry filters.
	 */
	protected List<AbstractEntryFilter> entryFilters = new ArrayList<AbstractEntryFilter>();
	/**
	 * The filter manager source (= log entry generator).
	 */
	protected LogEntryGenerator entryGenerator;
	
	/**
	 * Sets the source of the filter manager (= entry generator).
	 * When the source is set, the compatibility of all managed filters is checked.
	 * Filters are incompatible when they require information which is not provided by
	 * the log entry generator.
	 * 
	 * @param source The log entry generator to be used as filter manager source.
	 * @throws ParameterException 
	 * @throws IllegalArgumentException If the given log entry generator is incompatible to one of the managed filters.
	 */
	public void setSource(LogEntryGenerator source) throws ParameterException {
		Validate.notNull(source);
		try{
			for(AbstractEntryFilter filter: entryFilters){
				for(EntryField contextType: filter.requiredContextInformation()){
					if(!source.providesLogInformation(contextType))
						throw new IllegalArgumentException("Filter requirement ("+contextType+") cannot be provided by source.");
				}
			}
		}catch(ParameterException e){
			// Cannot happen, since filters are required to never return null-values
			// in the method filter.requiredContextInformation().
			e.printStackTrace();
		}
		this.entryGenerator = source;
	}
	
	public List<AbstractEntryFilter> getEntryFilters(){
		return Collections.unmodifiableList(entryFilters);
	}
	
	/**
	 * Adds the given distortion filters to the set of managed filters.<br>
	 * When filters are added, their compatibility with the filter manager
	 * source (= entry generator) is checked. Filters are incompatible when they
	 * require information which is not provided by the log entry generator.
	 * 
	 * @param entryFilter
	 *            Set of distortion filters.
	 * @throws MissingRequirementException 
	 * @throws ParameterException 
	 * @throws Exception
	 *             If the given entry filter is incompatible with the filter
	 *             manager source.
	 */
	public void addFilter(AbstractEntryFilter entryFilter) throws MissingRequirementException, ParameterException {
		Validate.notNull(entryFilter);
		try {
			if (entryGenerator != null)
				for (EntryField contextType : entryFilter.requiredContextInformation())
					if (!entryGenerator.providesLogInformation(contextType))
						throw new MissingRequirementException(contextType);
		} catch (ParameterException e) {
			// Cannot happen, since filters are required to never return
			// null-values in the method filter.requiredContextInformation().
			e.printStackTrace();
		}
		this.entryFilters.add(entryFilter);
	}

	/**
	 * Applies all managed filters to the given log entry.<br>
	 * As some filters may need the corresponding case number, this information is also required.<br>
	 * Note: This method assumed that the given log entry is compatible with the managed filters,
	 * i.e. it provides sufficient information.
	 * Compatibility is checked when filter managers are added to log entry generators.
	 * 
	 * @see LogEntryGenerator
	 * @see DetailedLogEntryGenerator
	 * @param logEntry
	 * @param caseNumber
	 * @throws ParameterException 
	 */
	public void applyFilters(LogEntry logEntry, int caseNumber) throws ParameterException{
		EntryFilterEvent event = new EntryFilterEvent(logEntry, caseNumber, entryGenerator);
		for(AbstractEntryFilter tl: entryFilters){
			AbstractFilterResult filterResult = tl.filterLogEntry(event);
			if(filterResult.containsMessages()){
//				System.out.println(filterResult.getFilterMessages());
//				System.out.println();
			}
		}
	}
	
	public void clear(){
		entryFilters.clear();
	}

}
