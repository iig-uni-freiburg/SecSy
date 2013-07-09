package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import de.invation.code.toval.validate.ParameterException;

public interface TraceCompletionListener {
	
	/**
	 * Notifies listeners about the completion of a trace.<br>
	 * This method should throw an exception of type ParameterException
	 * in case the case number is smaller than 1.
	 * @param caseNumber
	 * @throws ParameterException
	 */
	public void traceCompleted(int caseNumber) throws ParameterException;

}
