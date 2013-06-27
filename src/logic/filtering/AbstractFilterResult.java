package logic.filtering;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public abstract class AbstractFilterResult {

	private boolean filterApplied = false;
	private boolean filterSuccess = true;
	private StringBuilder filterMessages = new StringBuilder();
	private final String MESSAGE_FORMAT = "[%s] - %s";
	private int caseNumber;
	
	public AbstractFilterResult(boolean filterApplied){
		this.filterApplied = filterApplied;
	}
	
	public void setCaseNumber(int caseNumber) throws ParameterException{
		Validate.bigger(caseNumber, 0);
		this.caseNumber = caseNumber;
	}
	
	public void setFilterSuccess(boolean success){
		filterSuccess = success;
	}
	
	public boolean isSuccess(){
		return filterSuccess;
	}
	
	public boolean wasFilterApplied(){
		return filterApplied;
	}
	
	public int getCaseNumber(){
		return caseNumber;
	}
	
	public boolean containsMessages(){
		return filterMessages.length()!=0;
	}
	
	public void  addFilterMessage(String message){
		if(message==null)
			throw new NullPointerException();
		if(message.equals(""))
			return;
		if(containsMessages()){
			filterMessages.append('\n');
		}
		filterMessages.append(String.format(MESSAGE_FORMAT, caseNumber, message));
	}
	
	public String getFilterMessages(){
		return filterMessages.toString();
	}

}
