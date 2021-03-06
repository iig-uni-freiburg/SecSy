package de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties;

import java.io.IOException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.time.Weekday;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.AdjustableCaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.RandomCaseTimeGenerator;


public class TimeGeneratorFactory {
	
	public static CaseTimeGenerator createCaseTimeGenerator(TimeProperties properties) throws PropertyException{
		CaseTimeGenerator timeGenerator;
		boolean randomSettings = 	properties.existDefaultActivityDelayBounds() ||
								 	properties.existsDayCaseDeviation() ||
								 	properties.existActivitiesWithIndividualDurationDeviation() ||
								 	properties.existActivitiesWithIndividualDelayDeviation();
		boolean adjustedSettings = 	properties.existActivitiesWithIndividualDelay() ||
								   	properties.existActivitiesWithIndividualDuration();
		
		if(randomSettings){
			// Create time generator of type RandomCaseTimeGenerator
			// and set appropriate properties.
			timeGenerator = new RandomCaseTimeGenerator(properties.getStartTime(), properties.getCasesPerDay());
			
			//Cases per day deviation
			if(properties.existsDayCaseDeviation())
				((RandomCaseTimeGenerator) timeGenerator).setDayCasesDeviation(properties.getCasesPerDayDeviation());
			
			//Default activity duration deviation
			if(properties.existsDefaultActivityDurationDeviation())
				((RandomCaseTimeGenerator) timeGenerator).setDefaultActivityDurationDeviation(properties.getDefaultActivityDurationDeviation());
			
			//Default activity delay deviation
			if(properties.existsDefaultActivityDelayDeviation())
				((RandomCaseTimeGenerator) timeGenerator).setDefaultActivityDelayDeviation(properties.getDefaultActivityDelayDeviation());
			
			//Default activity duration bounds
			if(properties.existDefaultActivityDurationBounds())
				((RandomCaseTimeGenerator) timeGenerator).setDefaultDurationBounds(properties.getDefaultActivityMinDuration(), properties.getDefaultActivityMaxDuration());
			
			//Default activity delay bounds
			if(properties.existDefaultActivityDelayBounds())
				((RandomCaseTimeGenerator) timeGenerator).setDefaultDelayBounds(properties.getDefaultActivityMinDelay(), properties.getDefaultActivityMaxDelay());
			
			//Individual duration deviations
			if(properties.existActivitiesWithIndividualDurationDeviation()){
				for(String activity: properties.getActivitiesWithIndividualDurationDeviation()){
					((RandomCaseTimeGenerator) timeGenerator).setDuration(activity, properties.getIndividualActivityDuration(activity), properties.getIndividualActivityDurationDeviation(activity));
				}
			}
			
			//Individual delay deviations
			if(properties.existActivitiesWithIndividualDelayDeviation()){
				for(String activity: properties.getActivitiesWithIndividualDelayDeviation()){
					((RandomCaseTimeGenerator) timeGenerator).setDelay(activity, properties.getIndividualActivityDelay(activity), properties.getIndividualActivityDelayDeviation(activity));
				}
			}
		} else if(adjustedSettings){
			// Create time generator of type AdjustableCaseTimeGenerator
			// and set appropriate properties.
			timeGenerator = new AdjustableCaseTimeGenerator(properties.getStartTime(), properties.getCasesPerDay());
			if(properties.existActivitiesWithIndividualDuration()){
				for(String activity: properties.getActivitiesWithIndividualDuration()){
					((AdjustableCaseTimeGenerator) timeGenerator).setDuration(activity, properties.getIndividualActivityDuration(activity));
				}
			}
			if(properties.existActivitiesWithIndividualDelay()){
				for(String activity: properties.getActivitiesWithIndividualDelay()){
					((AdjustableCaseTimeGenerator) timeGenerator).setDelay(activity, properties.getIndividualActivityDelay(activity));
				}
			}
		} else {
			// Create time generator of type CaseTimeGenerator
			timeGenerator = new CaseTimeGenerator(properties.getStartTime(), properties.getCasesPerDay());
		}
		
		// Set general properties for all timegenerators
		timeGenerator.removeSkipDays();
		for(Weekday weekday: properties.getSkipDays()){
			timeGenerator.skipDay(weekday);
		}
		timeGenerator.setDefaultDuration(properties.getDefaultActivityDuration());
		timeGenerator.setDefaultDelay(properties.getDefaultActivityDelay());
		timeGenerator.setWorkingHours(properties.getOfficeHoursStart(), properties.getOfficeHoursEnd());
		timeGenerator.setMaxCasesPerDay(properties.getCasesPerDay());
		timeGenerator.setCaseStartingTimePrecision(properties.getCaseStarttimePrecision());
		timeGenerator.setName(properties.getName());
		
		return timeGenerator;
	}
	
	public static CaseTimeGenerator parse(String propertyFile) throws IOException, PropertyException{
		return createCaseTimeGenerator(new TimeProperties(propertyFile));
	}

}
