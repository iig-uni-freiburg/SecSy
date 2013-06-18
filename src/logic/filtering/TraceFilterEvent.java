package logic.filtering;

import validate.ParameterException;
import validate.Validate;
import log.LogTrace;

/**
 * 
 * @author ts552
 *
 */
public class TraceFilterEvent {
	
	public LogTrace logTrace;
	public Object sender;
	
	public TraceFilterEvent(LogTrace logTrace, Object sender) throws ParameterException {
		Validate.notNull(logTrace);
		Validate.notNull(sender);
		this.logTrace = logTrace;
		this.sender = sender;
	}

}
