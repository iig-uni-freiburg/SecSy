package logic.transformation.transformer;

public enum TransformerType {
	
	DAY_DELAY_FILTER,
	
	BOD_FILTER,
	
	SOD_FILTER,
	
	UNAUTHORIZED_EXECUTION_FILTER,
	
	OBFUSCATION_FILTER,
	
	SKIP_ACTIVITIES_FILTER,
	
	INCOMPLETE_LOGGING_FILTER;
	
	@Override
	public String toString(){
		switch(this){
		case DAY_DELAY_FILTER: return "Day Delay";
		case BOD_FILTER: return "Binding of Duties";
		case SOD_FILTER: return "Separation of Duties";
		case UNAUTHORIZED_EXECUTION_FILTER: return "Unauthorized Execution";
		case OBFUSCATION_FILTER: return "Obfuscation";
		case SKIP_ACTIVITIES_FILTER: return "Skip Activities";
		case INCOMPLETE_LOGGING_FILTER: return "Incomlete Logging";
		}
		
		return null;
	}
	
	
	public static TransformerType valueOfString(String string){
		if(string.equals("Day Delay"))
			return DAY_DELAY_FILTER;
		if(string.equals("Binding of Duties"))
			return BOD_FILTER;
		if(string.equals("Separation of Duties"))
			return SOD_FILTER;
		if(string.equals("Unauthorized Execution"))
			return UNAUTHORIZED_EXECUTION_FILTER;
		if(string.equals("Obfuscation"))
			return OBFUSCATION_FILTER;
		if(string.equals("Skip Activities"))
			return SKIP_ACTIVITIES_FILTER;
		if(string.equals("Incomplete Logging"))
			return INCOMPLETE_LOGGING_FILTER;
		return null;
	}
	

}
