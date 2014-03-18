package de.uni.freiburg.iig.telematik.secsy.logic.util;

import java.util.List;

import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

public class TraceGenerationResult{
	
	public List<LogTrace<LogEntry>> traces = null;
	public int distinctActivitySequences = 0;
 
	public TraceGenerationResult(List<LogTrace<LogEntry>> traces, int distinctActivitySequences) {
		super();
		this.traces = traces;
		this.distinctActivitySequences = distinctActivitySequences;
	}
}
