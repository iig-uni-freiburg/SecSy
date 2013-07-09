package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.exception;

import de.uni.freiburg.iig.telematik.secsy.logic.generator.SimulationException;

public abstract class TransformerException extends SimulationException {
	
	private static final long serialVersionUID = 1L;
	
	protected ErrorCode errorCode;
	
	public TransformerException(ErrorCode errorCode){
		this.errorCode = errorCode;
	}
	
	public enum ErrorCode {
		MISSING_REQUIREMENT;
	}

}
