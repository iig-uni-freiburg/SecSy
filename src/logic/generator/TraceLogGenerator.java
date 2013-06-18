package logic.generator;

import java.io.IOException;
import java.util.Date;

import log.LockingException;
import log.LogEntry;
import log.LogTrace;
import logformat.LogFormat;
import logformat.LogPerspective;
import logic.filtering.EntryFilterManager;
import logic.filtering.TraceFilterManager;
import logic.filtering.filter.AbstractFilter;
import logic.generator.time.CaseTimeGenerator.ExecutionTime;
import logic.simulation.SimulationRun;
import misc.valuegeneration.ValueGenerationException;
import petrinet.AbstractTransition;
import validate.InconsistencyException;
import validate.ParameterException;
import writer.PerspectiveException;
import exception.PNException;

/**
 * This class overrides the abstract class {@link LogGenerator}.
 * It generates logs in a trace oriented manner.  
 * Every log entry contains a complete executions trace.
 * 
 * @author Thomas Stocker
 *
 */
public class TraceLogGenerator extends LogGenerator{
	
	public TraceLogGenerator(LogFormat logFormat) 
			throws ParameterException, IOException, PerspectiveException {
		super(logFormat, LogPerspective.TRACE_PERSPECTIVE);
	}
	
	public TraceLogGenerator(LogFormat logFormat, String fileName) 
			throws ParameterException, IOException, PerspectiveException {
		super(logFormat, LogPerspective.TRACE_PERSPECTIVE, fileName);
	}
	
	public TraceLogGenerator(LogFormat logFormat, String logPath, String fileName) 
			throws ParameterException, IOException, PerspectiveException {
		super(logFormat, LogPerspective.TRACE_PERSPECTIVE, logPath, fileName);
	}
	
	@Override
	public void setLogPerspective(LogPerspective logPerspective) throws UnsupportedOperationException{
		if(logPerspective != LogPerspective.TRACE_PERSPECTIVE)
			throw new IllegalArgumentException("Cannot set event perspective for trace log generator.");
	}
	
	@Override
	protected void simulateNet(SimulationRun simulationRun) throws SimulationException, IOException{
		EntryFilterManager entryFilterManager = null;
		TraceFilterManager traceFilterManager = null;
		try{
			entryFilterManager = simulationRun.getEntryFilterManager();
			entryFilterManager.setSource(getLogEntryGenerator());
			traceFilterManager = simulationRun.getTraceFilterManager();
			traceFilterManager.setSource(getLogEntryGenerator());
		}catch(ParameterException e){
			// Is only thrown if setSource() methods are called with null-Parameters.
			// This cannot happen, since simulateNet is called by startSimulation(),
			// which throws an Exception, in case the log entry generator is null.
			e.printStackTrace();
		}
		try {
			caseGenerator.setPetriNet(simulationRun.getPetriNet(), simulationRun.getPNTraverser());
			if(getLogEntryGenerator() instanceof DetailedLogEntryGenerator){
				DetailedLogEntryGenerator entryGenerator = (DetailedLogEntryGenerator) getLogEntryGenerator();
				caseGenerator.setContext(entryGenerator.getContext());
				caseGenerator.setCaseDataContainer(entryGenerator.getCaseDataContainer());
			}
		} catch (ParameterException e2) {
			throw new SimulationException("Case generator is in invalid state. Null-value for Petri net or PN-traverser.");
		}
			
		try {
			while(!simulationRun.isDone()){
				int nextCaseID = startNextCase();
				LogTrace trace = new LogTrace(nextCaseID);
				caseGenerator.newCase(nextCaseID);
				while(!caseGenerator.isCaseCompleted()){
					AbstractTransition<?,?> nextTransition = caseGenerator.getNextTransition();
					AbstractTransition<?,?> next = simulationRun.getPetriNet().fire(nextTransition.getName());
					if (next!=null && !next.isSilent()){
						LogEntry entry = null;
						try {
							entry = getLogEntryGenerator().getLogEntryFor(next,nextCaseID);
						} catch (ParameterException e1) {
							// Cannot happen, since next is not null and the case id is always positive.
							e1.printStackTrace();
						}
						if(entry!=null){
							try {
								addEntryToTrace(trace, entry, getCaseTimeGenerator().getTimeFor(next.getLabel(), nextCaseID));
							} catch (LockingException e) {
								// Cannot happen, since the entry is newly generated
								// and no fields were locked so far.
								e.printStackTrace();
							} catch (InconsistencyException e){
								// Cannot happen, since transition names are never null 
								// and the case id is always positive.
								e.printStackTrace();
							} catch (ParameterException e) {
								// Cannot happen, since the preceding startNextCase() ensures,
								// that case data is generated for the actual case id.
								e.printStackTrace();
							} 
							notifyEntryListeners(entry);
						}
					}
				}
				try {
					traceFilterManager.applyFilters(trace);
				} catch (ParameterException e) {
					// Is only thrown if applyFilters() is called with a null-parameter.
					// This cannot happen, since trace is created before.
					e.printStackTrace();
				}
				trace.sort();
				notifyTraceListeners(trace);
				logWriter.writeTrace(trace);
				completeCase(nextCaseID);
			}
		} catch (PerspectiveException e) {
			//Cannot happen since TraceLogGenerator enforces trace perspective.
			e.printStackTrace();
		} catch (PNException e) {
			e.printStackTrace();
		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (ValueGenerationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param trace
	 * @param logEntry
	 * @param executionTime
	 * @throws LockingException If the time field of the given entry is locked.
	 */
	protected void addEntryToTrace(LogTrace trace, LogEntry logEntry, ExecutionTime executionTime) throws LockingException{
		logEntry.setTimestamp(new Date(executionTime.startTime));
		trace.addEntry(logEntry);
	}

	@Override
	public void filterMessage(String message) {
		simulationListenerSupport.fireSimulationMessage(message);
	}

	@Override
	public void filterSuccess(AbstractFilter filter) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void filterFailure(AbstractFilter filter) {
		// TODO Auto-generated method stub
	}
	
	

}