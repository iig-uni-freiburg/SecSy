package logic.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.misc.FormatUtils;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogPerspective;
import de.uni.freiburg.iig.telematik.jawl.writer.LogWriter;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;

import logic.generator.time.CaseTimeGenerator;
import logic.simulation.ConfigurationException;
import logic.simulation.ConfigurationException.ErrorCode;
import logic.simulation.SimulationListener;
import logic.simulation.SimulationListenerSupport;
import logic.simulation.SimulationRun;
import logic.transformation.TransformerListener;
import logic.transformation.transformer.TransformerType;
import logic.transformation.transformer.trace.AbstractTraceFilter;
import logic.transformation.transformer.trace.multiple.UnauthorizedExecutionFilter;
import petrinet.AbstractPetriNet;


public abstract class LogGenerator implements TransformerListener{
	
	public static final String DEFAULT_LOG_PATH = "logs/";
	public static final String DEFAULT_FILE_NAME = "LOG";
	
	protected LogPerspective logPerspective = null;
	protected LogFormat logFormat = null;
	protected String fileName = null;
	protected String logPath = null;
	protected List<SimulationRun> simulationRuns = new ArrayList<SimulationRun>();
	protected LogWriter logWriter = null;
	
	protected int startedTraces = 0;
	
	protected Set<TraceStartListener> traceStartListeners = new HashSet<TraceStartListener>();
	protected Set<TraceCompletionListener> traceCompletionListeners = new HashSet<TraceCompletionListener>();
	protected Set<TraceListener> traceListeners = new HashSet<TraceListener>();
	protected Set<EntryListener> entryListeners = new HashSet<EntryListener>();
	
	
	protected LogEntryGenerator logEntryGenerator = null;
	protected CaseTimeGenerator caseTimeGenerator = null;
	protected CaseGenerator caseGenerator = new CaseGenerator();
	
	protected SimulationListenerSupport simulationListenerSupport = new SimulationListenerSupport();
	
	//------- Constructors -------------------------------------------------------------------
	
	public LogGenerator(LogFormat logFormat, LogPerspective logPerspective) 
			throws ParameterException, PerspectiveException, IOException {
		this(logFormat, logPerspective, DEFAULT_LOG_PATH, DEFAULT_FILE_NAME);
	}
	
	public LogGenerator(LogFormat logFormat, LogPerspective logPerspective, String fileName) 
			throws ParameterException, PerspectiveException, IOException {
		this(logFormat, logPerspective, DEFAULT_LOG_PATH, fileName);
	}
	
	public LogGenerator(LogFormat logFormat, LogPerspective logPerspective, String logPath, String fileName) 
			throws ParameterException, PerspectiveException, IOException {
		initialize(logFormat, logPerspective, logPath, fileName);
		
	}
	
	//------- Getters and Setters ------------------------------------------------------------
	
	public LogEntryGenerator getLogEntryGenerator(){
		return logEntryGenerator;
	}
	
	public CaseTimeGenerator getCaseTimeGenerator(){
		return caseTimeGenerator;
	}
	
	public LogFormat getLogFormat(){
		return logFormat;
	}
	
	public void setLogFormat(LogFormat logFormat) throws PerspectiveException{
		this.logFormat = logFormat;
		logFormat.setLogPerspective(this.logPerspective);
	}
	
	public String getLogPath() {
		return logWriter.getPath();
	}
	
	public String getLogFileSize(){
		File file = new File(getFileName());
		return FormatUtils.formatFileSize((double) file.length());
	}
	
	public String getFileName() {
		return logWriter.getFileName();
	}
	
	public String getFileNameShort(){
		return fileName;
	}
	
	public void registerTraceStartListener(TraceStartListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceStartListeners.add(listener);
	}
	
	public void removeTraceStartListener(TraceStartListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceStartListeners.remove(listener);
	}
	
	public void registerTraceCompletionListener(TraceCompletionListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceCompletionListeners.add(listener);
	}
	
	public void removeTraceCompletionListener(TraceCompletionListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceCompletionListeners.remove(listener);
	}
	
	public void registerTraceListener(TraceListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceListeners.add(listener);
	}
	
	public void removeTraceListener(TraceListener listener) throws ParameterException{
		Validate.notNull(listener);
		traceListeners.remove(listener);
	}
	
	public void registerEntryListener(EntryListener listener) throws ParameterException{
		Validate.notNull(listener);
		entryListeners.add(listener);
	}
	
	public void removeEntryListener(EntryListener listener) throws ParameterException{
		Validate.notNull(listener);
		entryListeners.remove(listener);
	}
	
	public void addSimulationListener(SimulationListener listener) throws ParameterException{
		simulationListenerSupport.addSimulationListener(listener);
	}
	
	public void removeSimulationListener(SimulationListener listener){
		simulationListenerSupport.removeSimulationListener(listener);
	}
	
	//------- Methods to set up the log generator --------------------------------------------
	
	protected void initialize(LogFormat logFormat, LogPerspective logPerspective, String logPath, String fileName) 
			throws ParameterException, PerspectiveException,  IOException {
		this.logFormat = logFormat;
		setLogPerspective(logPerspective);
		prepareLogWriter(logFormat, logPath, fileName);
		this.fileName = fileName;
		this.logPath = logPath;
	}
	
	public void setLogPath(String path) throws CompatibilityException, PerspectiveException, ParameterException, IOException{
		prepareLogWriter(getLogFormat(), path, fileName);
		this.logPath = path;
	}
	
	
	/**
	 * If there is already an open file, it is closed.
	 * @throws PerspectiveException - if the log format does not support the writers' log perspective.
	 * @throws CompatibilityException - if the charset of the log writer is not supported by the log format.
	 * @throws ParameterException - if some parameters are null or file name is an empty string.
	 * @throws IOException - if output file creation or header writing cause an exception.
	 */
	protected void prepareLogWriter(LogFormat logFormat, String logPath, String fileName) throws CompatibilityException, PerspectiveException, ParameterException, IOException {
		if(logWriter != null){
			logWriter.closeFile();
		}
		logWriter = new LogWriter(logFormat, logPath, fileName);
	}
	
	/**
	 * 
	 * @param logPerspective
	 * @throws PerspectiveException  if the given log perspective is incompatible with the generators' log format.
	 * @throws ParameterException if the given log perspective is <code>null</code>.
	 */
	protected void setLogPerspective(LogPerspective logPerspective) throws PerspectiveException, ParameterException{
		Validate.notNull(logPerspective);
		this.logPerspective = logPerspective;
		logFormat.setLogPerspective(logPerspective);
	}
	
	public void setCaseTimeGenerator(CaseTimeGenerator caseTimeGenerator) throws ParameterException{
		Validate.notNull(caseTimeGenerator);
		if(this.caseTimeGenerator != null){
			removeTraceStartListener(this.caseTimeGenerator);
			removeTraceCompletionListener(this.caseTimeGenerator);
		}
		registerTraceStartListener(caseTimeGenerator);
		registerTraceCompletionListener(caseTimeGenerator);
		this.caseTimeGenerator = caseTimeGenerator;
	}
	
	public void setLogEntryGenerator(LogEntryGenerator logEntryGenerator) throws ParameterException{
		Validate.notNull(logEntryGenerator);
		if(this.logEntryGenerator != null){
			removeTraceCompletionListener(this.logEntryGenerator);
		}
		registerTraceCompletionListener(logEntryGenerator);
		this.logEntryGenerator = logEntryGenerator;
	}
	
	public boolean isValid() {
		try{
			checkValidity();
			return true;
		}catch(ConfigurationException e){
			return false;
		}
	}
	
	private void checkValidity() throws ConfigurationException {
		if(logEntryGenerator == null)
			throw new ConfigurationException(ErrorCode.NO_ENTRYGENERATOR);
		logEntryGenerator.checkValidity();
		if(caseTimeGenerator == null)
			throw new ConfigurationException(ErrorCode.NO_TIMEGENERATOR);
	}
	
	//------- Functionality ------------------------------------------------------------------
	
	public void addSimulationRuns(SimulationRun... runs) throws ParameterException {
		addSimulationRuns(Arrays.asList(runs));
	}
	
	public void addSimulationRuns(Collection<SimulationRun> runs) throws ParameterException{
		Validate.notNull(runs);
		for(SimulationRun simulationRun: runs){
			addSimulationRun(simulationRun);
		}
	}
	
	public void addSimulationRun(SimulationRun simulationRun) throws ParameterException{
		Validate.notNull(simulationRun);
		if(simulationRuns.add(simulationRun)){
			try {
				registerTraceCompletionListener(simulationRun);
			} catch (ParameterException e) {
				// Cannot happen, since simulation run is not null
				e.printStackTrace();
			}
			simulationRun.getTraceFilterManager().registerFilterListener(this);
		}
	}
	
	public boolean containsSimulationRuns(){
		return !simulationRuns.isEmpty();
	}
	
	public List<SimulationRun> getSimulationRuns(){
		return Collections.unmodifiableList(simulationRuns);
	}
	
	public void removeAllSimulationRuns(){
		try {
			for (SimulationRun run : simulationRuns)
				removeTraceCompletionListener(run);
		} catch (ParameterException e) {
			// Cannot happen, since all runs are taken from the list simulationRuns and thus are not null
			e.printStackTrace();
		}
		simulationRuns.clear();
	}
	
	/**
	 * This method generates the frame of a log file using format specific headers and footers.
	 * The content of the log depends on which nets are processed how often and in which way
	 * the executions are recorded. This method calls {@link #startSimulation(FileWriter)}
	 * to generate the log content.
	 * @throws ConfigurationException 
	 * @throws SimulationException 
	 * @throws IOException 
	 * @throws ParameterException 
	 * @throws InconsistencyException 
	 * @throws Exception If log generator is in invalid state,<br>
	 * i.e. either the log entry generator or the case time generator are not set.
	 */
	public void generateLog() throws SimulationException, IOException, ConfigurationException{
		startSimulation();

		reset();
		try {
			prepareLogWriter(logFormat, logPath, fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This method generates the content of the log in the sense of log entries.
	 * The default behavior is to sequentially process the maintained list of
	 * simulation runs by calling {@link #simulateNet(AbstractPetriNet, int, FileWriter)}.
	 * Subclasses can override this method to control the way of creating log content.
	 * @param fileWriter The file handle.
	 * @throws SimulationException if Exceptions occur during the simulation of nets.
	 * @throws IOException if I/O errors occur on writing simulated cases to system files.
	 * @throws ConfigurationException if {@link #caseTimeGenerator} or {@link #logEntryGenerator} were not set yet.
	 */
	public void startSimulation() throws SimulationException, IOException, ConfigurationException{
		if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
			Context context = ((DetailedLogEntryGenerator) logEntryGenerator).getContext();
			try {
				((DetailedLogEntryGenerator) logEntryGenerator).getCaseDataContainer().setContext(context);
			} catch (ParameterException e) {
				throw new ConfigurationException(ErrorCode.CONTEXT_INCONSISTENCY, "Cannot assign simulation context to case data container.");
			}
		}
		logEntryGenerator.checkValidity();
		
		checkValidity();
		for(SimulationRun run: simulationRuns){
			simulationListenerSupport.fireSimulationMessage("Starting new simulation run: "+run.getName());
			simulationListenerSupport.fireSimulationRunStarted(run);
			simulationListenerSupport.fireSimulationMessage("Checking filter requirements.");
			checkFilterRequirements(run);
			simulationListenerSupport.fireSimulationMessage("Simulating net \""+run.getPetriNet().getName()+"\" ("+run.getPasses()+" passes).\n");
			simulateNet(run);
			simulationListenerSupport.fireSimulationMessage(run.getTraceFilterManager().getTransformerSummary());
			simulationListenerSupport.fireSimulationRunCompleted(run);
		}
	}
	
	private void checkFilterRequirements(SimulationRun run) throws ConfigurationException{
		for(AbstractTraceFilter traceFilter: run.getTraceFilterManager().getTraceTransformers()){
			if(traceFilter.getFilterType().equals(TransformerType.UNAUTHORIZED_EXECUTION_FILTER)){
				if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
					try {
						((UnauthorizedExecutionFilter) traceFilter).setContext(((DetailedLogEntryGenerator) getLogEntryGenerator()).getContext());
					} catch (ParameterException e) {
						throw new ConfigurationException(ErrorCode.CONTEXT_INCONSISTENCY, "Missing context reference in UnauthorizedExecution-filter");
					}
				}
			}
		}
	}

	public Integer startNextCase() {
		startedTraces++;
		for (TraceStartListener listener : traceStartListeners)
			try {
				listener.traceStarted(startedTraces);
			} catch (ParameterException e) {
				// Cannot happen, since startedTraces is always positive.
				e.printStackTrace();
			}
		return startedTraces;
	}

	public void completeCase(int caseNumber) {
		for (TraceCompletionListener listener : traceCompletionListeners)
			try {
				listener.traceCompleted(caseNumber);
			} catch (ParameterException e) {
				// Cannot happen, since startedTraces is always positive.
				e.printStackTrace();
			}
	}
	
	/**
	 * Notifies all entry listeners about the generation of a new entry.<br>
	 * It is assumed that the caller ensures parameter validity.
	 * @param entry The newly generated log entry.
	 */
	protected void notifyEntryListeners(LogEntry entry){
		for(EntryListener listener: entryListeners){
			listener.entryGenerated(entry);
		}
	}
	
	/**
	 * Notifies all trace listeners about the generation of a new trace.<br>
	 * It is assumed that the caller ensures parameter validity.
	 * @param entry The newly generated log trace.
	 */
	protected void notifyTraceListeners(LogTrace trace){
		for(TraceListener listener: traceListeners){
			listener.traceGenerated(trace);
		}
	}
	
	public void reset(){
		caseGenerator = new CaseGenerator();
		startedTraces = 0;
		for(SimulationRun simulationRun: simulationRuns){
			simulationRun.reset();
		}
	}
	
	/**
	 * This method simulates a Petri net and writes executions in a file.
	 * Subclasses can override this method to generate specific log files,
	 * (i.e. complete log traces of a workflow instance).<br>
	 * This method is called by {@link #startSimulation()} for every simulation run.<br>
	 * @param simulationRun
	 * @throws SimulationException if errors occur during the simulation run.
	 * @throws IOException if errors occur on writing simulated process traces to system files.
	 */
	protected abstract void simulateNet(SimulationRun simulationRun) throws SimulationException, IOException;
}
