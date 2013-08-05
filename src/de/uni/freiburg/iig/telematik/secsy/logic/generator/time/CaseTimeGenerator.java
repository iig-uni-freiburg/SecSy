package de.uni.freiburg.iig.telematik.secsy.logic.generator.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.time.Weekday;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceCompletionListener;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceStartListener;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties.CaseStartPrecision;

//TODO: Startzeit von Event muss sich an der Endzeit der letzten Aktivität orientieren
//zu der ein kausaler Zusammenhang beteht.
//-> Modell und die Endzeit aller Case-Aktivitäten werden benötigt um das festzustellen.
// Gehe die Aktivitäten von hinten her durch.
// Sobald zu einer aktivität ein kausaler zusammenhang besteht (=> diese Aktivität im Netz ein Vorgänger ist)
//  -> Generiere Strtzeit anhand der Endzeit dieser Aktivität

//TODO: Anfragen für Aktivitäten an die Aktivitäten im modlel knüpfen.
//-> Validität von Aktivitäts-Strings
// Verwenden von Namen, nicht IDs

/**
 * Class for executions times of process activities.<br>
 * <br>
 * This class keeps track of the activity execution times within process traces (cases).<br>
 * For each case, the class stores the actual time which is incremented when execution times
 * for activities are requested by calling the method {@link #getTimeFor(String, int)}.
 * The interpretation of such method calls is, that the given activity is about to be executed.
 * The internal case time is then incremented by the duration of the given activity ({@link #getDurationFor(String)})
 * and possibly additional delays ({@link #getDelayFor(String)}) between the given activity and following
 * activities.<br>
 * <br>
 * The basic assumption is, that an activity can only start after the completion of the preceding activity.<br>
 * <br>
 * This class implements the interface {@link TraceCompletionListener} to reduce the memory overhead
 * of storing the actual time for every log trace. When trace completions are reported,
 * the corresponding entries are deleted from the storage map.<br>
 * 
 * @author Thomas Stocker
 *
 */
public class CaseTimeGenerator implements TraceStartListener, TraceCompletionListener{
	
	protected static final String toStringFormat = " Time generator name: %s\n\n" +
												   "           StartDate: %s\n" +
												   "       Cases per day: %s\n" +
												   "Case start precision: %s\n" +
												   "         Office days: %s\n" +
												   "        Office hours: %s\n\n" +
												   "Default activity duration: %s\n" +
												   "   Default activity delay: %s\n";
	
	protected final int FACTOR_SECONDS = 1000;
	protected final int FACTOR_MINUTES = FACTOR_SECONDS*60;
	protected final int FACTOR_HOURS = FACTOR_MINUTES*60;
	protected final int FACTOR_DAYS = FACTOR_HOURS*24;
	protected final int FACTOR_WEEKS = FACTOR_DAYS*7;
	protected final int FACTOR_MONTHS = FACTOR_DAYS*30;
	protected final int FACTOR_YEARS = FACTOR_DAYS*360;
	
	protected String name = TimeProperties.defaultName;
	
	/**
	 * Stores the actual time of log traces.<br>
	 * Actual case times are incremented when execution times for activities are requested.
	 */
	protected HashMap<Integer, Long> caseTimes = new HashMap<Integer, Long>();
	/**
	 * Set of days that are skipped during case start calculation.
	 * @see #skipDay(Weekday)
	 * @see #skipWeekend(boolean)
	 */
	protected Set<Integer> skipDays = new HashSet<Integer>(7);
	/**
	 * The default duration of process activities (1 hour).<br>
	 */
	protected TimeValue defaultActivityDuration = TimeProperties.defaultActivityDuration;
	/**
	 * The default delay between process activities (0 hour).<br>
	 */
	protected TimeValue defaultActivityDelay = TimeProperties.defaultActivityDelay;
	/**
	 * Begin of the daily working hours.<br>
	 * Standard value is 8 am.
	 * @see #setWorkingHours(int, int)
	 */
	protected int dayStart = TimeProperties.defaultDayStart;
	/**
	 * End of the daily working hours.<br>
	 * Standard value is 5 pm.
	 * @see #setWorkingHours(int, int)
	 */
	protected int dayEnd = TimeProperties.defaultDayEnd;
	/**
	 * Defines the general maximum number of cases to be generated per day.
	 */
	protected int maxCasesPerDay = TimeProperties.defaultCasesPerDay;
	/**
	 * Defines the precision of case starting times.<br> 
	 * @see #setCaseStartingTimePrecision(CaseStartPrecision)
	 */
	protected CaseStartPrecision caseStartingTimePrecision = TimeProperties.defaultCaseStarttimePrecision;
	
	
	/**
	 * Defines the number of cases to be generated for the actual day.
	 */
	protected int casesPerDay;
	/**
	 * Number of cases generated so far for the actual day.<br>
	 * Ranges between 0 and {@link #casesPerDay} and is reset
	 * when a new day is started.
	 * @see #startNewDay()
	 */
	protected int dayCasesSoFar = 0;
	/**
	 * Keeps the starting times for all passes of the actual day
	 * and is indexable by {@link #dayCasesSoFar}.
	 */
	protected ArrayList<Long> dayCaseStartingTimes = null;
	
	private long startTime = 0;
	
	//------- Helper objects -----------------------------------------------------------------
	
	DateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	protected Random rand = new Random();
	protected Calendar calendar = new GregorianCalendar(Calendar.getInstance().getTimeZone());
	/**
	 * Marks the global time state which is used for calculating case start times.
	 * Is initialized with the given start time in the constructor field.
	 */
	protected Calendar caseStartTime = new GregorianCalendar(Calendar.getInstance().getTimeZone());
	
	
	//------- Constructors -------------------------------------------------------------------

	
	/**
	 * Creates a new random adjustable time generator using the given start time 
	 * and the number of cases to be generated per day.<br>
	 * Note that only the day of the given start time is used and adjusted by the
	 * start of the daily working hours.
	 * 
	 * @param startTime Day for the start time of the first Case.
	 * @param passesPerDay Number of cases to be generated per day.
	 * @throws ParameterException 
	 * @see #dayStart
	 * @see #setWorkingHours(int, int)
	 */
	public CaseTimeGenerator(long startTime, int casesPerDay) throws ParameterException{
		Validate.notNegative(startTime);
		setMaxCasesPerDay(casesPerDay);
		try {
			skipWeekend(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.startTime = startTime;
		try {
			dayCaseStartingTimes = new ArrayList<Long>(casesPerDay);
		} catch(OutOfMemoryError memoryError){
			throw new ParameterException(ErrorCode.MEMORY, "Memory Error: Assign more memory to Java VM or adjust cases per day.");
		}
		caseStartTime.setTime(new Date(startTime));
	}
	
	//------- Getters and Setters ------------------------------------------------------------
	
	public long getStartTime(){
		return startTime;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the number of cases to be generated per day.<br>
	 * Note that when case times are still to be created for the current day,
	 * the effect of this change will not be recognizable before the next day starts.
	 * @throws ParameterException 
	 */
	public void setMaxCasesPerDay(int casesPerDay) throws ParameterException{
		Validate.notNegative(casesPerDay);
		maxCasesPerDay = casesPerDay;
	}
	
	public Integer getMaxCasesPerDay(){
		return maxCasesPerDay;
	}
	
	public int getCasesPerDay(){
		return casesPerDay;
	}

	/**
	 * Sets the default activity duration to the given value.
	 * @param duration Default duration time.
	 * @throws ParameterException 
	 */
	public void setDefaultDuration(TimeValue duration) throws ParameterException {
		setDefaultDuration(duration.getValue(), duration.getScale());
	}
	
	public TimeValue getDefaultActivityDuration(){
		return defaultActivityDuration;
	}
	
	public TimeValue getDefaultActivityDelay(){
		return defaultActivityDelay;
	}
	
	/**
	 * Sets the default activity duration to the given value.
	 * @param duration Time value for duration.
	 * @param scale Scale for the interpretation of the time value.
	 * @throws ParameterException 
	 */
	public void setDefaultDuration(Double duration, TimeScale scale) throws ParameterException {
		Validate.notNull(duration);
		Validate.notNull(scale);
		Validate.notNegative(duration, "Negative duration");
		defaultActivityDuration = new TimeValue(duration, scale);
	}
	
	/**
	 * Sets the precision for case starting times.<br>
	 * Depending on the chosen precision, time information on lower levels are set to zero.
	 * @param precision Precision for case starting times.
	 * @throws ParameterException 
	 * @see CaseStartPrecision
	 */
	public void setCaseStartingTimePrecision(CaseStartPrecision precision) throws ParameterException{
		Validate.notNull(precision);
		this.caseStartingTimePrecision = precision;
	}
	
	public CaseStartPrecision getCaseStartingTimePrecision(){
		return caseStartingTimePrecision;
	}
	
	/**
	 * Sets the daily working hours.<br>
	 * The default setting is 7 am to 5 pm.
	 * @param startTime Start working time (0-24)
	 * @param endTime End working time (0-24)
	 * @throws ParameterException 
	 */
	public void setWorkingHours(int startTime, int endTime) throws ParameterException{
		TimeProperties.validateWorkingHours(startTime, endTime);
		this.dayStart = startTime;
		this.dayEnd = endTime;
	}
	
	public int getDayStart(){
		return dayStart;
	}
	
	public int getDayEnd(){
		return dayEnd;
	}
	
	/**
	 * Allows to set weekend skipping.<br>
	 * If set to <code>true</code> saturdays and sundays are skipped during case start calculation.
	 * @param skipWeekend
	 * @throws InconsistencyException If operation results in the skipping of all weekdays.
	 */
	public void skipWeekend(boolean skipWeekend) throws InconsistencyException {
		if(skipWeekend){
			skipDay(Weekday.SATURDAY);
			skipDay(Weekday.SUNDAY);
		} else {
			skipDays.remove(Calendar.SATURDAY);
			skipDays.remove(Calendar.SUNDAY);
		}
	}
	
	/**
	 * Allows to skip a specific weekday during case start time calculation.<br>
	 * @param weekday Weekday to skip.
	 * @throws InconsistencyException If call results in the skipping of all weekdays.
	 */
	public void skipDay(Weekday weekday) throws InconsistencyException {
		skipDays.add(weekday.ordinal()+1);
		if(skipDays.size() == 7){
			throw new InconsistencyException("Cannot skip all weekdays.");
		}
	}
	
	public Set<Weekday> getSkipDays(){
		Set<Weekday> result = new HashSet<Weekday>();
		for(Integer skipDayInt: skipDays)
			result.add(getWeekDayFromInt(skipDayInt));
		return result;
	}
	
	public Set<Weekday> getWorkingDays(){
		Set<Weekday> workingDays = new HashSet<Weekday>(Arrays.asList(Weekday.values()));
		workingDays.removeAll(getSkipDays());
		return workingDays;
	}
	
	private Weekday getWeekDayFromInt(int intValue){
		switch(intValue){
			case Calendar.SUNDAY: return Weekday.SUNDAY;
			case Calendar.SATURDAY: return Weekday.SATURDAY;
			case Calendar.FRIDAY: return Weekday.FRIDAY;
			case Calendar.THURSDAY: return Weekday.THURSDAY;
			case Calendar.WEDNESDAY: return Weekday.WEDNESDAY;
			case Calendar.TUESDAY: return Weekday.TUESDAY;
			case Calendar.MONDAY: return Weekday.MONDAY;
		}
		return null;
	}
	
	//------- Functionality -------------------------------------------------------------------
	
	/**
	 * Checks if the starting time of the next case still lies in the actual day.
	 * @return <code>true</code> if the next case still lies in the actual day;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean nextCaseInActualDay(){
		return dayCasesSoFar + 1 < casesPerDay;
	}
	
	/**
	 * Returns the execution time of a process activity within a specific case.<br>
	 * When this method is called, the time generator increments the actual time of the
	 * corresponding trace by the activity duration. The interpretation of calls thus is,
	 * that the given activity is about to be executed.
	 * 
	 * @param activity Activity name.
	 * @param caseNumber Case number.
	 * @return The execution time of the given activity within the given case.
	 * @throws ParameterException If activity is <code>null</code>, 
	 * caseNmber is negative or there is no starting time for the case.
	 * @see ExecutionTime
	 */
	public ExecutionTime getTimeFor(String activity, int caseNumber) throws InconsistencyException, ParameterException{
		Validate.notNull(activity);
		Validate.notNegative(caseNumber);
		if(caseTimes.get(caseNumber) == null)
			throw new InconsistencyException("No starting time for case "+caseNumber);
		
		long startTime = caseTimes.get(caseNumber);
		long duration = getDurationFor(activity).getValueInMilliseconds();
		incrementCaseTime(caseNumber, duration);
		incrementCaseTime(caseNumber, getDelayFor(activity).getValue().longValue());
		ExecutionTime time = new ExecutionTime(startTime, duration);
		return time;
	}
	
	/**
	 * Increments the actual time of the case with the given number by the given time.<br>
	 * This method is public to allow external case time increments (e.g. by time-oriented transformers).
	 * @param caseNumber Case number.
	 * @param increase Increase in milliseconds.
	 * @throws ParameterException 
	 * @throws IllegalArgumentException If the increase is smaller than zero.
	 */
	public void incrementCaseTime(int caseNumber, long increase) throws ParameterException{
		Validate.notNegative(increase, "Cannot decrease actual case time.");
		Validate.notNegative(caseNumber);
		caseTimes.put(caseNumber, caseTimes.get(caseNumber)+increase);
	}
	
	//------- Helper Methods ---------------------------------------------------------------
	
	/**
	 * Starts a new day for case time calculation.<br>
	 * The global time for case starts is incremented by 24 hours
	 * and afterwards day starting times are calculated via {@link #prepareDay()}.
	 * @see #prepareDay()
	 */
	protected void startNewDay(){
		increaseOverallCaseTimeByOneDay();
		prepareDay();
	}
	
	/**
	 * Prepares case starting times for the actual day and stores them in {@link #dayCaseStartingTimes}.
	 * Uses {@link #checkWeekDay()} to check if the actual day is valid or should be skipped.
	 * @see #checkWeekDay()
	 */
	protected void prepareDay(){
		checkWeekDay();
		caseStartTime.set(Calendar.HOUR_OF_DAY, dayStart);
		caseStartTime.set(Calendar.MINUTE, 0);
		caseStartTime.set(Calendar.SECOND, 0);
		caseStartTime.set(Calendar.MILLISECOND, 0);
		dayCasesSoFar = 0;
		setCasesPerDay();
		dayCaseStartingTimes.clear();
		for(int i=1; i<=casesPerDay; i++){
			long startTime = caseStartTime.getTimeInMillis()+rand.nextInt((dayEnd-dayStart)*60*60*1000);
			calendar.setTimeInMillis(startTime);
			switch(caseStartingTimePrecision){
			case HOUR:
				calendar.set(Calendar.MINUTE, 0);
			case MINUTE:
				calendar.set(Calendar.SECOND, 0);
			case SECOND:
				calendar.set(Calendar.MILLISECOND, 0);
			case MILLISECOND:
			}
			startTime = calendar.getTimeInMillis();
			dayCaseStartingTimes.add(startTime);
		}
		Collections.sort(dayCaseStartingTimes);
	}
	
	/**
	 * Sets the number of cases that are generated per day.<br>
	 * This method can be overridden by subclasses to adjust/vary the number of cases per day.
	 * @return The number of cases to be generated per day.
	 */
	protected void setCasesPerDay(){
		casesPerDay = maxCasesPerDay;
	}
	
	/**
	 * Checks if the actual day is valid and skips days if needed.
	 * This method just changes the weekday, not the daytime.
	 * @see #increaseOverallCaseTimeByOneDay()
	 */
	protected void checkWeekDay(){
		//Check if weekends should be skipped
		if(!skipDays.isEmpty()){
			while(skipDays.contains(caseStartTime.get(Calendar.DAY_OF_WEEK))){
				increaseOverallCaseTimeByOneDay();
			}
		}
	}
	
	/**
	 * Increases the overall case time by one day.<br>
	 * The overall case time marks the global time state which is used for 
	 * calculating case start times.
	 * @see #caseStartTime
	 */
	protected void increaseOverallCaseTimeByOneDay(){
		caseStartTime.setTimeInMillis(caseStartTime.getTimeInMillis()+86400000);
	}
	
	/**
	 * Returns the duration of a process activity.<br>
	 * The standard implementation returns the default duration.
	 * Subclasses can override this method to individualize activity duration.
	 * 
	 * @param activity Name of the activity.
	 * @return The activity duration in milliseconds.
	 * @throws ParameterException 
	 * @see CaseTimeGenerator#defaultDuration
	 */
	public TimeValue getDurationFor(String activity) throws ParameterException{
		Validate.notNull(activity);
		return defaultActivityDuration;
	}
	
	/**
	 * Returns the delay of a process activity.<br>
	 * The delay is defined as the time that lies between the end time of an activity
	 * and the start time of a following activity.
	 * Delays cause the time generator to increment the corresponding internal case time
	 * by more than just the activity duration.<br>
	 * The standard implementation returns 0L.
	 * Subclasses can override this method to individualize activity delays.
	 * 
	 * @param activity Activity name.
	 * @return The delay added after the end time of the given activity.
	 * @throws ParameterException 
	 */
	public TimeValue getDelayFor(String activity) throws ParameterException{
		Validate.notNull(activity);
		return defaultActivityDelay;
	}
	
	/**
	 * Sets the delay of a process activity.<br>
	 * The delay is defined as the time that lies between the end time of an activity
	 * and the start time of a following activity.
	 * Delays cause the time generator to increment the corresponding internal case time
	 * by more than just the activity duration.<br>
	 * @param delay Default delay for all process activities.
	 * @throws ParameterException 
	 */
	public void setDefaultDelay(TimeValue delay) throws ParameterException{
		Validate.notNull(delay);
		setDefaultDelay(delay.getValue(), delay.getScale());
	}
	
	/**
	 * Sets the delay of a process activity.<br>
	 * The delay is defined as the time that lies between the end time of an activity
	 * and the start time of a following activity.
	 * Delays cause the time generator to increment the corresponding internal case time
	 * by more than just the activity duration.<br>
	 * @param defaultAcivityDelay Default delay for all process activities.
	 * @throws ParameterException 
	 */
	public void setDefaultDelay(Double delay, TimeScale scale) throws ParameterException{
		Validate.notNull(delay);
		Validate.notNull(scale);
		Validate.notNegative(delay, "Negative delay");
		this.defaultActivityDelay = new TimeValue(delay, scale);
	}
	
	//------- Interface Methods ------------------------------------------------------------
	
	/**
	 * Reports the start of a process trace.<br>
	 * Interface Implementation: TraceStartListener.<br>
	 * On trace start, a new case is started and time information added to the storage map.
	 * @throws ParameterException 
	 */
	@Override
	public void traceStarted(int caseNumber) throws ParameterException {
		Validate.bigger(caseNumber, 0);
		if(dayCaseStartingTimes.isEmpty()){
			prepareDay();
		}
		if(dayCasesSoFar == casesPerDay){
//			System.out.println();
			startNewDay();
		}
		caseTimes.put(caseNumber, dayCaseStartingTimes.get(dayCasesSoFar++));
//		System.out.println(format.format(new Date(caseTimes.get(caseNumber))));
	}
	
	/**
	 * Reports the completion of a process trace.<br>
	 * Interface Implementation: TraceCompletionListener.<br>
	 * On trace completion, corresponding entries in the storage map 
	 * for actual trace times are deleted.
	 */
	@Override
	public void traceCompleted(int caseNumber) throws ParameterException {
		Validate.bigger(caseNumber, 0);
		caseTimes.remove(caseNumber);
	}
	
	//------- Classes and Enumerations ------------------------------------------------------
	
	/**
	 * Class for the execution time of process activities.<br>
	 * Stores start time, end time and duration.
	 * 
	 * @author Thomas Stocker
	 */
	public class ExecutionTime {
		private final String toStringFormat = "%s | %s | %s";
		public long startTime = 0L;
		public long endTime = 0L;
		public long duration = 0L;
		
		public ExecutionTime(long start, long duration) throws ParameterException{
			Validate.notNegative(start);
			Validate.notNegative(duration);
			startTime = start;
			this.duration = duration;
			endTime = startTime+duration;
		}
		
		@Override
		public String toString(){
			return String.format(toStringFormat, startTime, duration, endTime);
		}
	}
	
	protected void fillProperties(TimeProperties properties) throws ParameterException{
		properties.setStartTime(getStartTime());
		properties.setName(getName());
		properties.setWorkingHours(getDayStart(), getDayEnd());
		properties.setCasesPerDay(getMaxCasesPerDay());
		properties.setSkipDays(getSkipDays());
		properties.setDefaultActivityDuration(defaultActivityDuration);
		properties.setDefaultActivityDelay(defaultActivityDelay);
		properties.setCaseStarttimePrecision(caseStartingTimePrecision);
	}
	
	public TimeProperties getProperties() throws ParameterException{
		TimeProperties properties = new TimeProperties();
		fillProperties(properties);
		return properties;
	}
	
	@Override
	public String toString(){
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String startTime = sdf.format(new Date(getStartTime()));
		String officeHours = getDayStart() + "-" + getDayEnd();
		return String.format(toStringFormat, getName(), 
											 startTime, 
											 getMaxCasesPerDayString(), 
											 getCaseStartingTimePrecision(), 
											 getWorkingDays(), 
											 officeHours, 
											 getDefaultActivityDurationString(), 
											 getDefaultActivityDelayString());
	}
	
	protected String getMaxCasesPerDayString(){
		return getMaxCasesPerDay().toString();
	}
	
	protected String getDefaultActivityDurationString(){
		return getDefaultActivityDuration().toString();
	}
	
	protected String getDefaultActivityDelayString(){
		return getDefaultActivityDelay().toString();
	}
	
	public static void main(String[] args) throws Exception {
		Calendar cal = new GregorianCalendar(Calendar.getInstance().getTimeZone());
		cal.set(2012, Calendar.JANUARY, 1);
		CaseTimeGenerator time = new CaseTimeGenerator(cal.getTimeInMillis(), 10);
		time.setCaseStartingTimePrecision(CaseStartPrecision.HOUR);
		for(int i=1; i<20; i++){
			time.traceStarted(i);
		}
		time.setMaxCasesPerDay(20);
		for(int i=21; i<70; i++){
			time.traceStarted(i);
		}
	}

}