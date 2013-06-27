package logic.filtering.filter;

import java.io.IOException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;

import logic.filtering.filter.properties.AbstractFilterProperties;
import logic.filtering.filter.properties.BoDFilterProperties;
import logic.filtering.filter.properties.DayDelayFilterProperties;
import logic.filtering.filter.properties.ObfuscationFilterProperties;
import logic.filtering.filter.properties.SkipActivitiesFilterProperties;
import logic.filtering.filter.properties.SoDFilterProperties;
import logic.filtering.filter.properties.UnauthorizedExecutionFilterProperties;
import logic.filtering.filter.trace.BoDPropertyFilter;
import logic.filtering.filter.trace.SoDPropertyFilter;
import logic.filtering.filter.trace.multiple.DayDelayFilter;
import logic.filtering.filter.trace.multiple.IncompleteLoggingFilter;
import logic.filtering.filter.trace.multiple.ObfuscationFilter;
import logic.filtering.filter.trace.multiple.SkipActivitiesFilter;
import logic.filtering.filter.trace.multiple.UnauthorizedExecutionFilter;

public class FilterFactory {
	
	public static AbstractFilter loadFilter(String file) throws IOException, ParameterException, PropertyException{
		
		AbstractFilterProperties properties = new AbstractFilterProperties();
		properties.load(file);
		
		switch(properties.getFilterType()){
			case DAY_DELAY_FILTER: 	
				return new DayDelayFilter(new DayDelayFilterProperties(file));
			case BOD_FILTER:		
				return new BoDPropertyFilter(new BoDFilterProperties(file));
			case SOD_FILTER:		
				return new SoDPropertyFilter(new SoDFilterProperties(file));
			case OBFUSCATION_FILTER: 
				return new ObfuscationFilter(new ObfuscationFilterProperties(file));
			case SKIP_ACTIVITIES_FILTER: 
				return new SkipActivitiesFilter(new SkipActivitiesFilterProperties(file));
			case INCOMPLETE_LOGGING_FILTER: 
				return new IncompleteLoggingFilter(new SkipActivitiesFilterProperties(file));
			case UNAUTHORIZED_EXECUTION_FILTER: 
				return new UnauthorizedExecutionFilter(new UnauthorizedExecutionFilterProperties(file));
		}
		return null;
	}

}
