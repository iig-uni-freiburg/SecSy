package de.uni.freiburg.iig.telematik.secsy.logic.generator.time;

import java.util.HashMap;

import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;


/**
 * Class for executions times of process activities.<br>
 * <br>
 * Inherits from {@link CaseTimeGenerator} and provides method support for adjusting 
 * activity durations in different scales (seconds, minutes, ...).<br>
 * If no adjustment for activity durations are made,
 * the default activity duration is used instead.
 * 
 * @author Thomas Stocker
 */
public class AdjustableCaseTimeGenerator extends CaseTimeGenerator{
	
	protected static final String toStringFormat = "%s\n%s\n\n%s\n";
	protected static final String durationFormat = "%s: %s\n";
	protected final String noIndividualActivityDurations = "No individual activity durations";
	protected final String noIndividualActivityDelays = "No individual activity delays";
	
	/**
	 * Activity durations in milliseconds.
	 */
	protected HashMap<String, TimeValue> activityDurations = new HashMap<String, TimeValue>();
	/**
	 * Activity delays in milliseconds.
	 */
	protected HashMap<String, TimeValue> activityDelays = new HashMap<String, TimeValue>();
	
	
	/**
	 * Creates a new random adjustable time generator using the given start time 
	 * and the number of cases to be generated per day.<br>
	 * Note that only the day of the given start time is used and adjusted by the
	 * start of the daily working hours.
	 * 
	 * @param startTime Day for the start time of the first Case.
	 * @param passesPerDay Number of cases to be generated per day.
	 * @ 
	 * @see #dayStart
	 * @see #setWorkingHours(int, int)
	 */
	public AdjustableCaseTimeGenerator(long startTime, int passesPerDay)  {
		super(startTime, passesPerDay);
	}
	
	//------- Functionality ------------------------------------------------------------------------
	
	/**
	 * Adjusts the duration of the given activity to the given value.<br>
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDuration(String activity, Integer value, TimeScale scale) {
		setDuration(activity, value.doubleValue(), scale);
	}
	
	/**
	 * Adjusts the duration of the given activity to the given value.<br>
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDuration(String activity, TimeValue duration) {
		Validate.notNull(duration);
		setDuration(activity, duration.getValue(), duration.getScale());
	}
	
	/**
	 * Adjusts the duration of the given activity to the given value.<br>
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDuration(String activity, Double value, TimeScale scale) {
		Validate.notNull(activity);
		Validate.notNegative(value);
		Validate.notNull(scale);
		activityDurations.put(activity, new TimeValue(value, scale));
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value.<br>
	 * The delay is the time between the completion of one activity and the
	 * start of a successive activity.
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDelay(String activity, Integer value, TimeScale scale) {
		setDelay(activity, value.doubleValue(), scale);
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value.<br>
	 * The delay is the time between the completion of one activity and the
	 * start of a successive activity.
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDelay(String activity, TimeValue delay) {
		Validate.notNull(delay);
		setDelay(activity, delay.getValue(), delay.getScale());
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value.<br>
	 * The delay is the time between the completion of one activity and the
	 * start of a successive activity.
	 * One month is considered to have 30 days.<br>
	 * One year is considered to have 360 days.
	 * 
	 * @param activity Activity name.
	 * @param value Time value.
	 * @param scale Scale for the interpretation of the time value.
	 * @ 
	 * @see TimeScale
	 */
	public void setDelay(String activity, Double value, TimeScale scale) {
		Validate.notNull(activity);
		Validate.notNegative(value);
		Validate.notNull(scale);
		activityDelays.put(activity, new TimeValue(value, scale));
	}
	
	//------- Helper Methods -----------------------------------------------------------------------

	/**
	 * Returns the duration of a process activity.<br>
	 * Overrides the superclass method to consider adjusted activity durations.
	 * In case no adjustment was made for the given activity,
	 * the method returns the default activity duration.
	 * @ 
	 */
	@Override
	public TimeValue getDurationFor(String activity) {
		Validate.notNull(activity);
		if(!activityDurations.containsKey(activity))
			return super.getDurationFor(activity);
		return activityDurations.get(activity);
	}
	
	/**
	 * Returns the delay of a process activity, i.e. the time after the activity
	 * before a successive activity can start.<br>
	 * Overrides the superclass method to consider adjusted activity delays.
	 * In case no adjustment was made for the given activity,
	 * the method returns the default activity delay.
	 * @ 
	 */
	@Override
	public TimeValue getDelayFor(String activity) {
		Validate.notNull(activity);
		if(!activityDelays.containsKey(activity))
			return super.getDelayFor(activity);
		return activityDelays.get(activity);
	}

	@Override
	protected void fillProperties(TimeProperties properties)  {
		super.fillProperties(properties);
		
		for(String activity: activityDurations.keySet()){
			properties.setActivityDuration(activity, activityDurations.get(activity));
		}
		for(String activity: activityDelays.keySet()){
			properties.setActivityDelay(activity, activityDelays.get(activity));
		}
	}
	
	
	@Override
	public String toString(){
		return String.format(toStringFormat, super.toString(), getActivityDurationsString(), getActivityDelaysString());
	}
	
	protected String getActivityDurationsString(){
		if(activityDurations.isEmpty()){
			return noIndividualActivityDurations;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Individual activity durations:\n\n");
		for(String activity: activityDurations.keySet()){
			builder.append(String.format(durationFormat, activity, activityDurations.get(activity)));
		}
		return builder.toString();
	}
	
	protected String getActivityDelaysString(){
		if(activityDelays.isEmpty()){
			return noIndividualActivityDelays;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("Individual activity delays:\n\n");
		for(String activity: activityDelays.keySet()){
			builder.append(String.format(durationFormat, activity, activityDelays.get(activity)));
		}
		return builder.toString();
	}

}
