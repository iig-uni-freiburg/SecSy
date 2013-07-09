package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

public class TraceTransformerResult extends AbstractTransformerResult{
	
	private LogTrace logTrace;
	
	public TraceTransformerResult(LogTrace logTrace, boolean transformerApplied) throws ParameterException {
		super(transformerApplied);
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
