package de.uni.freiburg.iig.telematik.secsy.logic.transformation;


import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;

public class TraceTransformerResult extends AbstractTransformerResult{
	
	private LogTrace<SimulationLogEntry> logTrace;
	
	public TraceTransformerResult(LogTrace<SimulationLogEntry> logTrace, boolean transformerApplied){
		super(transformerApplied);
		setLogTrace(logTrace);
		this.setCaseNumber(logTrace.getCaseNumber());
	}
	
	public void setLogTrace(LogTrace<SimulationLogEntry> logTrace){
		Validate.notNull(logTrace);
		this.logTrace = logTrace;
	}
	
	public LogTrace<SimulationLogEntry> getLogTrace(){
		return logTrace;
	}

}
