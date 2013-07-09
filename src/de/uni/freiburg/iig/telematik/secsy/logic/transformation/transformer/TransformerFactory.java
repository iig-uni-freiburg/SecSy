package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer;

import java.io.IOException;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.BoDTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.DayDelayTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.ObfuscationTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SoDTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.UnauthorizedExecutionTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.BoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.SoDPropertyTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.DayDelayTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.IncompleteLoggingTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.ObfuscationTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.SkipActivitiesTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple.UnauthorizedExecutionTransformer;


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
