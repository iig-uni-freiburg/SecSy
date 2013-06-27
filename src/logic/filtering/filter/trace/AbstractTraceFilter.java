package logic.filtering.filter.trace;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import logic.filtering.TraceFilterEvent;
import logic.filtering.TraceFilterResult;
import logic.filtering.filter.AbstractFilter;
import logic.filtering.filter.FilterType;
import logic.filtering.filter.properties.AbstractFilterProperties;

/**
 * abstract class for filters that apply to whole log traces instead of single log entries.
 * This is particularly helpful in cases where properties must be ensured/violated that apply to a set of entries.<br>
 * <br>
 * The class ensures that the filter appliances respect the activation probability.
 * @author Thomas Stocker
 */
public abstract class AbstractTraceFilter extends AbstractFilter {
	
	
	
	public AbstractTraceFilter(AbstractFilterProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}

	/**
	 * Creates a new TraceFilter according to the given parameters.
	 * @param filterType A String-description of the filter.
	 * @param activationProbability Probability for applying the filter.<br>
	 * If the probability is 1, the filter is always applied.
	 * @param includeMessages Indicates if the filter result should include status messages.
	 * @throws ParameterException 
	 */
	public AbstractTraceFilter(FilterType filterType, double activationProbability) throws ParameterException{
		super(filterType, activationProbability);
	}
	
	/**
	 * Applies the filter to a log trace.<br>
	 * It checks if the activation probability allows the filter to be applied
	 * (either it is 1 or a newly generated random number is smaller or equal than the activation probability).
	 * In case of an appliance it calls a subroutine for the transformation itself.
	 * @param event Contains the LogTrace as such together with information on the caller of the filter routine (e.g. a LogEntryGenerator).
	 * @return Information on the success of the filter appliance together with filter messages.
	 * @throws ParameterException 
	 * @see AbstractTraceFilter#applyTransformation(TraceFilterEvent);
	 */
	public TraceFilterResult filterLogTrace(TraceFilterEvent event) throws ParameterException{
		Validate.notNull(event);
		if(activationProbability==1.0 || rand.nextDouble()<=activationProbability){
			return applyTransformation(event);
		}
		try {
			return new TraceFilterResult(event.logTrace, false);
		} catch (ParameterException e) {
			// Cannot happen, since TraceFilterEvent enforces non-null values for log traces.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Transforms a trace according to the aim of the trace filter.
	 * @param event Contains the LogTrace as such together with information on the caller of the filter routine (e.g. a LogEntryGenerator).
	 * @return Information on the success of the filter appliance together with filter messages.
	 */
	protected abstract TraceFilterResult applyTransformation(TraceFilterEvent event) throws ParameterException;

}
