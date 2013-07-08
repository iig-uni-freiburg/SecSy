package logic.transformation.transformer;

public enum TransformerType {
	
	DAY_DELAY,
	
	BOD,
	
	SOD,
	
	UNAUTHORIZED_EXECUTION,
	
	OBFUSCATION,
	
	SKIP_ACTIVITIES,
	
	INCOMPLETE_LOGGING;
	
	@Override
	public String toString(){
		switch(this){
		case DAY_DELAY: return "Day Delay";
		case BOD: return "Binding of Duties";
		case SOD: return "Separation of Duties";
		case UNAUTHORIZED_EXECUTION: return "Unauthorized Execution";
		case OBFUSCATION: return "Obfuscation";
		case SKIP_ACTIVITIES: return "Skip Activities";
		case INCOMPLETE_LOGGING: return "Incomlete Logging";
		}
		
		return null;
	}
	
	
	public static TransformerType valueOfString(String string){
		if(string.equals("Day Delay"))
			return DAY_DELAY;
		if(string.equals("Binding of Duties"))
			return BOD;
		if(string.equals("Separation of Duties"))
			return SOD;
		if(string.equals("Unauthorized Execution"))
			return UNAUTHORIZED_EXECUTION;
		if(string.equals("Obfuscation"))
			return OBFUSCATION;
		if(string.equals("Skip Activities"))
			return SKIP_ACTIVITIES;
		if(string.equals("Incomplete Logging"))
			return INCOMPLETE_LOGGING;
		return null;
	}
	

}
