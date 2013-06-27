package logic.simulation.properties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormatType;

import logic.filtering.filter.AbstractFilter;
import logic.simulation.SimulationRun;

public class SimulationProperties extends AbstractProperties{
	
	private static final String SIMULATION_RUN_FORMAT = SimulationProperty.SIMULATION_RUN + "_%s";
	private static final String SIMULATION_RUN_VALUE_FORMAT = "%s#%s#%s#%s";
	
	public static final String defaultName = "NewSimulation";
	public static final String defaultFileName = "NewSimulationOutput";
	public static final String defaultLogPath = "logs/";
	
	public static final EntryGenerationType defaultEntryGenerationType = EntryGenerationType.SIMPLE;
	public static final LogFormatType defaultLogFormat = LogFormatType.MXML;
	
	
	public SimulationProperties() throws ParameterException {
		super();
	}

	public SimulationProperties(String fileName) throws IOException, ParameterException {
		super(fileName);
	}
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(SimulationProperty simulationProperty, Object value){
		props.setProperty(simulationProperty.toString(), value.toString());
	}
	
	private String getProperty(SimulationProperty simulationProperty){
		return props.getProperty(simulationProperty.toString());
	}
	
	private void removeProperty(SimulationProperty simulationProperty){
		props.remove(simulationProperty.toString());
	}
	
	//-- Simulation name
	
	public void setName(String name) throws ParameterException{
		validateStringValue(name);
		setProperty(SimulationProperty.SIMULATION_NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(SimulationProperty.SIMULATION_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.SIMULATION_NAME, propertyValue);
		return propertyValue;
	}

//	//-- Log path
//	
//	public void setLogPath(String logPath) throws ParameterException{
//		validatePath(logPath);
//		setProperty(SimulationProperty.LOG_PATH, logPath);
//	}
//	
//	public String getLogPath() throws PropertyException {
//		String propertyValue = getProperty(SimulationProperty.LOG_PATH);
//		if(propertyValue == null)
//			throw new PropertyException(SimulationProperty.LOG_PATH, propertyValue);
//		return propertyValue;
//	}
	
	//-- File name
	
	public void setFileName(String fileName) throws ParameterException{
		validateStringValue(fileName);
		setProperty(SimulationProperty.FILE_NAME, fileName);
	}
	
	public String getFileName() throws PropertyException {
		String propertyValue = getProperty(SimulationProperty.FILE_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.FILE_NAME, propertyValue);
		return propertyValue;
	}
	
	//-- Entry generation type
	
	public void setEntryGeneration(EntryGenerationType entryGenerationType) throws ParameterException{
		Validate.notNull(entryGenerationType);
		setProperty(SimulationProperty.ENTRY_GENERATION, entryGenerationType.toString());
	}
	
	public EntryGenerationType getEntryGenerationType() throws PropertyException {
		String propertyValue = getProperty(SimulationProperty.ENTRY_GENERATION);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.ENTRY_GENERATION, propertyValue);
		EntryGenerationType generationType = null;
		try {
			generationType = EntryGenerationType.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(SimulationProperty.ENTRY_GENERATION, propertyValue);
		}
		return generationType;
	}
	
	
	//-- Log format
	
	public void setLogFormat(LogFormatType logFormatType) throws ParameterException{
		Validate.notNull(logFormatType);
		setProperty(SimulationProperty.LOG_FORMAT, logFormatType.toString());
	}
	
	public LogFormatType getLogFormatType() throws PropertyException {
		String propertyValue = getProperty(SimulationProperty.LOG_FORMAT);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.LOG_FORMAT, propertyValue);
		LogFormatType formatType = null;
		try {
			formatType = LogFormatType.valueOf(propertyValue);
		}catch(Exception e){
			throw new PropertyException(SimulationProperty.LOG_FORMAT, propertyValue);
		}
		return formatType;
	}
	
	//-- Context
	
	public void setContextName(String contextName) throws ParameterException{
		Validate.notNull(contextName);
		Validate.notEmpty(contextName);
		setProperty(SimulationProperty.CONTEXT_NAME, contextName);
	}
	
	public String getContextName() throws PropertyException, ParameterException {
		String propertyValue = getProperty(SimulationProperty.CONTEXT_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.CONTEXT_NAME, propertyValue);
		
		validateStringValue(propertyValue);
		
		return propertyValue;
	}
	
	public void removeContextName(){
		removeProperty(SimulationProperty.CONTEXT_NAME);
	}
	
	//-- Data container
	
	public void setDataContainerName(String containerName) throws ParameterException{
		Validate.notNull(containerName);
		Validate.notEmpty(containerName);
		setProperty(SimulationProperty.DATA_CONTAINER_NAME, containerName);
	}
	
	public String getDataContainerName() throws PropertyException, ParameterException {
		String propertyValue = getProperty(SimulationProperty.DATA_CONTAINER_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.DATA_CONTAINER_NAME, propertyValue);
		
		validateStringValue(propertyValue);
		
		return propertyValue;
	}
	
	public void removeDataContainerName(){
		removeProperty(SimulationProperty.DATA_CONTAINER_NAME);
	}
	
	//-- Time generator
	
	public void setTimeGeneratorName(String generatorName) throws ParameterException{
		validateStringValue(generatorName);
		setProperty(SimulationProperty.TIME_GENERATOR_NAME, generatorName);
	}
	
	public String getTimeGeneratorName() throws PropertyException, ParameterException {
		String propertyValue = getProperty(SimulationProperty.TIME_GENERATOR_NAME);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.TIME_GENERATOR_NAME, propertyValue);
		
		validateStringValue(propertyValue);
		
		return propertyValue;
	}
	
	//-- Simulation runs
	
	public void addSimulationRun(SimulationRun simulationRun) throws ParameterException{
		Validate.notNull(simulationRun);
		
		String simulationRunName = String.format(SIMULATION_RUN_FORMAT, getSimulationRunNames().size()+1);
		addSimulationRunName(simulationRunName);
		Set<String> filterNames = new HashSet<String>();
		for(AbstractFilter filter: simulationRun.getTraceFilterManager().getTraceFilters()){
			filterNames.add(filter.getName());
		}
		props.setProperty(simulationRunName, String.format(SIMULATION_RUN_VALUE_FORMAT, "'"+simulationRun.getName()+"'", "'"+simulationRun.getPetriNet().getName()+"'", simulationRun.getPasses(), ArrayUtils.toString(encapsulateValues(filterNames))));
	}
	
	public Set<SimulationRunProperties> getSimulationRuns() throws PropertyException, ParameterException{
		Set<SimulationRunProperties> result = new HashSet<SimulationRunProperties>();
		for(String simulationRunName: getSimulationRunNames()){
			result.add(getSimulationRunProperties(simulationRunName));
		}
		return result;
	}
	
	private SimulationRunProperties getSimulationRunProperties(String simulationRunName) throws PropertyException, ParameterException{
		validateStringValue(simulationRunName);
		
		String propertyValue = props.getProperty(simulationRunName);
		if(propertyValue == null)
			throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Cannot extract simulation run.");
		
		StringTokenizer tokenizer = new StringTokenizer(propertyValue, "#");
		List<String> tokens = new ArrayList<String>();
		while (tokenizer.hasMoreElements()) {
			tokens.add(tokenizer.nextElement().toString());
		}
		if(tokens.size() != 4){
			throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Invalid property value: Cannot extract simulation run information");
		}
		
		Integer passes = null;
		try{
			passes = Integer.parseInt(tokens.get(2));
		}catch(Exception e){
			throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Invalid property value: Cannot extract number of passes");
		}
		
		Set<String> filterNames = new HashSet<String>();
		StringTokenizer nameTokens = StringUtils.splitArrayString(tokens.get(3), " ");
		while(nameTokens.hasMoreTokens()){
			String filterNameEncapsulated = nameTokens.nextToken();
			if(filterNameEncapsulated.length() <= 2)
				throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Invalid property value: Too short filter name");
			filterNames.add(filterNameEncapsulated.substring(1, filterNameEncapsulated.length()-1));
		}
		
		if(tokens.get(0).length() < 3){
			throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Invalid property value: Cannot extract simulation run name");
		}
		if(tokens.get(1).length() < 3){
			throw new PropertyException(SimulationProperty.SIMULATION_RUN, propertyValue, "Invalid property value: Cannot extract Petri net name");
		}
		
		SimulationRunProperties result = new SimulationRunProperties();
		result.setName(tokens.get(0).substring(1, tokens.get(0).length()-1));
		result.setNetName(tokens.get(1).substring(1, tokens.get(1).length()-1));
		result.setPasses(passes);
		result.setFilterNames(filterNames);
		
		return result;
	}
	
	public void addSimulationRunName(String simulationRunName) throws ParameterException{
		validateStringValue(simulationRunName);
		
		Set<String> currentRuns = getSimulationRunNames();
		currentRuns.add(simulationRunName);
		setProperty(SimulationProperty.SIMULATION_RUNS, ArrayUtils.toString(currentRuns.toArray()));
	}
	
	public Set<String> getSimulationRunNames(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(SimulationProperty.SIMULATION_RUNS);
		if(propertyValue == null)
			return result;
		StringTokenizer nameTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(nameTokens.hasMoreTokens()){
			result.add(nameTokens.nextToken());
		}
		return result;
	}
	
	public boolean existSimulationRunNames(){
		return !getSimulationRunNames().isEmpty();
	}
	
	public void removeSimulationRunName(String simulationRunName) throws ParameterException{
		Validate.notNull(simulationRunName);
		Validate.notEmpty(simulationRunName);
		Set<String> currentRuns = getSimulationRunNames();
		currentRuns.remove(simulationRunName);
		setProperty(SimulationProperty.SIMULATION_RUNS, ArrayUtils.toString(currentRuns.toArray()));
	}
	
	//------- Default Properties -----------------------------------------------------------
	
	@Override
	protected Properties getDefaultProperties(){
		Properties defaultProperties = new Properties();
		
		defaultProperties.setProperty(SimulationProperty.SIMULATION_NAME.toString(), defaultName);
		defaultProperties.setProperty(SimulationProperty.FILE_NAME.toString(), defaultFileName);
		defaultProperties.setProperty(SimulationProperty.LOG_PATH.toString(), defaultLogPath);
		defaultProperties.setProperty(SimulationProperty.ENTRY_GENERATION.toString(), defaultEntryGenerationType.toString());
		defaultProperties.setProperty(SimulationProperty.LOG_FORMAT.toString(), defaultLogFormat.toString());
		
		return defaultProperties;
	}

}
