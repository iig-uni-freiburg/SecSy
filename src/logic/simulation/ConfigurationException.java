package logic.simulation;

public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 1L;
	private ErrorCode errorCode = null;
	private final String msg_NoLogGenerator = "No log generator defined.";
	private final String msg_NoEntryGenerator = "No entry generator defined.";
	private final String msg_NoTimeGenerator = "No time generator defined.";
	private final String msg_NoContext = "No context defined.";
	private final String msg_ContextInconsistency = "Context inconsistency between entry generator and case data generator.";
	private final String msg_TransformerMisconfiguration = "Inconsistency in transformer configuration.";
	
	private final String reasonFormat = "%s\nReason: %s";
	
	private String reason = null;
	
	public ConfigurationException(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	public ConfigurationException(ErrorCode errorCode, String reason) {
		this(errorCode);
		this.reason = reason;
	}

	@Override
	public String getMessage(){
		String message = null;
		switch(errorCode){
			case NO_CONTEXT: message = msg_NoContext;
			case NO_LOGGENERATOR: message = msg_NoLogGenerator;
			case NO_ENTRYGENERATOR: message = msg_NoEntryGenerator;
			case NO_TIMEGENERATOR: message = msg_NoTimeGenerator;
			case CONTEXT_INCONSISTENCY: message = msg_ContextInconsistency;
			case TRANSFORMER_MISCONFIGURATION: message = msg_TransformerMisconfiguration;
		}
		if(message != null && reason != null){
			message = String.format(reasonFormat, message, reason);
		}
		return message;
	}

	public enum ErrorCode {
		NO_CONTEXT, NO_LOGGENERATOR, NO_ENTRYGENERATOR, NO_TIMEGENERATOR, CONTEXT_INCONSISTENCY, TRANSFORMER_MISCONFIGURATION;
	}

}
