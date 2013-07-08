package logic.transformation.transformer;

import java.io.IOException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;

import logic.transformation.transformer.properties.AbstractTransformerProperties;
import logic.transformation.transformer.properties.BoDTransformerProperties;
import logic.transformation.transformer.properties.DayDelayTransformerProperties;
import logic.transformation.transformer.properties.ObfuscationTransformerProperties;
import logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;
import logic.transformation.transformer.properties.SoDTransformerProperties;
import logic.transformation.transformer.properties.UnauthorizedExecutionTransformerProperties;
import logic.transformation.transformer.trace.BoDPropertyTransformer;
import logic.transformation.transformer.trace.SoDPropertyTransformer;
import logic.transformation.transformer.trace.multiple.DayDelayTransformer;
import logic.transformation.transformer.trace.multiple.IncompleteLoggingTransformer;
import logic.transformation.transformer.trace.multiple.ObfuscationTransformer;
import logic.transformation.transformer.trace.multiple.SkipActivitiesTransformer;
import logic.transformation.transformer.trace.multiple.UnauthorizedExecutionTransformer;

public class TransformerFactory {
	
	public static AbstractTransformer loadTransformer(String file) throws IOException, ParameterException, PropertyException{
		
		AbstractTransformerProperties properties = new AbstractTransformerProperties();
		properties.load(file);
		
		switch(properties.getType()){
			case DAY_DELAY: 	
				return new DayDelayTransformer(new DayDelayTransformerProperties(file));
			case BOD:		
				return new BoDPropertyTransformer(new BoDTransformerProperties(file));
			case SOD:		
				return new SoDPropertyTransformer(new SoDTransformerProperties(file));
			case OBFUSCATION: 
				return new ObfuscationTransformer(new ObfuscationTransformerProperties(file));
			case SKIP_ACTIVITIES: 
				return new SkipActivitiesTransformer(new SkipActivitiesTransformerProperties(file));
			case INCOMPLETE_LOGGING: 
				return new IncompleteLoggingTransformer(new SkipActivitiesTransformerProperties(file));
			case UNAUTHORIZED_EXECUTION: 
				return new UnauthorizedExecutionTransformer(new UnauthorizedExecutionTransformerProperties(file));
		}
		return null;
	}

}
