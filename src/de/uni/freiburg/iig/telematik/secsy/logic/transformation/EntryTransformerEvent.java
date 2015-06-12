package de.uni.freiburg.iig.telematik.secsy.logic.transformation;


import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;

public class EntryTransformerEvent {
	
	public LogEntry logEntry;
	public int caseNumber;
	public Object sender;
	
	public EntryTransformerEvent(LogEntry logEntry, int caseNumber, Object sender){
		Validate.notNull(logEntry);
		Validate.notNull(sender);
		Validate.bigger(caseNumber, 0);
		this.logEntry = logEntry;
		this.caseNumber = caseNumber;
		this.sender = sender;
	}

}
