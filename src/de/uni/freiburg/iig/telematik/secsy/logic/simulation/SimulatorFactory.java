package de.uni.freiburg.iig.telematik.secsy.logic.simulation;

import java.io.IOException;

import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.jawl.logformat.LogFormat;
import de.uni.freiburg.iig.telematik.jawl.writer.PerspectiveException;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.AttributeValueGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.CaseDataContainer;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.DetailedLogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.properties.EventHandling;


public class SimulatorFactory {
	
	public static Simulation createTraceSimulator(EventHandling eventHandling,
												 LogFormat logFormat,
												 long startTime,
												 int casesPerDay) 
			throws ParameterException, IOException, PerspectiveException, ConfigurationException{
		
		TraceLogGenerator logGenerator =  new TraceLogGenerator(logFormat);
		logGenerator.setEventHandling(eventHandling);
		LogEntryGenerator entryGenerator = new LogEntryGenerator();
		CaseTimeGenerator timeGenerator = new CaseTimeGenerator(startTime, casesPerDay);
		return new Simulation(logGenerator, entryGenerator, timeGenerator);
	}
	
	public static Simulation createTraceSimulator(EventHandling eventHandling,
												 Context context,
			 									 LogFormat logFormat,
			 									 long startTime,
			 									 int casesPerDay) 
			throws ParameterException, IOException, PerspectiveException, ConfigurationException{

		AttributeValueGenerator valueGenerator = new AttributeValueGenerator();
		CaseDataContainer caseDataContainer = new CaseDataContainer(context, valueGenerator);
		return createTraceSimulator(eventHandling, context, caseDataContainer, logFormat, startTime, casesPerDay);
	}
	
	public static Simulation createTraceSimulator(EventHandling eventHandling, 
												 Context context,
												 CaseDataContainer caseDataContainer,
												 LogFormat logFormat, 
												 long startTime, 
												 int casesPerDay)
			throws ParameterException, IOException, PerspectiveException,
			ConfigurationException {

		TraceLogGenerator logGenerator =  new TraceLogGenerator(logFormat);
		logGenerator.setEventHandling(eventHandling);
		DetailedLogEntryGenerator entryGenerator = new DetailedLogEntryGenerator(context, caseDataContainer);
		CaseTimeGenerator timeGenerator = new CaseTimeGenerator(startTime, casesPerDay);
		return new Simulation(logGenerator, entryGenerator, timeGenerator);
	}

	public enum EntryGeneratorType {
		STANDARD, DETAILED;
	}
	
	public enum TimeGeneratorType {
		STANDARD, ADJUSTABLE, RANDOM;
	}
	
	public static void main(String[] args) throws Exception{
//		Simulator simulator = SimulatorFactory.createTraceSimulator(TraceLogGeneratorType.TRACE_GENERATOR, LogFormatFactory.MXML(), startTime, casesPerDay)
	}
}
