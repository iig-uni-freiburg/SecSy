package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.invation.code.toval.validate.ParameterException;

public interface TraceStartListener {
	
	/**
	 * Notifies listeners about the start of a trace.<br>
	 * This method should throw an exception of type 
	 * ParameterException in case the case number is smaller 1.
	 * @param caseNumber
	 * @throws ParameterException
	 */
	public void traceStarted(int caseNumber) throws ParameterException;

}
