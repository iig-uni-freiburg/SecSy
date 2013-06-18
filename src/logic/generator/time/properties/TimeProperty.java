package logic.generator.time.properties;

public enum TimeProperty {
	
	GENERATOR_NAME,
	
	START_TIME, 
	SKIP_DAYS, 
	DAY_START, DAY_END, 
	CASES_PER_DAY, DAY_CASES_DEVIATION,
	CASE_STARTTIME_PRECISION,
	
	DEFAULT_ACTIVITY_DURATION, 
	DEFAULT_ACTIVITY_DURATION_SCALE, 
	DEFAULT_ACTIVITY_DURATION_DEVIATION,
	
	DEFAULT_ACTIVITY_DELAY, 
	DEFAULT_ACTIVITY_DELAY_SCALE,
	DEFAULT_ACTIVITY_DELAY_DEVIATION,
	
	ACTIVITY_DURATION, 
	ACTIVITY_DURATION_SCALE,
	ACTIVITY_DURATION_DEVIATION,
	
	ACTIVITY_DELAY, 
	ACTIVITY_DELAY_SCALE,
	ACTIVITY_DELAY_DEVIATION,
	
	ACTIVITIES_WITH_INDIVIDUAL_DURATION, 
	ACTIVITIES_WITH_INDIVIDUAL_DURATION_DEVIATION,
	
	ACTIVITIES_WITH_INDIVIDUAL_DELAY,
	ACTIVITIES_WITH_INDIVIDUAL_DELAY_DEVIATION,
	
	MIN_DELAY, 
	MIN_DELAY_SCALE, 
	MAX_DELAY, 
	MAX_DELAY_SCALE,
	
	MIN_DURATION, 
	MIN_DURATION_SCALE, 
	MAX_DURATION, 
	MAX_DURATION_SCALE;

}