package logic.filtering;

import java.util.HashSet;
import java.util.Set;

import logic.filtering.filter.AbstractFilter;
import validate.ParameterException;
import validate.Validate;

public class FilterListenerSupport {
	
	private Set<FilterListener> listeners = new HashSet<FilterListener>();
	
	public void addFilterListener(FilterListener listener) throws ParameterException{
		Validate.notNull(listener);
		this.listeners.add(listener);
	}
	
	public void removeFilterListener(FilterListener listener){
		this.listeners.remove(listener);
	}
	
	public void fireFilterMessage(String message){
		for(FilterListener listener: listeners){
			listener.filterMessage(message);
		}
	}
	
	public void fireFilterSuccess(AbstractFilter filter){
		for(FilterListener listener: listeners){
			listener.filterSuccess(filter);
		}
	}

	public void fireFilterFailure(AbstractFilter filter){
		for(FilterListener listener: listeners){
			listener.filterFailure(filter);
		}
	}
	
}
