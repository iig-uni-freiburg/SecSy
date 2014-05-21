package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;

public interface TraceListener {
	
	public void traceGenerated(LogTrace<SimulationLogEntry> trace);

}
