package logic.filtering.filter.properties;

import java.io.IOException;
import java.util.Properties;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;


public class DayDelayFilterProperties extends AbstractMultipleTraceFilterProperties {
	
	public static final Integer defaultMinDays = 1;
	public static final Integer defaultMaxDays = 1;
	
	public DayDelayFilterProperties() {
		super();
	}

	public DayDelayFilterProperties(String fileName) throws IOException {
		super(fileName);
	}
	
	public void setDayBounds(Integer minDays, Integer maxDays) throws ParameterException {
		validateDayBounds(minDays, maxDays);
		props.setProperty(DayDelayFilterProperty.MIN_DAYS.toString(), minDays.toString());
		props.setProperty(DayDelayFilterProperty.MAX_DAYS.toString(), maxDays.toString());
	}
	
	public Integer getMinDays() throws PropertyException, ParameterException{
		return getDays(DayBound.MIN_DAY);
	}
	
	public Integer getMaxDays() throws PropertyException, ParameterException{
		return getDays(DayBound.MAX_DAY);
	}
	
	private Integer getDays(DayBound bound) throws PropertyException, ParameterException{
		String minDays = props.getProperty(DayDelayFilterProperty.MIN_DAYS.toString());
		String maxDays = props.getProperty(DayDelayFilterProperty.MAX_DAYS.toString());
		if(minDays == null)
			throw new PropertyException(DayDelayFilterProperty.MIN_DAYS, minDays);
		if(maxDays == null)
			throw new PropertyException(DayDelayFilterProperty.MAX_DAYS, maxDays);
		
		Integer minTime = null;
		Integer maxTime = null;
		try{
			minTime = Integer.valueOf(minDays);
		}catch(Exception e){
			throw new PropertyException(DayDelayFilterProperty.MIN_DAYS, minDays);
		}
		try{
			maxTime = Integer.valueOf(maxDays);
		}catch(Exception e){
			throw new PropertyException(DayDelayFilterProperty.MAX_DAYS, maxDays);
		}
		validateDayBounds(minTime, maxTime);
		if(bound == DayBound.MIN_DAY){
			return minTime;
		} else {
			return maxTime;
		}
	}
	
	public static void validateDayBounds(Integer minDays, Integer maxDays) throws ParameterException{
		Validate.notNull(minDays);
		Validate.notNull(maxDays);
		Validate.notNegative(minDays);
		Validate.notNegative(maxDays);
		Validate.minMax(minDays, maxDays);
	}

	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = super.getDefaultProperties();
		defaultProperties.setProperty(DayDelayFilterProperty.MIN_DAYS.toString(), defaultMinDays.toString());
		defaultProperties.setProperty(DayDelayFilterProperty.MAX_DAYS.toString(), defaultMaxDays.toString());
		return defaultProperties;
	}
	
	private enum DayDelayFilterProperty {
		MIN_DAYS, MAX_DAYS, TIME_GENERATOR_NAME;
	}
	
	private enum DayBound {
		MIN_DAY, MAX_DAY;
	}
	
}
