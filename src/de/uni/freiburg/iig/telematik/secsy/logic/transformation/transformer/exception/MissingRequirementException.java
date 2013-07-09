package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.exception;

import de.uni.freiburg.iig.telematik.jawl.log.EntryField;

public class MissingRequirementException extends TransformerException {

	private static final long serialVersionUID = 4502840296579873955L;
	
	private final String msgFormat = "Missing transformer requirement: %s";
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
