package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties;

import java.io.IOException;
import java.util.Properties;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;


public class DayDelayTransformerProperties extends AbstractMultipleTraceTransformerProperties {
	
	public static final Integer defaultMinDays = 1;
	public static final Integer defaultMaxDays = 1;
	
	public DayDelayTransformerProperties() {
		super();
	}

	public DayDelayTransformerProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setDayBounds(Integer minDays, Integer maxDays){
		validateDayBounds(minDays, maxDays);
		props.setProperty(DayDelayTransformerProperty.MIN_DAYS.toString(), minDays.toString());
		props.setProperty(DayDelayTransformerProperty.MAX_DAYS.toString(), maxDays.toString());
	}
	
	public Integer getMinDays() throws PropertyException{
		return getDays(DayBound.MIN_DAY);
	}
	
	public Integer getMaxDays() throws PropertyException{
		return getDays(DayBound.MAX_DAY);
	}
	
	private Integer getDays(DayBound bound) throws PropertyException{
		String minDays = props.getProperty(DayDelayTransformerProperty.MIN_DAYS.toString());
		String maxDays = props.getProperty(DayDelayTransformerProperty.MAX_DAYS.toString());
		if(minDays == null)
			throw new PropertyException(DayDelayTransformerProperty.MIN_DAYS, minDays);
		if(maxDays == null)
			throw new PropertyException(DayDelayTransformerProperty.MAX_DAYS, maxDays);
		
		Integer minTime = null;
		Integer maxTime = null;
		try{
			minTime = Integer.valueOf(minDays);
		}catch(Exception e){
			throw new PropertyException(DayDelayTransformerProperty.MIN_DAYS, minDays);
		}
		try{
			maxTime = Integer.valueOf(maxDays);
		}catch(Exception e){
			throw new PropertyException(DayDelayTransformerProperty.MAX_DAYS, maxDays);
		}
		validateDayBounds(minTime, maxTime);
		if(bound == DayBound.MIN_DAY){
			return minTime;
		} else {
			return maxTime;
		}
	}
	
	public static void validateDayBounds(Integer minDays, Integer maxDays){
		Validate.notNull(minDays);
		Validate.notNull(maxDays);
		Validate.notNegative(minDays);
		Validate.notNegative(maxDays);
		Validate.minMax(minDays, maxDays);
	}

	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(DayDelayTransformerProperty.MIN_DAYS.toString(), defaultMinDays.toString());
		defaultProperties.setProperty(DayDelayTransformerProperty.MAX_DAYS.toString(), defaultMaxDays.toString());
		return defaultProperties;
	}
	
	private enum DayDelayTransformerProperty {
		MIN_DAYS, MAX_DAYS, TIME_GENERATOR_NAME;
	}
	
	private enum DayBound {
		MIN_DAY, MAX_DAY;
	}
	
}
