package logic.transformation.transformer.exception;

import logic.generator.SimulationException;

public abstract class FilterException extends SimulationException {
	
	private static final long serialVersionUID = 1L;
	
	protected ErrorCode errorCode;
	
	public FilterException(ErrorCode errorCode){
		this.errorCode = errorCode;
	}
	
	public enum ErrorCode {
		MISSING_REQUIREMENT;
	}

}
