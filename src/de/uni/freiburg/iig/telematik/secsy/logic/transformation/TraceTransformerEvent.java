package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

/**
 * 
 * @author ts552
 *
 */
public class TraceTransformerEvent {
	
	public LogTrace logTrace;
	public Object sender;
	
	public TraceTransformerEvent(LogTrace logTrace, Object sender) throws ParameterException {
		Validate.notNull(logTrace);
		Validate.notNull(sender);
		this.logTrace = logTrace;
		this.sender = sender;
	}

}
