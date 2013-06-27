package logic.simulation.properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;

import logformat.LogFormatType;

public class SimulationRunProperties extends AbstractProperties{
	
	public static final String defaultName = "NewSimulationRun";
	public static final String defaultFileName = "NewSimulationOutput";
	public static final String defaultLogPath = "logs/";
	
	public static final EntryGenerationType defaultEntryGenerationType = EntryGenerationType.SIMPLE;
	public static final LogFormatType defaultLogFormat = LogFormatType.MXML;
	
	
	public SimulationRunProperties() throws ParameterException {
		super();
	}

	public SimulationRunProperties(String fileName) throws IOException, ParameterException {
		super(fileName);
	}
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(SimulationRunProperty simulationRunProperty, Object value){
		props.setProperty(simulationRunProperty.toString(), value.toString());
	}
	
	private String getProperty(SimulationRunProperty simulationRunProperty){
		return props.getProperty(simulationRunProperty.toString());
	}
	
	//-- Simulation run name
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		Validate.notEmpty(name);
		setProperty(SimulationRunProperty.SIMULATION_RUN_NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(SimulationRunProperty.SIMULATION_RUN_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationRunProperty.SIMULATION_RUN_NAME, propertyValue);
		return propertyValue;
	}
	
	
	//-- Net name
	
	public void setNetName(String name) throws ParameterException{
		Validate.notNull(name);
		Validate.notEmpty(name);
		setProperty(SimulationRunProperty.NET_NAME, name);
	}
	
	public String getNetName() throws PropertyException {
		String propertyValue = getProperty(SimulationRunProperty.NET_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationRunProperty.NET_NAME, propertyValue);
		return propertyValue;
	}
	

	//-- Passes
	
	public void setPasses(Integer passes) throws ParameterException{
		Validate.notNull(passes);
		setProperty(SimulationRunProperty.PASSES, passes);
	}
	
	public Integer getPasses() throws PropertyException {
		String propertyValue = getProperty(SimulationRunProperty.PASSES);
		if(propertyValue == null)
			throw new PropertyException(SimulationRunProperty.PASSES, propertyValue);
		Integer passes = null;
		try{
			passes = Integer.parseInt(propertyValue);
		}catch(Exception e){
			throw new PropertyException(SimulationRunProperty.PASSES, propertyValue, "Invalid property value: Cannot extract number of passes");
		}
		
		return passes;
	}
	
	
	//-- Filter names
	
	public void setFilterNames(Set<String> filterNames) throws ParameterException{
		Validate.notNull(filterNames);
		Validate.noNullElements(filterNames);
		
		setProperty(SimulationRunProperty.FILTERS, ArrayUtils.toString(encapsulateValues(filterNames)));
	}
	
	public Set<String> getFilterNames(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(SimulationRunProperty.FILTERS);
		if(propertyValue == null)
			return result;
		StringTokenizer nameTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(nameTokens.hasMoreTokens()){
			String nextToken = nameTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
}
