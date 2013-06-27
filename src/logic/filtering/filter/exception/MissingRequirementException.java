package logic.filtering.filter.exception;

import de.uni.freiburg.iig.telematik.jawl.log.EntryField;

public class MissingRequirementException extends FilterException {

	private static final long serialVersionUID = 4502840296579873955L;
	
	private final String msgFormat = "Missing filter requirement: %s";
	private EntryField requirement = null;

	public MissingRequirementException(EntryField requirement) {
		super(ErrorCode.MISSING_REQUIREMENT);
		this.requirement = requirement;
	}
	
	public EntryField getRequirement(){
		return requirement;
	}

	@Override
	public String getMessage(){
		return String.format(msgFormat, requirement);
	}

}
