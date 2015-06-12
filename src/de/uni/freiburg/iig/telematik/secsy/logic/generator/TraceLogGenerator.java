package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.io.IOException;
import java.util.Date;

import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.validate.InconsistencyException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator.ExecutionTime;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.SimulationRun;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EventHandling;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerManager;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;
import de.uni.freiburg.iig.telematik.sepia.exception.PNException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractTransition;
import de.uni.freiburg.iig.telematik.sewol.format.AbstractLogFormat;
import de.uni.freiburg.iig.telematik.sewol.format.LogPerspective;
import de.uni.freiburg.iig.telematik.sewol.log.EventType;
import de.uni.freiburg.iig.telematik.sewol.log.LockingException;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.writer.PerspectiveException;


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
	
	public TraceLogGenerator(AbstractLogFormat logFormat) 
			throws IOException, PerspectiveException {
		super(logFormat, LogPerspective.TRACE_PERSPECTIVE);
	}
	
	public TraceLogGenerator(AbstractLogFormat logFormat, String fileName) 
			throws IOException, PerspectiveException {
		super(logFormat, LogPerspective.TRACE_PERSPECTIVE, fileName);
	}
	
	public TraceLogGenerator(AbstractLogFormat logFormat, String logPath, String fileName) 
			throws IOException, PerspectiveException {
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
		
		entryTransformerManager = simulationRun.getEntryTransformerManager();
		entryTransformerManager.setSource(getLogEntryGenerator());
		traceTransformerManager = simulationRun.getTraceTransformerManager();
		traceTransformerManager.setSource(getLogEntryGenerator());

		caseGenerator.setPetriNet(simulationRun.getPetriNet(), simulationRun.getPNTraverser());
		if (getLogEntryGenerator() instanceof DetailedLogEntryGenerator) {
			DetailedLogEntryGenerator entryGenerator = (DetailedLogEntryGenerator) getLogEntryGenerator();
			caseGenerator.setContext(entryGenerator.getContext());
			caseGenerator.setCaseDataContainer(entryGenerator.getCaseDataContainer());
		}
			
		try {
			while(!simulationRun.isDone()){
				int nextCaseID = startNextCase();
				LogTrace<SimulationLogEntry> trace = new LogTrace<SimulationLogEntry>(nextCaseID);
				caseGenerator.newCase(nextCaseID);
				while(!caseGenerator.isCaseCompleted()){
					AbstractTransition<?,?> nextTransition = caseGenerator.getNextTransition();
					AbstractTransition<?,?> next = simulationRun.getPetriNet().fire(nextTransition.getName());
					if (next!=null && !next.isSilent()){
						SimulationLogEntry entry = getLogEntryGenerator().getLogEntryFor(next,nextCaseID);
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
							}
							notifyEntryListeners(entry);
						}
					}
				}
				traceTransformerManager.applyTransformers(trace);
				trace.sort();
				notifyTraceListeners(trace);
				logWriter.writeTrace(trace);
				completeCase(nextCaseID);
			}
		} catch (PerspectiveException e) {
			//Cannot happen since TraceLogGenerator enforces trace perspective.
			throw new SimulationException("Cannot write generated log trace [perspective exception].<br>Reason: " + e.getMessage());
		} catch (PNException e) {
			throw new SimulationException("Exception during Petri net execution.<br>Reason: " + e.getMessage());
		} catch (ValueGenerationException e) {
			throw new SimulationException("Exception during attribute value generation.<br>Reason: " + e.getMessage());
		}
	}
	
	/**
	 * 
	 * @param trace
	 * @param logEntry
	 * @param executionTime
	 * @throws LockingException If the time field of the given entry is locked.
	 * @throws ParameterException 
	 */
	protected void addEntryToTrace(LogTrace<SimulationLogEntry> trace, SimulationLogEntry logEntry, ExecutionTime executionTime) throws LockingException{
		
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
			SimulationLogEntry completeEntry = logEntry.clone();
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
