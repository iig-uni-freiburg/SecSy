package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.io.IOException;
import java.util.Date;

import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.log.EventType;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogPerspective;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator.ExecutionTime;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EventHandling;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;
import de.uni.freiburg.iig.telematik.sepia.exception.PNException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;


/**
 * This class overrides the abstract class {@link LogGenerator}.
 * It generates logs in a trace oriented manner.  
 * Every log entry contains a complete executions trace.
 * 
 * @author Thomas Stocker
 *
 */
public class TraceLogGenerator extends LogGenerator{
	
	private EventHandling eventHandling = EventHandling.END;
	
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
	
	public EventHandling getEventHandling() {
		return eventHandling;
	}

	public void setEventHandling(EventHandling eventHandling) {
		this.eventHandling = eventHandling;
	}

	@Override
	protected void simulateNet(SimulationRun simulationRun) throws SimulationException, IOException{
		EntryTransformerManager entryTransformerManager = null;
		TraceTransformerManager traceTransformerManager = null;
		try{
			entryTransformerManager = simulationRun.getEntryTransformerManager();
			entryTransformerManager.setSource(getLogEntryGenerator());
			traceTransformerManager = simulationRun.getTraceTransformerManager();
			traceTransformerManager.setSource(getLogEntryGenerator());
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
					traceTransformerManager.applyTransformers(trace);
				} catch (ParameterException e) {
					// Is only thrown if applyTransformers() is called with a null-parameter.
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
		
		switch(eventHandling){
		
		case START:
			logEntry.setTimestamp(new Date(executionTime.startTime));
			logEntry.setEventType(EventType.start);
			trace.addEntry(logEntry);
			break;
		case END:
			logEntry.setTimestamp(new Date(executionTime.endTime));
			logEntry.setEventType(EventType.complete);
			trace.addEntry(logEntry);
			break;
		case BOTH:
			String group = String.valueOf(logEntry.hashCode());
			logEntry.setEventType(EventType.start);
			logEntry.setTimestamp(new Date(executionTime.startTime));
			logEntry.setGroup(group);
			trace.addEntry(logEntry);
			LogEntry completeEntry = logEntry.clone();
			completeEntry.setEventType(EventType.complete);
			completeEntry.setTimestamp(new Date(executionTime.endTime));
			completeEntry.setGroup(group);
			trace.addEntry(completeEntry);
		}
	}

	@Override
	public void transformerMessage(String message) {
		simulationListenerSupport.fireSimulationMessage(message);
	}

	@Override
	public void transformerSuccess(AbstractTransformer transformer) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void ransformerFailure(AbstractTransformer transformer) {
		// TODO Auto-generated method stub
	}
	
	public enum TimestampInterpretation {
		ACTIVITY_START, ACTIVITY_END;
	}

}
