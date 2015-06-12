package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;

public interface TraceListener {
	
	public void traceGenerated(LogTrace<SimulationLogEntry> trace);

}
