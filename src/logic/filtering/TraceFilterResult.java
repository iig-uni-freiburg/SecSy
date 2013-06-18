package logic.filtering;

import validate.ParameterException;
import validate.Validate;
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
