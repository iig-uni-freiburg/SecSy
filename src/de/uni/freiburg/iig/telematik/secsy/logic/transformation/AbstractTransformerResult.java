package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public abstract class AbstractTransformerResult {

	private boolean transformerApplied = false;
	private boolean transformerSuccess = true;
	private StringBuilder transformerMessages = new StringBuilder();
	private final String MESSAGE_FORMAT = "[%s] - %s";
	private int caseNumber;
	
	public AbstractTransformerResult(boolean transformerApplied){
		this.transformerApplied = transformerApplied;
	}
	
	public void setCaseNumber(int caseNumber) throws ParameterException{
		Validate.bigger(caseNumber, 0);
		this.caseNumber = caseNumber;
	}
	
	public void setTransformerSuccess(boolean success){
		transformerSuccess = success;
	}
	
	public boolean isSuccess(){
		return transformerSuccess;
	}
	
	public boolean wasTransformerApplied(){
		return transformerApplied;
	}
	
	public int getCaseNumber(){
		return caseNumber;
	}
	
	public boolean containsMessages(){
		return transformerMessages.length()!=0;
	}
	
	public void  addTransformerMessage(String message){
		if(message==null)
			throw new NullPointerException();
		if(message.equals(""))
			return;
		if(containsMessages()){
			transformerMessages.append('\n');
		}
		transformerMessages.append(String.format(MESSAGE_FORMAT, caseNumber, message));
	}
	
	public String getTransformerMessages(){
		return transformerMessages.toString();
	}

}
