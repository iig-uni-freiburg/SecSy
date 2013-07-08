package logic.transformation.transformer;

import java.io.IOException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;

import logic.transformation.transformer.properties.AbstractFilterProperties;
import logic.transformation.transformer.properties.BoDFilterProperties;
import logic.transformation.transformer.properties.DayDelayFilterProperties;
import logic.transformation.transformer.properties.ObfuscationFilterProperties;
import logic.transformation.transformer.properties.SkipActivitiesFilterProperties;
import logic.transformation.transformer.properties.SoDFilterProperties;
import logic.transformation.transformer.properties.UnauthorizedExecutionFilterProperties;
import logic.transformation.transformer.trace.BoDPropertyFilter;
import logic.transformation.transformer.trace.SoDPropertyFilter;
import logic.transformation.transformer.trace.multiple.DayDelayFilter;
import logic.transformation.transformer.trace.multiple.IncompleteLoggingFilter;
import logic.transformation.transformer.trace.multiple.ObfuscationFilter;
import logic.transformation.transformer.trace.multiple.SkipActivitiesFilter;
import logic.transformation.transformer.trace.multiple.UnauthorizedExecutionFilter;

public class TransformerFactory {
	
	public static AbstractTransformer loadFilter(String file) throws IOException, ParameterException, PropertyException{
		
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
