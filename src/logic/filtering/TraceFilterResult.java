package logic.filtering;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import log.LogTrace;

public class TraceFilterResult extends AbstractFilterResult{
	
	private LogTrace logTrace;
	
	public TraceFilterResult(LogTrace logTrace, boolean filterApplied) throws ParameterException {
		super(filterApplied);
		setLogTrace(logTrace);
		this.setCaseNumber(logTrace.getCaseNumber());
	}
	
	public void setLogTrace(LogTrace logTrace) throws ParameterException{
		Validate.notNull(logTrace);
		this.logTrace = logTrace;
	}
	
	public LogTrace getLogTrace(){
		return logTrace;
	}

}
