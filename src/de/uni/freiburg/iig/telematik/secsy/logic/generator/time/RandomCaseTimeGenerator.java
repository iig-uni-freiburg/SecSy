package de.uni.freiburg.iig.telematik.secsy.logic.generator.time;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import de.invation.code.toval.misc.RandomUtils;
import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties.CaseStartPrecision;


/**
 * Class for executions times of process activities.<br>
 * <br>
 * Inherits from {@link AdjustableCaseTimeGenerator} and provides method support for adjusting 
 * activity durations in different scales (seconds, minutes, ...) and additionally
 * a deviation percentage.<br>
 * Adjusted activity durations are modified by a random deviation (up and down).
 * 
 * @author Thomas Stocker
 */
public class RandomCaseTimeGenerator extends AdjustableCaseTimeGenerator {
	
	protected static final String durationFormatDeviation = "%s: %s with %s%% deviation\n";
	protected static final String valueWithDeviationFormat = "%s with %s%% deviation";
	private NumberFormat nf = new DecimalFormat("#0.##");
	
	/**
	 * Map that stores the deviation percentage of activity durations.<br>
	 * Activity names are used as keys.
	 */
	protected HashMap<String, Double> durationDeviations = new HashMap<String, Double>();
	/**
	 * Map that stores the deviation percentage of activity delays.<br>
	 * Activity names are used as keys.
	 */
	protected HashMap<String, Double> delayDeviations = new HashMap<String, Double>();
	
	protected Double defaultDurationDeviation = TimeProperties.defaultActivityDurationDeviation;
	protected Double defaultDelayDeviation = TimeProperties.defaultActivityDelayDeviation;
	
	protected TimeValue defaultDelayMin = TimeProperties.defaultMinDelay;
	protected TimeValue defaultDelayMax = TimeProperties.defaultMaxDelay;
	protected TimeValue defaultDurationMin = TimeProperties.defaultMinDuration;
	protected TimeValue defaultDurationMax = TimeProperties.defaultMaxDuration;
	protected double dayCasesDeviation = TimeProperties.defaultDayCasesDeviation;
	
	
	
	/**
	 * Creates a new random adjustable time generator using the given start time, 
	 * the number of cases to be generated per day
	 * and the deviation percentage for the number of cases to be generated per day.<br>
	 * Note that only the day of the given start time is used and adjusted by the
	 * start of the daily working hours.
	 * 
	 * @param startTime Day for the start time of the first Case.
	 * @param passesPerDay Number of cases to be generated per day.
	 * @param dayCasesDeviation Deviation for the number of cases to be generated per day.
	 * @throws ParameterException 
	 * @see #dayStart
	 * @see #setWorkingHours(int, int)
	 */
	public RandomCaseTimeGenerator(long startTime, int passesPerDay, double dayCasesDeviation) throws ParameterException {
		super(startTime, passesPerDay);
		setDayCasesDeviation(dayCasesDeviation);
	}
	
	/**
	 * Creates a new random adjustable time generator using the given start time 
	 * and the number of cases to be generated per day.<br>
	 * Note that only the day of the given start time is used and adjusted by the
	 * start of the daily working hours.
	 * 
	 * @param startTime Day for the start time of the first Case.
	 * @param passesPerDay Number of cases to generate per day.
	 * @throws ParameterException 
	 * @see #dayStart
	 * @see #setWorkingHours(int, int)
	 */
	public RandomCaseTimeGenerator(long startTime, int passesPerDay) throws ParameterException {
		super(startTime, passesPerDay);
	}
	
	//------- Getters and Setters ------------------------------------------------------------------
	
	/**
	 * Sets the mean deviation for the number of generated cases per day.<br>
	 * Note that when case times are still to be created for the current day,
	 * the effect of this change will not be recognizable before the next day starts.
	 * @param dayCasesDeviation
	 * @throws ParameterException 
	 */
	public void setDayCasesDeviation(double dayCasesDeviation) throws ParameterException{
		Validate.probability(dayCasesDeviation, "Deviation must be within [0;1].");
		this.dayCasesDeviation = dayCasesDeviation;
	}
	
	public void setDefaultActivityDurationDeviation(Double deviation) throws ParameterException{
		Validate.probability(deviation);
		this.defaultDurationDeviation = deviation;
	}
	
	public void setDefaultActivityDelayDeviation(Double deviation) throws ParameterException{
		Validate.probability(deviation);
		this.defaultDelayDeviation = deviation;
	}
	
	/**
	 * Sets the bounds for the randomization of default activity delays.<br>
	 * @param minDelay Minimum delay time.
	 * @param maxDelay Maximum delay time.
	 * @throws ParameterException 
	 */
	public void setDefaultDelayBounds(TimeValue minDelay, TimeValue maxDelay) throws ParameterException {
		Validate.notNull(minDelay);
		Validate.notNull(maxDelay);
		Validate.isFalse(minDelay.isBiggerThan(maxDelay));

		this.defaultDelayMin = minDelay.clone();
		this.defaultDelayMax = maxDelay.clone();
	}
	
	/**
	 * Sets the bounds for the randomization of default activity durations.<br>
	 * @param minDuration Minimum duration time.
	 * @param maxDuration Maximum duration time.
	 * @throws ParameterException 
	 */
	public void setDefaultDurationBounds(TimeValue minDuration, TimeValue maxDuration) throws ParameterException {
		Validate.notNull(minDuration);
		Validate.notNull(maxDuration);
		Validate.isFalse(minDuration.isBiggerThan(maxDuration));

		this.defaultDurationMin = minDuration.clone();
		this.defaultDurationMax = maxDuration.clone();
	}
	
	//------- Functionality ------------------------------------------------------------------------
	
	/**
	 * Adjusts the duration of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param value Duration value.
	 * @param scale Scale for the interpretation of the time value.
	 * @param deviationPercentage Deviation percentage.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDuration(String activity, Integer value, TimeScale scale, double deviationPercentage) throws ParameterException{
		setDuration(activity, value.doubleValue(), scale, deviationPercentage);
	}
	
	
	/**
	 * Adjusts the duration of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param duration Duration
	 * @param deviationPercentage Deviation percentage.
	 * @param scale Scale for the interpretation of the time value.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDuration(String activity, TimeValue duration, double deviationPercentage) throws ParameterException{
		setDuration(activity, duration.getValue(), duration.getScale(), deviationPercentage);
	}
	
	/**
	 * Adjusts the duration of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param seconds Duration value
	 * @param scale Scale for the interpretation of the time value.
	 * @param deviationPercentage Deviation percentage.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDuration(String activity, Double value, TimeScale scale, double deviationPercentage) throws ParameterException{
		super.setDuration(activity, value, scale);
		Validate.probability(deviationPercentage, "Deviation must be within [0;1].");
		durationDeviations.put(activity, deviationPercentage);
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param seconds Delay value.
	 * @param deviationPercentage Deviation percentage.
	 * @param scale Scale for the interpretation of the time value.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDelay(String activity, Integer value, TimeScale scale, double deviationPercentage) throws ParameterException{
		setDelay(activity, value.doubleValue(), scale, deviationPercentage);
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param seconds Delay value.
	 * @param deviationPercentage Deviation percentage.
	 * @param scale Scale for the interpretation of the time value.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDelay(String activity, TimeValue delay, double deviationPercentage) throws ParameterException{
		setDelay(activity, delay.getValue(), delay.getScale(), deviationPercentage);
	}
	
	/**
	 * Adjusts the delay of the given activity to the given value
	 * together with the given deviation percentage.
	 * @param activity Activity name.
	 * @param seconds Delay value.
	 * @param deviationPercentage Deviation percentage.
	 * @param scale Scale for the interpretation of the time value.
	 * @throws ParameterException 
	 * @see TimeScale
	 */
	public void setDelay(String activity, Double value, TimeScale scale, double deviationPercentage) throws ParameterException{
		super.setDelay(activity, value, scale);
		Validate.probability(deviationPercentage, "Deviation must be within [0;1].");
		delayDeviations.put(activity, deviationPercentage);
	}
	
	//------- Helper Methods -----------------------------------------------------------------------

	/**
	 * Returns the duration of a process activity.<br>
	 * Overrides the superclass method to consider deviation percentages.
	 * In case no adjustment was made for the given activity,
	 * the method calls the corresponding superclass method.
	 * @throws ParameterException 
	 */
	@Override
	public TimeValue getDurationFor(String activity) throws ParameterException{
		Validate.notNull(activity);
		if(activityDurations.containsKey(activity)){
			//Activity duration was adjusted
			if(durationDeviations.containsKey(activity)){
				//Duration deviation was explicitly set
				//-> Add random deviation to adjusted duration
				TimeValue duration = activityDurations.get(activity);
				long deviation = ((int) Math.signum(rand.nextInt()))*(Math.abs(rand.nextLong()) % ((long) Math.ceil(duration.getValue()*durationDeviations.get(activity))));
				return new TimeValue(duration.getValue() + deviation, duration.getScale());
			} else {
				//Duration deviation was not explicitly set
				//-> If duration bounds are set, return random duration in these bounds
			    //   Else return adjusted duration
				if(defaultDurationBoundsSet())
					return new TimeValue(RandomUtils.randomLongBetween(defaultDurationMin.getValueInMilliseconds(), defaultDurationMax.getValueInMilliseconds()), TimeScale.MILLISECONDS);
				return activityDurations.get(activity);
			}
		} else {
			//Activity duration was not adjusted
			
			//If duration bounds are set, return random duration in these bounds
			if(defaultDurationBoundsSet())
				return new TimeValue(RandomUtils.randomLongBetween(defaultDurationMin.getValueInMilliseconds(), defaultDurationMax.getValueInMilliseconds()), TimeScale.MILLISECONDS);
			//Else check if deviation for default duration was explicitly set. 
			if(defaultDurationDeviation > 0){
				//Add random deviation to default duration
				long deviation = ((int) Math.signum(rand.nextInt()))*(Math.abs(rand.nextLong()) % ((long) Math.ceil(defaultActivityDuration.getValue()*defaultDurationDeviation)));
				return new TimeValue(defaultActivityDuration.getValue() + deviation, defaultActivityDuration.getScale());
			} else {
				//Return default delay
				return defaultActivityDuration;
			}
		}
	}
	
	/**
	 * Returns the delay of a process activity.<br>
	 * Overrides superclass method to provide a random delay
	 * between min and max values that can be explicitly adjusted.
	 * The default implementation adds no delay.
	 * @throws ParameterException 
	 */
	@Override
	public TimeValue getDelayFor(String activity) throws ParameterException{
		Validate.notNull(activity);
		if(activityDelays.containsKey(activity)){
			//Activity delay was adjusted
			if(delayDeviations.containsKey(activity)){
				//Delay deviation was explicitly set
				//-> Add random deviation to adjusted delay
				TimeValue delay = activityDelays.get(activity);
				long deviation = ((int) Math.signum(rand.nextInt()))*(Math.abs(rand.nextLong()) % ((long) Math.ceil(delay.getValue()*delayDeviations.get(activity))));
				return new TimeValue(delay.getValue() + deviation, delay.getScale());
			} else {
				//Delay deviation was not explicitly set
				//-> If delay bounds are set, return random delay in these bounds
			    //   Else return adjusted delay
				if(defaultDelayBoundsSet())
					return new TimeValue(RandomUtils.randomLongBetween(defaultDelayMin.getValueInMilliseconds(), defaultDelayMax.getValueInMilliseconds()), TimeScale.MILLISECONDS);
				return activityDelays.get(activity);
			}
		} else {
			//Activity delay was not adjusted
			
			//If delay bounds are set, return random delay in these bounds
			if(defaultDelayBoundsSet())
				return new TimeValue(RandomUtils.randomLongBetween(defaultDelayMin.getValueInMilliseconds(), defaultDelayMax.getValueInMilliseconds()), TimeScale.MILLISECONDS);
			//Else check if deviation for default delay was explicitly set. 
			if(defaultDelayDeviation > 0){
				//Add random deviation to default delay
				long deviation = ((int) Math.signum(rand.nextInt()))*(Math.abs(rand.nextLong()) % ((long) Math.ceil(defaultActivityDelay.getValue()*defaultDelayDeviation)));
				return new TimeValue(defaultActivityDelay.getValue() + deviation, defaultActivityDelay.getScale());
			} else {
				//Return default delay
				return defaultActivityDelay;
			}
		}
	}
	
	private boolean defaultDelayBoundsSet(){
		return defaultDelayMax.getValueInMilliseconds()-defaultDelayMin.getValueInMilliseconds() > 0;
	}
	
	private boolean defaultDurationBoundsSet(){
		return defaultDurationMax.getValueInMilliseconds()-defaultDurationMin.getValueInMilliseconds() > 0;
	}
	
	/**
	 * Randomly sets the number of passes for the next day.
	 */
	@Override
	protected void setCasesPerDay(){
		int sign;
		while((sign = (int) Math.signum(rand.nextInt()))==0){}
		int delta = rand.nextInt((int) (maxCasesPerDay*dayCasesDeviation)+1);
		casesPerDay = maxCasesPerDay + sign * delta;
	}
	
	
	@Override
	protected void fillProperties(TimeProperties properties) throws ParameterException {
		super.fillProperties(properties);
		
		properties.setDayCasesDeviation(dayCasesDeviation);
		if(defaultDurationBoundsSet())
			properties.setDefaultActivityDurationBounds(defaultDurationMin, defaultDurationMax);
		if(defaultDelayBoundsSet())
			properties.setDefaultActivityDelayBounds(defaultDelayMin, defaultDelayMax);
		if(defaultDurationDeviation > 0)
			properties.setDefaultActivityDuration(defaultActivityDuration, defaultDurationDeviation);
		if(defaultDelayDeviation > 0)
			properties.setDefaultActivityDelay(defaultActivityDelay, defaultDelayDeviation);
		for(String activity: durationDeviations.keySet()){
			properties.setActivityDuration(activity, activityDurations.get(activity), durationDeviations.get(activity));
		}
		for(String activity: delayDeviations.keySet()){
			properties.setActivityDelay(activity, activityDelays.get(activity), delayDeviations.get(activity));
		}
	}
	
	
	
	@Override
	protected String getMaxCasesPerDayString() {
		return String.format(valueWithDeviationFormat, getMaxCasesPerDay(), nf.format(dayCasesDeviation*100.0));
	}

	@Override
	protected String getDefaultActivityDurationString() {
		return String.format(valueWithDeviationFormat, getDefaultActivityDuration(), nf.format(defaultDurationDeviation*100.0));
	}

	@Override
	protected String getDefaultActivityDelayString() {
		return String.format(valueWithDeviationFormat, getDefaultActivityDelay(), nf.format(defaultDelayDeviation*100.0));
	}

	protected String getActivityDurationsString(){
		if(activityDurations.isEmpty()){
			return noIndividualActivityDurations;
		}
		StringBuilder builder = new StringBuilder();
		for(String activity: activityDurations.keySet()){
			if(durationDeviations.containsKey(activity)){
				builder.append(String.format(durationFormatDeviation, activity, activityDurations.get(activity), nf.format(durationDeviations.get(activity)*100.0)));
			} else {
				builder.append(String.format(durationFormat, activity, activityDurations.get(activity)));
			}
		}
		return builder.toString();
	}
	
	protected String getActivityDelaysString(){
		if(activityDurations.isEmpty()){
			return noIndividualActivityDelays;
		}
		StringBuilder builder = new StringBuilder();
		for(String activity: activityDelays.keySet()){
			if(delayDeviations.containsKey(activity)){
				builder.append(String.format(durationFormatDeviation, activity, activityDelays.get(activity), nf.format(delayDeviations.get(activity)*100.0)));
			} else {
				builder.append(String.format(durationFormat, activity, activityDelays.get(activity)));
			}
		}
		return builder.toString();
	}

	public static void main(String[] args) throws Exception {
		Calendar cal = new GregorianCalendar(Calendar.getInstance().getTimeZone());
		cal.set(2012, Calendar.JANUARY, 1);
		RandomCaseTimeGenerator time = new RandomCaseTimeGenerator(cal.getTimeInMillis(), 10, 0.0);
//		time.setDelayBounds(10, 30, TimeScale.MINUTES);
		time.setCaseStartingTimePrecision(CaseStartPrecision.HOUR);
		for(int i=1; i<20; i++){
			time.traceStarted(i);
		}
		time.setDayCasesDeviation(0.4);
		for(int i=21; i<41; i++){
			time.traceStarted(i);
		}
	}

}
