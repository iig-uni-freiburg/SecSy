package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;

public class TraceTransformerResult extends AbstractTransformerResult{
	
	private LogTrace<SimulationLogEntry> logTrace;
	
	public TraceTransformerResult(LogTrace<SimulationLogEntry> logTrace, boolean transformerApplied) throws ParameterException {
		super(transformerApplied);
		setLogTrace(logTrace);
		this.setCaseNumber(logTrace.getCaseNumber());
	}
	
	public void setLogTrace(LogTrace<SimulationLogEntry> logTrace) throws ParameterException{
		Validate.notNull(logTrace);
		this.logTrace = logTrace;
	}
	
	public LogTrace<SimulationLogEntry> getLogTrace(){
		return logTrace;
	}

}
