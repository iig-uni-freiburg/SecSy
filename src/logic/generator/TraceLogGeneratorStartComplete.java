package logic.generator;

import java.io.IOException;
import java.util.Date;

import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;

import logic.generator.time.CaseTimeGenerator.ExecutionTime;

/**
 * 
 * @author Thomas Stocker
 *
 */
public class TraceLogGeneratorStartComplete extends TraceLogGenerator{
	
	public TraceLogGeneratorStartComplete(LogFormat logFormat) 
			throws IOException, PerspectiveException, ParameterException {
		super(logFormat);
	}
	
	public TraceLogGeneratorStartComplete(LogFormat logFormat, String fileName) 
			throws IllegalArgumentException, IOException, PerspectiveException, ParameterException {
		super(logFormat, fileName);
	}

	@Override
	protected void addEntryToTrace(LogTrace trace, LogEntry logEntry, ExecutionTime executionTime) throws LockingException {
		String group = String.valueOf(logEntry.hashCode());
		logEntry.setEventType(EventType.start);
		logEntry.setTimestamp(new Date(executionTime.startTime));
		logEntry.setGroup(group);
		trace.addEntry(logEntry);
		LogEntry completeEntry = logEntry.clone();
		completeEntry.setEventType(EventType.complete);
		completeEntry.setTimestamp(new Date(executionTime.endTime));
		completeEntry.setGroup(group);
		trace.addEntry(completeEntry);
	}
	
	

}
