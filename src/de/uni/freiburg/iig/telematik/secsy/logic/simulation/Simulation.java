package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.SimulationException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeGeneratorFactory;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.properties.TimeProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException.ErrorCode;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EntryGenerationType;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.SimulationProperties;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;


public class Simulation implements SimulationListener{

	public static Calendar calendar = new GregorianCalendar(Calendar.getInstance().getTimeZone());
	
	protected CaseTimeGenerator timeGenerator = null;
	protected LogEntryGenerator entryGenerator = null;
	protected LogGenerator logGenerator = null;
	protected String name = null;
	
	protected static final String toStringFormat = " Simulation name: %s\n\n" +
			   									   "   Log file name: %s\n" +
			   									   "      Log format: %s\n" +
			   									   "        Log path: %s\n" +
			   									   "Entry generation: %s\n\n\n" +
			   									   "%s\n\n" + //Time generator
			   									   "%s\n\n" + //Context + Data Container
			   									   "%s\n\n";  //Simulation runs
	
	private static final String simulationRunsFormat = "Simulation runs:\n\n%s\n";
	private static final String simulationRunFormat = "%s\n";
	private static final String contextDataContainerFormat = "%s\n%s\n";
	
	private SimulationListenerSupport simulationListenerSupport = new SimulationListenerSupport();
	
	public Simulation(){}
	
	public Simulation(LogGenerator logGenerator, LogEntryGenerator entryGenerator, CaseTimeGenerator timeGenerator) 
			throws ConfigurationException, ParameterException {
		setLogGenerator(logGenerator);
		setLogEntryGenerator(entryGenerator);
		setCaseTimeGenerator(timeGenerator);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addSimulationListener(SimulationListener listener) throws ParameterException{
		simulationListenerSupport.addSimulationListener(listener);
	}
	
	public void removeSimulationListener(SimulationListener listener){
		simulationListenerSupport.removeSimulationListener(listener);
	}

	public void executeSimulation() throws ConfigurationException, SimulationException, IOException {
		checkValidity();
		long startTime = System.currentTimeMillis();
		logGenerator.generateLog();
		long endTime = System.currentTimeMillis();
		System.out.println("Simulation time: " + (endTime - startTime) / 1000.0 + " s");
	}
	
	public void setLogGenerator(LogGenerator logGenerator) throws ConfigurationException, ParameterException{
		this.logGenerator = logGenerator;
		validateLogGenerator();
		if(entryGenerator != null)
			this.logGenerator.setLogEntryGenerator(entryGenerator);
		if(timeGenerator != null)
			this.logGenerator.setCaseTimeGenerator(timeGenerator);
		this.logGenerator.addSimulationListener(this);
	}
	
	public LogGenerator getLogGenerator(){
		return logGenerator;
	}
	
	public LogEntryGenerator getLogEntryGenerator(){
		return entryGenerator;
	}
	
	public void setLogEntryGenerator(LogEntryGenerator entryGenerator) throws ConfigurationException, ParameterException {
		this.entryGenerator = entryGenerator;
		validateEntryGenerator();
		if(logGenerator != null)
			logGenerator.setLogEntryGenerator(entryGenerator);
	}
	
	public void setCaseTimeGenerator(CaseTimeGenerator timeGenerator) throws ConfigurationException, ParameterException{
		this.timeGenerator = timeGenerator;
		validateTimeGenerator();
		if(logGenerator != null)
			logGenerator.setCaseTimeGenerator(timeGenerator);
	}
	
	public void setCaseTimeGenerator(TimeProperties timeProperties) 
			throws ConfigurationException, ParameterException, PropertyException {
		setCaseTimeGenerator(TimeGeneratorFactory.createCaseTimeGenerator(timeProperties));
	}
	
	public CaseTimeGenerator getCaseTimeGenerator(){
		return timeGenerator;
	}
	
	public void setSimulationRuns(Collection<SimulationRun> runs) throws ParameterException{
		logGenerator.removeAllSimulationRuns();
		logGenerator.addSimulationRuns(runs);
	}
	
	public void setSimulationRuns(SimulationRun...runs) throws ParameterException{
		logGenerator.removeAllSimulationRuns();
		logGenerator.addSimulationRuns(runs);
	}
	
	public void addSimulationRun(SimulationRun simulationRun) throws ParameterException, ConfigurationException {
		Validate.notNull(simulationRun);
		
		//Check if the log entry generator is set
		if(getLogEntryGenerator() == null){
			throw new ConfigurationException(ErrorCode.NO_ENTRYGENERATOR, "Cannot add simulation runs without log entry generator.");
		}
		
		//Check the log entry generation type
		if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			//Check if the context is set
			if(((DetailedLogEntryGenerator) getLogEntryGenerator()).getContext() == null){
				throw new ConfigurationException(ErrorCode.NO_CONTEXT, "Cannot add simulation runs without context.");
			}
			if(!((DetailedLogEntryGenerator) getLogEntryGenerator()).getContext().getActivities().containsAll(PNUtils.getLabelSetFromTransitions(simulationRun.getPetriNet().getTransitions()))){
				//At least one activity of the simulation run is not contained in the context
				//-> Abort adding the simulation run to avoid inconsistencies.
				throw new ConfigurationException(ErrorCode.CONTEXT_INCONSISTENCY, "The simulation run contains unknown activities which are not contained in the context.");
			}
		}
		
		logGenerator.addSimulationRun(simulationRun);
	}
	
	public void addSimulationRuns(SimulationRun... simulationRuns) throws ParameterException, ConfigurationException {
		addSimulationRuns(Arrays.asList(simulationRuns));
	}
	
	public void addSimulationRuns(Collection<SimulationRun> simulationRuns) throws ParameterException, ConfigurationException{
		Validate.notNull(simulationRuns);
		for(SimulationRun simulationRun: simulationRuns)
			addSimulationRun(simulationRun);
	}
	
	public boolean containsSimulationRuns(){
		return logGenerator.containsSimulationRuns();
	}
	
	public List<SimulationRun> getSimulationRuns() throws ConfigurationException {
		validateLogGenerator();
		return Collections.unmodifiableList(logGenerator.getSimulationRuns());
	}
	
	public boolean isValid(){
		try { 
			checkValidity();
		} catch(ConfigurationException e){
			return false;
		}
		return true;
	}
	
	public SimulationProperties getProperties() throws ParameterException{
		if(!isValid()){
			throw new ParameterException(de.invation.code.toval.validate.ParameterException.ErrorCode.INCONSISTENCY, "Cannot extract properties in invalid state.");
		}
		
		SimulationProperties properties = new SimulationProperties();
		
		properties.setName(getName());
		
		properties.setFileName(getLogGenerator().getFileNameShort());
		
		properties.setLogFormat(getLogGenerator().getLogFormat().getLogFormatType());
		
		properties.setTimeGeneratorName(getCaseTimeGenerator().getName());
		
		properties.setEventHandling(((TraceLogGenerator) getLogGenerator()).getEventHandling());
		
		if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			properties.setEntryGeneration(EntryGenerationType.DETAILED);
			properties.setContextName(((DetailedLogEntryGenerator) getLogEntryGenerator()).getContext().getName());
			properties.setDataContainerName(((DetailedLogEntryGenerator) getLogEntryGenerator()).getCaseDataContainer().getName());
		} else {
			properties.setEntryGeneration(EntryGenerationType.SIMPLE);
		}
		
		try {
			for(SimulationRun simulationRun: getSimulationRuns()){
				properties.addSimulationRun(simulationRun);
			}
		} catch (ConfigurationException e) {
			// Should not happen, since the simulation is valid.
			e.printStackTrace();
		}
		
		return properties;
	}
	
	public void checkValidity() throws ConfigurationException{
		validateLogGenerator();
		validateEntryGenerator();
		validateTimeGenerator();
	}
	
	private void validateLogGenerator() throws ConfigurationException{
		if(logGenerator == null)
			throw new ConfigurationException(ErrorCode.NO_LOGGENERATOR);
	}
	
	private void validateEntryGenerator() throws ConfigurationException{
		if(entryGenerator == null)
			throw new ConfigurationException(ErrorCode.NO_ENTRYGENERATOR);
	}
	
	private void validateTimeGenerator() throws ConfigurationException{
		if(timeGenerator == null)
			throw new ConfigurationException(ErrorCode.NO_TIMEGENERATOR);
	}
	
	@Override
	public String toString(){
		return String.format(toStringFormat, getName(), 
											 getLogGenerator().getFileNameShort(), 
											 getLogGenerator().getLogFormat().getLogFormatType(), 
											 getLogGenerator().getLogPath(),
											 getEntryGenerationTypeString(),
											 getCaseTimeGenerator().toString(),
											 getContextDataContainerString(),
											 getSimulationRunsString());
	}
	
	private String getContextDataContainerString(){
		if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			Context context = ((DetailedLogEntryGenerator) getLogEntryGenerator()).getContext();
			CaseDataContainer dataContainer = ((DetailedLogEntryGenerator) getLogEntryGenerator()).getCaseDataContainer();
			if(context != null && dataContainer != null){
				return String.format(contextDataContainerFormat, context.toString(), dataContainer.toString());
			} else {
				return "Cannot extract context anddata container (incomplete definition or misconfiguration)";
			}
		} else {
			return "";
		}
	}
	
	private String getSimulationRunsString(){
		try {
			StringBuilder builder = new StringBuilder();
			for(SimulationRun run: getSimulationRuns()){
				builder.append(String.format(simulationRunFormat, run.toString()));
			}
			return String.format(simulationRunsFormat, builder.toString());
		} catch(ConfigurationException e){
			return "Cannot extract simulation runs (configuration exception).\n";
		}
	}
	
	private String getEntryGenerationTypeString(){
		if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			return "DETAILED";
		}
		return "SIMPLE";
	}
	
	public void takeoverValues(Simulation otherSimulation) throws ConfigurationException, ParameterException{
		this.setName(otherSimulation.getName());
		this.setCaseTimeGenerator(otherSimulation.getCaseTimeGenerator());
		this.setLogGenerator(otherSimulation.getLogGenerator());
		this.setLogEntryGenerator(otherSimulation.getLogEntryGenerator());
		this.setSimulationRuns(otherSimulation.getSimulationRuns());
	}
	
	public void reset(){
		getLogGenerator().reset();
	}

	@Override
	public void simulationMessage(String message) {
		simulationListenerSupport.fireSimulationMessage(message);
	}

	@Override
	public void simulationRunStarted(SimulationRun simulationRun) {
		simulationListenerSupport.fireSimulationRunStarted(simulationRun);
	}

	@Override
	public void simulationRunCompleted(SimulationRun simulationRun) {
		simulationListenerSupport.fireSimulationRunCompleted(simulationRun);
	}
	
}
