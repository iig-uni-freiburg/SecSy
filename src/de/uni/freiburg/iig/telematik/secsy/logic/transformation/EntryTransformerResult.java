package de.uni.freiburg.iig.telematik.secsy.logic.transformation;


import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;

public class EntryTransformerResult extends AbstractTransformerResult{
	
	private LogEntry logEntry;
	
	public EntryTransformerResult(LogEntry logEntry, int caseNumber, boolean transformerApplied){
		super(transformerApplied);
		setLogEntry(logEntry);
		setCaseNumber(caseNumber);
	}
	
	public void setLogEntry(LogEntry logEntry){
		Validate.notNull(logEntry);
		this.logEntry = logEntry;
	}
	
	public LogEntry getLogEntry(){
		return logEntry;
	}

}
