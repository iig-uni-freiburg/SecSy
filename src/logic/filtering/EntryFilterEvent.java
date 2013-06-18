package logic.filtering;

import validate.ParameterException;
import validate.Validate;
import log.LogEntry;

public class EntryFilterEvent {
	
	public LogEntry logEntry;
	public int caseNumber;
	public Object sender;
	
	public EntryFilterEvent(LogEntry logEntry, int caseNumber, Object sender) throws ParameterException {
		Validate.notNull(logEntry);
		Validate.notNull(sender);
		Validate.bigger(caseNumber, 0);
		this.logEntry = logEntry;
		this.caseNumber = caseNumber;
		this.sender = sender;
	}

}
