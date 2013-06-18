package logic.filtering;

import validate.ParameterException;
import validate.Validate;
import log.LogEntry;

public class EntryFilterResult extends AbstractFilterResult{
	
	private LogEntry logEntry;
	
	public EntryFilterResult(LogEntry logEntry, int caseNumber, boolean filterApplied) throws ParameterException {
		super(filterApplied);
		setLogEntry(logEntry);
		setCaseNumber(caseNumber);
	}
	
	public void setLogEntry(LogEntry logEntry) throws ParameterException{
		Validate.notNull(logEntry);
		this.logEntry = logEntry;
	}
	
	public LogEntry getLogEntry(){
		return logEntry;
	}

}