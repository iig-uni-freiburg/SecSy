package de.uni.freiburg.iig.telematik.secsy.logic.transformation;


import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;

/**
 * 
 * @author ts552
 *
 */
public class TraceTransformerEvent {
	
	public LogTrace<SimulationLogEntry> logTrace;
	public Object sender;
	
	public TraceTransformerEvent(LogTrace<SimulationLogEntry> logTrace, Object sender){
		Validate.notNull(logTrace);
		Validate.notNull(sender);
		this.logTrace = logTrace;
		this.sender = sender;
	}

}
