package logic.filtering.filter.entry;

import logic.filtering.EntryFilterEvent;
import logic.filtering.EntryFilterResult;
import logic.filtering.filter.AbstractFilter;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;
import properties.PropertyException;
import validate.ParameterException;
import validate.Validate;


public abstract class AbstractEntryFilter extends AbstractFilter {
	
	
	
	public AbstractEntryFilter(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}

	public AbstractEntryFilter(FilterType filterType, double activationProbability) throws ParameterException{
		super(filterType, activationProbability);
	}
	
	public EntryFilterResult filterLogEntry(EntryFilterEvent event) throws ParameterException{
		Validate.notNull(event);
		if(activationProbability==1.0 || rand.nextDouble()<=activationProbability){
			return applyTransformation(event);
		}
		try {
			return new EntryFilterResult(event.logEntry, event.caseNumber, false);
		} catch (ParameterException e) {
			// Cannot happen, since EntryFilterManager enforces non-null values for 
			// log entries and case numbers.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Subclasses are responsible to check parameter validity.
	 * @param event
	 * @return
	 */
	protected abstract EntryFilterResult applyTransformation(EntryFilterEvent event) throws ParameterException;

}
