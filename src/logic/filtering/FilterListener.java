package logic.filtering;

import logic.filtering.filter.AbstractFilter;

public interface FilterListener {
	
	public void filterMessage(String message);
	
	public void filterSuccess(AbstractFilter filter);
	
	public void filterFailure(AbstractFilter filter);

}
