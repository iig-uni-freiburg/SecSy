package de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.invation.code.toval.time.Weekday;
import de.invation.code.toval.validate.Validate;


public class TimeProperties extends AbstractProperties{
	
	
	
	private final String DURATION_FORMAT = TimeProperty.ACTIVITY_DURATION + "_%s";
	private final String DURATION_SCALE_FORMAT = TimeProperty.ACTIVITY_DURATION_SCALE + "_%s";
	private final String DURATION_DEVIATION_FORMAT = TimeProperty.ACTIVITY_DURATION_DEVIATION + "_%s";
	
	private final String DELAY_FORMAT = TimeProperty.ACTIVITY_DELAY + "_%s";
	private final String DELAY_SCALE_FORMAT = TimeProperty.ACTIVITY_DELAY_SCALE + "_%s";
	private final String DELAY_DEVIATION_FORMAT = TimeProperty.ACTIVITY_DELAY_DEVIATION + "_%s";
	
	public static final String defaultName = "NewCaseTimeGenerator";
	public static final Weekday[] defaultSkipDays = {Weekday.SATURDAY, Weekday.SUNDAY};
	public static final TimeValue defaultActivityDuration = new TimeValue();
	public static final Double defaultActivityDurationDeviation = 0.0;
	public static final TimeValue defaultActivityDelay = new TimeValue();
	public static final Double defaultActivityDelayDeviation = 0.0;

	public static final Integer defaultDayStart = 8;
	public static final Integer defaultDayEnd = 18;
	public static final Integer defaultCasesPerDay = 100;
	public static final CaseStartPrecision defaultCaseStarttimePrecision = CaseStartPrecision.HOUR;
	//---------------
	public static final TimeValue defaultMinDelay = new TimeValue();
	public static final TimeValue defaultMaxDelay = new TimeValue();
	public static final TimeValue defaultMinDuration = new TimeValue();
	public static final TimeValue defaultMaxDuration = new TimeValue();
	public static final Double defaultDayCasesDeviation = 0.7;
	
	public TimeProperties()  {
		super();
	}

	public TimeProperties(String fileName) throws IOException{
		super(fileName);
	}
	
	private TimeProperties(Properties properties){
		super(properties);
	}
	
//	private void initialize() {
//		setName(defaultName);
//		setDefaultActivityDuration(defaultActivityDuration.getValue(), defaultActivityDuration.getScale(), defaultActivityDurationDeviation);
//		setDefaultActivityDelay(defaultActivityDelay.getValue(), defaultActivityDelay.getScale(), defaultActivityDelayDeviation);
//		setSkipDays(defaultSkipDays);
//		setCasesPerDay(defaultCasesPerDay);
//		setWorkingHours(defaultDayStart, defaultDayEnd);
//		setCaseStarttimePrecision(defaultCaseStarttimePrecision);
//	}
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(TimeProperty timeProperty, Object value){
		props.setProperty(timeProperty.toString(), value.toString());
	}
	
	private String getProperty(TimeProperty timeProperty){
		return props.getProperty(timeProperty.toString());
	}
	
	private void removeProperty(TimeProperty timeProperty){
		props.remove(timeProperty.toString());
	}
	
	//-- Generator name
	
	public void setName(String name) {
		Validate.notNull(name);
		Validate.notEmpty(name);
		setProperty(TimeProperty.GENERATOR_NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(TimeProperty.GENERATOR_NAME);
		if(propertyValue == null)
			throw new PropertyException(TimeProperty.GENERATOR_NAME, propertyValue);
		return propertyValue;
	}

	//-- Start Time
	
	public void setStartTime(Long startTime) {
		Validate.notNull(startTime);
		Validate.notNegative(startTime);
		setProperty(TimeProperty.START_TIME, startTime);
	}
	
	public Long getStartTime() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.START_TIME);
		Long result = null;
		if(propertyValue == null)
			return result;
		try{
			result = Long.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.START_TIME, propertyValue);
		}
		Validate.notNegative(result);
		return result;
	}
	
	//-- Skip Days
	
	public void setSkipDays(Weekday... weekdays)  {
		setSkipDays(Arrays.asList(weekdays));
	}
	
	public void setSkipDays(Collection<Weekday> weekdays)  {
		validateWeekdays(weekdays);
		setProperty(TimeProperty.SKIP_DAYS, ArrayUtils.toString(weekdays.toArray()));
	}
	
	public List<Weekday> getSkipDays() throws PropertyException{
		List<Weekday> result = new ArrayList<Weekday>();
		String weekDays = getProperty(TimeProperty.SKIP_DAYS);
		if(weekDays == null)
			return result;
		StringTokenizer weekdayTokens = StringUtils.splitArrayString(weekDays, " ");
		while(weekdayTokens.hasMoreTokens()){
			String nextToken = weekdayTokens.nextToken();
			try{
				Weekday nextWeekday = Weekday.valueOf(nextToken.toUpperCase());
				result.add(nextWeekday);
			}catch(Exception e){
				throw new PropertyException("WEEKDAY", nextToken);
			}
		}
		return result;
	}
	
	//-- Default Activity Duration
	
	public void setDefaultActivityDuration(TimeValue duration) {
		Validate.notNull(duration);
		setDefaultActivityDuration(duration.getValue(), duration.getScale());
	}
	
	public void setDefaultActivityDuration(Integer duration, TimeScale scale) {
		setDefaultActivityDuration(duration.doubleValue(), scale); 
	}
	
	public void setDefaultActivityDuration(Double duration, TimeScale scale) {
		validateDuration(duration);
		Validate.notNull(scale);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION, duration);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_SCALE, scale);
	}
	
	public TimeValue getDefaultActivityDuration() throws PropertyException{
		String propertyValueDefaultActivityDuration = getProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION);
		String propertyValueDefaultActivityDurationScale = getProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_SCALE);
		
		Double defaultActivityDuration = null;
		try{
			defaultActivityDuration = Double.valueOf(propertyValueDefaultActivityDuration);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DURATION, propertyValueDefaultActivityDuration);
		}
		validateDuration(defaultActivityDuration);
		
		TimeScale defaultActivityDurationScale = null;
		try{
			defaultActivityDurationScale = TimeScale.valueOf(propertyValueDefaultActivityDurationScale.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DURATION_SCALE, propertyValueDefaultActivityDuration);
		}
		
		return new TimeValue(defaultActivityDuration, defaultActivityDurationScale);
	}
	
	public Double getDefaultActivityDurationDeviation() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_DEVIATION);
		if(propertyValue == null)
			return null;

		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DURATION_DEVIATION, propertyValue);
		}
		Validate.probability(result);
		return result;
	}
	
	public boolean existsDefaultActivityDurationDeviation(){
		try {
			if(getDefaultActivityDurationDeviation() == null)
				return false;
		} catch (Exception e) {}
		return true;
	}
	
	public boolean existsDefaultActivityDelayDeviation(){
		try {
			if(getDefaultActivityDelayDeviation() == null)
				return false;
		} catch (Exception e) {}
		return true;
	}
	
	public void setDefaultActivityDuration(TimeValue duration, Double deviationPercentage) {
		Validate.notNull(duration);
		setDefaultActivityDuration(duration.getValue(), duration.getScale(), deviationPercentage);
	}
	
	public void setDefaultActivityDuration(Integer duration, TimeScale scale, Double deviationPercentage) {
		setDefaultActivityDuration(duration.doubleValue(), scale, deviationPercentage);
	}
	
	public void setDefaultActivityDuration(Double delay, TimeScale scale, Double deviationPercentage) {
		setDefaultActivityDuration(delay, scale);
		Validate.notNull(deviationPercentage);
		Validate.probability(deviationPercentage);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_DEVIATION, deviationPercentage.toString());
	}
	
	//-- Default Delay
	
	public void setDefaultActivityDelay(TimeValue delay) {
		Validate.notNull(delay);
		setDefaultActivityDelay(delay.getValue(), delay.getScale());
	}
	
	public void setDefaultActivityDelay(Integer delay, TimeScale scale) {
		setDefaultActivityDelay(delay.doubleValue(), scale);
	}
	
	public void setDefaultActivityDelay(Double delay, TimeScale scale) {
		validateDuration(delay);
		Validate.notNull(scale);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY, delay);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_SCALE, scale);
	}
	
	public TimeValue getDefaultActivityDelay() throws PropertyException{
		String propertyValueDefaultActivityDelay = getProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY);
		String propertyValueDefaultActivityDelayScale = getProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_SCALE);
		
		Double defaultActivityDelay = null;
		try{
			defaultActivityDelay = Double.valueOf(propertyValueDefaultActivityDelay);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DELAY, propertyValueDefaultActivityDelay);
		}
		validateDelay(defaultActivityDelay);
		
		TimeScale defaultActivityDelayScale = null;
		try{
			defaultActivityDelayScale = TimeScale.valueOf(propertyValueDefaultActivityDelayScale.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DELAY_SCALE, propertyValueDefaultActivityDelayScale);
		}
		
		return new TimeValue(defaultActivityDelay, defaultActivityDelayScale);
	}
	
	public Double getDefaultActivityDelayDeviation() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_DEVIATION);
		if(propertyValue == null)
			return null;

		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DEFAULT_ACTIVITY_DELAY_DEVIATION, propertyValue);
		}
		Validate.probability(result);
		return result;
	}
	
	public void setDefaultActivityDelay(TimeValue delay, Double deviationPercentage) {
		Validate.notNull(delay);
		setDefaultActivityDelay(delay.getValue(), delay.getScale(), deviationPercentage);
	}
	
	public void setDefaultActivityDelay(Integer delay, TimeScale scale, Double deviationPercentage) {
		setDefaultActivityDelay(delay.doubleValue(), scale, deviationPercentage);
	}
	
	public void setDefaultActivityDelay(Double delay, TimeScale scale, Double deviationPercentage) {
		setDefaultActivityDelay(delay, scale);
		Validate.notNull(deviationPercentage);
		Validate.probability(deviationPercentage);
		setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_DEVIATION, deviationPercentage.toString());
	}
	
	
	//-- Working Hours
	
	public void setWorkingHours(Integer startTime, Integer endTime) {
		validateWorkingHours(startTime, endTime);
		setProperty(TimeProperty.DAY_START, startTime);
		setProperty(TimeProperty.DAY_END, endTime);
	}
	
	public Integer getOfficeHoursStart() throws PropertyException{
		return getWorkingTime(WorkingHourProperty.DAY_START);
	}
	
	public Integer getOfficeHoursEnd() throws PropertyException{
		return getWorkingTime(WorkingHourProperty.DAY_END);
	}
	
	private Integer getWorkingTime(WorkingHourProperty dayTime) throws PropertyException{
		String dayStart = getProperty(TimeProperty.DAY_START);
		String dayEnd = getProperty(TimeProperty.DAY_END);
		Integer startTime = null;
		Integer endTime = null;
		try{
			startTime = Integer.valueOf(dayStart);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DAY_START, dayStart);
		}
		try{
			endTime = Integer.valueOf(dayEnd);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DAY_END, dayEnd);
		}
		validateWorkingHours(startTime, endTime);
		if(dayTime == WorkingHourProperty.DAY_START){
			return startTime;
		} else {
			return endTime;
		}
	}
	
	//-- Cases Per Day
	
	public void setCasesPerDay(Integer cases) {
		validateCasesPerDay(cases);
		setProperty(TimeProperty.CASES_PER_DAY, cases);
	}
	
	public Integer getCasesPerDay() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.CASES_PER_DAY);
		Integer result = null;
		try{
			result = Integer.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.CASES_PER_DAY, propertyValue);
		}
		validateCasesPerDay(result);
		return result;
	}
	
	//-- Time Precision
	
	public void setCaseStarttimePrecision(CaseStartPrecision precision)  {
		validateTimePrecision(precision);
		setProperty(TimeProperty.CASE_STARTTIME_PRECISION, precision);
	}
	
	public CaseStartPrecision getCaseStarttimePrecision() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.CASE_STARTTIME_PRECISION);
		try{
			return CaseStartPrecision.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.CASE_STARTTIME_PRECISION, propertyValue);
		}
	}
	
	//--------Property Setting Adjustable Time Generator
	
	public void setActivityDuration(String activity, TimeValue duration) {
		Validate.notNull(duration);
		setActivityDuration(activity, duration.getValue(), duration.getScale());
	}
	
	public void setActivityDuration(String activity, Integer duration, TimeScale scale) {
		setActivityDuration(activity, duration.doubleValue(), scale);
	}
	
	public void setActivityDuration(String activity, Double duration, TimeScale scale) {
		Validate.notNull(activity);
		validateDuration(duration);
		Validate.notNull(scale);
		props.setProperty(String.format(DURATION_FORMAT, activity), duration.toString());
		props.setProperty(String.format(DURATION_SCALE_FORMAT, activity), scale.toString());
		addActivityWithIndividualDuration(activity);
	}
	
	private void addActivityWithIndividualDuration(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDuration();
		currentActivities.add(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public boolean hasIndivivualDuration(String activity) {
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DURATION_FORMAT, activity));
		return propertyValue != null;
	}

	public TimeValue getIndividualActivityDuration(String activity) throws PropertyException{
		Validate.notNull(activity);
		String propertyValueActivityDuration = props.getProperty(String.format(DURATION_FORMAT, activity));
		String propertyValueActivityDurationScale = props.getProperty(String.format(DURATION_SCALE_FORMAT, activity));
		
		if(propertyValueActivityDuration == null){
			// There is no individual duration for the given activity.
			return null;
		}
		Double activityDuration = null;
		try{
			activityDuration = Double.valueOf(propertyValueActivityDuration);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DURATION, propertyValueActivityDuration);
		}
		validateDuration(activityDuration);
		
		TimeScale activityDurationScale = null;
		try{
			activityDurationScale = TimeScale.valueOf(propertyValueActivityDurationScale.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DURATION_SCALE, propertyValueActivityDuration);
		}
		
		return new TimeValue(activityDuration, activityDurationScale);
	}
	
	public Set<String> getActivitiesWithIndividualDuration(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean existActivitiesWithIndividualDuration(){
		return !getActivitiesWithIndividualDuration().isEmpty();
	}
	
	private void removeActivityWithIndividualDuration(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDuration();
		currentActivities.remove(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public void setActivityDelay(String activity, TimeValue delay) {
		Validate.notNull(delay);
		setActivityDelay(activity, delay.getValue(), delay.getScale());
	}
	
	public void setActivityDelay(String activity, Integer delay, TimeScale scale) {
		setActivityDelay(activity, delay.doubleValue(), scale);
	}

	public void setActivityDelay(String activity, Double delay, TimeScale scale) {
		Validate.notNull(activity);
		validateDuration(delay);
		Validate.notNull(scale);
		props.setProperty(String.format(DELAY_FORMAT, activity), delay.toString());
		props.setProperty(String.format(DELAY_SCALE_FORMAT, activity), scale.toString());
		addActivityWithIndividualDelay(activity);
	}
	
	private void addActivityWithIndividualDelay(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDelay();
		currentActivities.add(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public boolean hasIndividualDelay(String activity) {
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DELAY_FORMAT, activity));
		return propertyValue != null;
	}
	
	public TimeValue getIndividualActivityDelay(String activity) throws PropertyException{
		Validate.notNull(activity);
		String propertyValueActivityDelay = props.getProperty(String.format(DELAY_FORMAT, activity));
		String propertyValueActivityDelayScale = props.getProperty(String.format(DELAY_SCALE_FORMAT, activity));

		if(propertyValueActivityDelay == null){
			// There is no individual delay for the given activity.
			return null;
		}
		Double activityDelay = null;
		try{
			activityDelay = Double.valueOf(propertyValueActivityDelay);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DELAY, propertyValueActivityDelay);
		}
		validateDuration(activityDelay);
		
		TimeScale activityDelayScale = null;
		try{
			activityDelayScale = TimeScale.valueOf(propertyValueActivityDelayScale.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DELAY_SCALE, propertyValueActivityDelayScale);
		}
		
		return new TimeValue(activityDelay, activityDelayScale);
	}
	
	public Set<String> getActivitiesWithIndividualDelay(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean existActivitiesWithIndividualDelay(){
		return !getActivitiesWithIndividualDelay().isEmpty();
	}
	
	private void removeActivityWithIndividualDelay(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		
		Set<String> currentActivities = getActivitiesWithIndividualDelay();
		currentActivities.remove(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	
	//--------Property Setting Randomized Time Generator
	
	public void setActivityDuration(String activity, TimeValue duration, Double deviationPercentage) {
		Validate.notNull(duration);
		setActivityDuration(activity, duration.getValue(), duration.getScale(), deviationPercentage);
	}
	
	public void setActivityDuration(String activity, Integer duration, TimeScale scale, Double deviationPercentage) {
		setActivityDuration(activity, duration.doubleValue(), scale, deviationPercentage);
	}
	
	public void setActivityDuration(String activity, Double duration, TimeScale scale, Double deviationPercentage) {
		setActivityDuration(activity, duration, scale);
		Validate.notNull(deviationPercentage);
		Validate.probability(deviationPercentage);
		props.setProperty(String.format(DURATION_DEVIATION_FORMAT, activity), deviationPercentage.toString());
		addActivityWithIndividualDurationDeviation(activity);
	}
	
	private void addActivityWithIndividualDurationDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDurationDeviation();
		currentActivities.add(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION_DEVIATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public Set<String> getActivitiesWithIndividualDurationDeviation(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION_DEVIATION);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean hasIndividualDurationDeviation(String activity) {
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DURATION_DEVIATION_FORMAT, activity));
		return propertyValue != null;
	}
	
	public Double getIndividualActivityDurationDeviation(String activity) throws PropertyException{
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DURATION_DEVIATION_FORMAT, activity));
		if(propertyValue == null){
			// There is no individual duration deviation for the given activity.
			return null;
		}
		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DURATION_DEVIATION, propertyValue);
		}
		Validate.probability(result);
		return result;
	}
	
	public boolean existActivitiesWithIndividualDurationDeviation(){
		return !getActivitiesWithIndividualDurationDeviation().isEmpty();
	}
	
	private void removeActivityWithIndividualDurationDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDurationDeviation();
		currentActivities.remove(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DURATION_DEVIATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public void setActivityDelay(String activity, TimeValue delay, Double deviationPercentage) {
		Validate.notNull(delay);
		setActivityDelay(activity, delay.getValue(), delay.getScale(), deviationPercentage);
	}
	
	public void setActivityDelay(String activity, Integer delay, TimeScale scale, Double deviationPercentage) {
		setActivityDelay(activity, delay.doubleValue(), scale, deviationPercentage);
	}
	
	public void setActivityDelay(String activity, Double delay, TimeScale scale, Double deviationPercentage) {
		setActivityDelay(activity, delay, scale);
		Validate.notNull(deviationPercentage);
		Validate.probability(deviationPercentage);
		props.setProperty(String.format(DELAY_DEVIATION_FORMAT, activity), deviationPercentage.toString());
		addActivityWithIndividualDelayDeviation(activity);
	}
	
	private void addActivityWithIndividualDelayDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDelayDeviation();
		currentActivities.add(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY_DEVIATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public Set<String> getActivitiesWithIndividualDelayDeviation(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY_DEVIATION);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean hasIndividualDelayDeviation(String activity) {
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DELAY_DEVIATION_FORMAT, activity));
		return propertyValue != null;
	}
	
	public Double getIndividualActivityDelayDeviation(String activity) throws PropertyException{
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(DELAY_DEVIATION_FORMAT, activity));
		if(propertyValue == null){
			// There is no individual delay deviation for the given activity.
			return null;
		}
		Double result = null;
		try{
			result = Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.ACTIVITY_DELAY_DEVIATION, propertyValue);
		}
		Validate.probability(result);
		return result;
	}
	
	public boolean existActivitiesWithIndividualDelayDeviation(){
		return !getActivitiesWithIndividualDelayDeviation().isEmpty();
	}
	
	private void removeActivityWithIndividualDelayDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithIndividualDelayDeviation();
		currentActivities.remove(activity);
		setProperty(TimeProperty.ACTIVITIES_WITH_INDIVIDUAL_DELAY_DEVIATION, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	public void setDefaultActivityDelayBounds(TimeValue minDelay, TimeValue maxDelay)  {
		Validate.notNull(minDelay);
		Validate.notNull(minDelay);
		setDefaultActivityDelayBounds(minDelay.getValue(), minDelay.getScale(), maxDelay.getValue(), maxDelay.getScale());
	}
	
	public void setDefaultActivityDelayBounds(Integer minDelay,  TimeScale minDelayScale, Integer maxDelay, TimeScale maxDelayScale)  {
		setDefaultActivityDelayBounds(minDelay.doubleValue(), minDelayScale, maxDelay.doubleValue(), maxDelayScale); 
	}
	
	public void setDefaultActivityDelayBounds(Double minDelay,  TimeScale minDelayScale, Double maxDelay, TimeScale maxDelayScale)  {
		validateDelayBounds(minDelay, minDelayScale, maxDelay, maxDelayScale);
		setProperty(TimeProperty.MIN_DELAY, minDelay);
		setProperty(TimeProperty.MIN_DELAY_SCALE, minDelayScale);
		setProperty(TimeProperty.MAX_DELAY, maxDelay);
		setProperty(TimeProperty.MAX_DELAY_SCALE, maxDelayScale);
	}
	
	public TimeValue getDefaultActivityMinDelay() throws PropertyException{
		return getDelay(DelayBound.MIN_DELAY);
	}
	
	public TimeValue getDefaultActivityMaxDelay() throws PropertyException{
		return getDelay(DelayBound.MAX_DELAY);
	}
	
	private TimeValue getDelay(DelayBound bound) throws PropertyException{
		String minDelayString = getProperty(TimeProperty.MIN_DELAY);
		String maxDelayString = getProperty(TimeProperty.MAX_DELAY);
		if(minDelayString == null && maxDelayString != null)
			throw new PropertyException(TimeProperty.MIN_DELAY, minDelayString);
		if(maxDelayString == null && minDelayString != null)
			throw new PropertyException(TimeProperty.MAX_DELAY, maxDelayString);
		if(minDelayString == null && maxDelayString == null)
			return null;
		
		String minDelayScaleString = getProperty(TimeProperty.MIN_DELAY_SCALE);
		String maxDelayScaleString = getProperty(TimeProperty.MAX_DELAY_SCALE);
		if(minDelayScaleString == null)
			throw new PropertyException(TimeProperty.MIN_DELAY_SCALE, minDelayScaleString);
		if(maxDelayScaleString == null)
			throw new PropertyException(TimeProperty.MAX_DELAY_SCALE, maxDelayScaleString);
		
		Double minDelay = null;
		try{
			minDelay = Double.valueOf(minDelayString);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MIN_DELAY, minDelayString);
		}
		TimeScale minDelayScale = null;
		try{
			minDelayScale = TimeScale.valueOf(minDelayScaleString.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MIN_DELAY_SCALE, minDelayScaleString);
		}
		
		Double maxDelay = null;
		try{
			maxDelay = Double.valueOf(maxDelayString);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MAX_DELAY, maxDelayString);
		}
		TimeScale maxDelayScale = null;
		try{
			maxDelayScale = TimeScale.valueOf(maxDelayScaleString.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MAX_DELAY_SCALE, maxDelayScaleString);
		}
		
		validateDelayBounds(minDelay, minDelayScale, maxDelay, maxDelayScale);
		if(bound == DelayBound.MIN_DELAY){
			return new TimeValue(minDelay, minDelayScale);
		} else {
			return new TimeValue(maxDelay, maxDelayScale);
		}
	}
	
	public void setDefaultActivityDurationBounds(TimeValue minDuration, TimeValue maxDuration)  {
		Validate.notNull(minDuration);
		Validate.notNull(minDuration);
		setDefaultActivityDurationBounds(minDuration.getValue(), minDuration.getScale(), maxDuration.getValue(), maxDuration.getScale());
	}
	
	public void setDefaultActivityDurationBounds(Integer minDuration,  TimeScale minDurationScale, Integer maxDuration, TimeScale maxDurationScale)  {
		setDefaultActivityDurationBounds(minDuration.doubleValue(), minDurationScale, maxDuration.doubleValue(), maxDurationScale); 
	}
	
	public void setDefaultActivityDurationBounds(Double minDuration, TimeScale minDurationScale, Double maxDuration, TimeScale maxDurationScale)  {
		validateDelayBounds(minDuration, minDurationScale, maxDuration, maxDurationScale);
		setProperty(TimeProperty.MIN_DURATION, minDuration);
		setProperty(TimeProperty.MIN_DURATION_SCALE, minDurationScale);
		setProperty(TimeProperty.MAX_DURATION, maxDuration);
		setProperty(TimeProperty.MAX_DURATION_SCALE, maxDurationScale);
	}
	
	public TimeValue getDefaultActivityMinDuration() throws PropertyException{
		return getDuration(DurationBound.MIN_DURATION);
	}
	
	public TimeValue getDefaultActivityMaxDuration() throws PropertyException{
		return getDuration(DurationBound.MAX_DURATION);
	}
	
	private TimeValue getDuration(DurationBound bound) throws PropertyException{
		String minDurationString = getProperty(TimeProperty.MIN_DURATION);
		String maxDurationString = getProperty(TimeProperty.MAX_DURATION);
		if(minDurationString == null && maxDurationString != null)
			throw new PropertyException(TimeProperty.MIN_DURATION, minDurationString);
		if(maxDurationString == null && minDurationString != null)
			throw new PropertyException(TimeProperty.MAX_DURATION, maxDurationString);
		if(minDurationString == null && maxDurationString == null)
			return null;
		
		String minDurationScaleString = getProperty(TimeProperty.MIN_DURATION_SCALE);
		String maxDurationScaleString = getProperty(TimeProperty.MAX_DURATION_SCALE);
		if(minDurationScaleString == null)
			throw new PropertyException(TimeProperty.MIN_DURATION_SCALE, minDurationScaleString);
		if(maxDurationScaleString == null)
			throw new PropertyException(TimeProperty.MAX_DURATION_SCALE, maxDurationScaleString);
		
		Double minDuration = null;
		try{
			minDuration = Double.valueOf(minDurationString);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MIN_DURATION, minDurationString);
		}
		TimeScale minDurationScale = null;
		try{
			minDurationScale = TimeScale.valueOf(minDurationScaleString.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MIN_DURATION_SCALE, minDurationScaleString);
		}
		
		Double maxDuration = null;
		try{
			maxDuration = Double.valueOf(maxDurationString);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MAX_DURATION, maxDurationString);
		}
		TimeScale maxDurationScale = null;
		try{
			maxDurationScale = TimeScale.valueOf(maxDurationScaleString.toUpperCase());
		}catch(Exception e){
			throw new PropertyException(TimeProperty.MAX_DURATION_SCALE, maxDurationScaleString);
		}
		
		validateDelayBounds(minDuration, minDurationScale, maxDuration, maxDurationScale);
		if(bound == DurationBound.MIN_DURATION){
			return new TimeValue(minDuration, minDurationScale);
		} else {
			return new TimeValue(maxDuration, maxDurationScale);
		}
	}
	
	public void removeIndividualActivityDuration(String activity) {
		Validate.notNull(activity);	
		Validate.notEmpty(activity);
		props.remove(String.format(DURATION_FORMAT, activity));
		removeActivityWithIndividualDuration(activity);
		removeIndividualActivityDurationDeviation(activity);
	}
	
	public void removeIndividualActivityDurationDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		props.remove(String.format(DURATION_DEVIATION_FORMAT, activity));
		removeActivityWithIndividualDurationDeviation(activity);
	}
	
	public void removeIndividualActivityDelay(String activity) {
		Validate.notNull(activity);	
		Validate.notEmpty(activity);
		props.remove(String.format(DELAY_FORMAT, activity));
		removeActivityWithIndividualDelay(activity);
		removeIndividualActivityDelayDeviation(activity);
	}
	
	public void removeIndividualActivityDelayDeviation(String activity) {
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		props.remove(String.format(DELAY_DEVIATION_FORMAT, activity));
		removeActivityWithIndividualDelayDeviation(activity);
	}
	
	public void removeDefaultActivityDelayBounds(){
		removeProperty(TimeProperty.MIN_DELAY);
		removeProperty(TimeProperty.MIN_DELAY_SCALE);
		removeProperty(TimeProperty.MAX_DELAY);
		removeProperty(TimeProperty.MAX_DELAY_SCALE);
	}
	
	public void removeDefaultActivityDurationBounds(){
		removeProperty(TimeProperty.MIN_DURATION);
		removeProperty(TimeProperty.MIN_DURATION_SCALE);
		removeProperty(TimeProperty.MAX_DURATION);
		removeProperty(TimeProperty.MAX_DURATION_SCALE);
	}
	
	public boolean existDefaultActivityDelayBounds(){
		try {
			if(getDefaultActivityMinDelay() == null)
				return false;
		} catch (Exception e) {}
		return true;
	}
	
	public boolean existDefaultActivityDurationBounds(){
		try {
			if(getDefaultActivityMinDuration() == null)
				return false;
		} catch (Exception e) {}
		return true;
	}
	
	public void setDayCasesDeviation(Double dayCasesDeviation) {
		Validate.probability(dayCasesDeviation);
		setProperty(TimeProperty.DAY_CASES_DEVIATION, dayCasesDeviation);
	}
	
	public Double getCasesPerDayDeviation() throws PropertyException{
		String propertyValue = getProperty(TimeProperty.DAY_CASES_DEVIATION);
		if(propertyValue == null)
			return null;
		try{
			return Double.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(TimeProperty.DAY_CASES_DEVIATION, propertyValue);
		}
	}
	
	public boolean existsDayCaseDeviation(){
		try {
			if(getCasesPerDayDeviation() == null)
				return false;
		} catch (Exception e) {}
		return true;
	}
	
	//------- Validation -------------------------------------------------------------------
	
	public static void validateWeekdays(Collection<Weekday> weekdays) {
		Validate.noNullElements(weekdays);
	}
	
	public static void validateDuration(Double duration) {
		Validate.notNull(duration);
		Validate.notNegative(duration);
	}
	
	public static void validateDelay(Double delay) {
		Validate.notNull(delay);
		Validate.notNegative(delay);
	}
	
	public static void validateWorkingHours(Integer startTime, Integer endTime) {
		Validate.notNull(startTime);
		Validate.notNull(endTime);
		Validate.inclusiveBetween(0, 23, startTime);
		Validate.inclusiveBetween(0, 23, endTime);
		Validate.isFalse(startTime == endTime);
		Validate.minMax(startTime, endTime);
	}
	
	public static void validateDelayBounds(Double minDelay,  TimeScale minDelayScale, Double maxDelay, TimeScale maxDelayScale) {
		Validate.notNull(minDelay);
		Validate.notNull(maxDelay);
		Validate.notNegative(minDelay);
		Validate.notNegative(maxDelay);
		Validate.notNull(minDelayScale);
		Validate.notNull(maxDelayScale);
		
		TimeValue minDelayV = new TimeValue(minDelay, minDelayScale);
		TimeValue maxDelayV = new TimeValue(maxDelay, maxDelayScale);
		
		Validate.isFalse(minDelayV.equals(maxDelayV));
		Validate.isFalse(minDelayV.isBiggerThan(maxDelayV));
	}
	
	public static void validateCasesPerDay(Integer casesPerDay) {
		Validate.notNull(casesPerDay);
		Validate.notNegative(casesPerDay);
	}
	
	public static void validateTimePrecision(CaseStartPrecision precision) {
		Validate.notNull(precision);
	}
	
	//------- Default Properties -----------------------------------------------------------
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = new Properties();
		defaultProperties.setProperty(TimeProperty.GENERATOR_NAME.toString(), defaultName);
		defaultProperties.setProperty(TimeProperty.SKIP_DAYS.toString(), ArrayUtils.toString(defaultSkipDays));
		
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION.toString(), defaultActivityDuration.getValue().toString());
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_SCALE.toString(), defaultActivityDuration.getScale().toString());
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DURATION_DEVIATION.toString(), defaultActivityDurationDeviation.toString());
		
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY.toString(), defaultActivityDelay.getValue().toString());
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_SCALE.toString(), defaultActivityDelay.getScale().toString());
		defaultProperties.setProperty(TimeProperty.DEFAULT_ACTIVITY_DELAY_DEVIATION.toString(), defaultActivityDelayDeviation.toString());
		
		defaultProperties.setProperty(TimeProperty.DAY_START.toString(), defaultDayStart.toString());
		defaultProperties.setProperty(TimeProperty.DAY_END.toString(), defaultDayEnd.toString());
		defaultProperties.setProperty(TimeProperty.CASES_PER_DAY.toString(), defaultCasesPerDay.toString());
		defaultProperties.setProperty(TimeProperty.CASE_STARTTIME_PRECISION.toString(), defaultCaseStarttimePrecision.toString());
		
		return defaultProperties;
	}
	
	@Override
	public TimeProperties clone(){
		return new TimeProperties((Properties) super.getProperties().clone());
	}
	
	//--------------------------------------------------------------------------------------
	
	private enum WorkingHourProperty {
		DAY_START, DAY_END;
	}
	
	private enum DelayBound {
		MIN_DELAY, MAX_DELAY;
	}
	
	private enum DurationBound {
		MIN_DURATION, MAX_DURATION;
	}
	
	/**
	 * Enumeration for the precision of case starting times.
	 * @author Thomas Stocker
	 */
	public enum CaseStartPrecision {HOUR, MINUTE, SECOND, MILLISECOND}
	
	
	
	public void store() throws IOException, PropertyException{
		store("time_generators/"+getName());
	}

}
