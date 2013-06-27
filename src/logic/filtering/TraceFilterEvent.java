package logic.filtering;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
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
