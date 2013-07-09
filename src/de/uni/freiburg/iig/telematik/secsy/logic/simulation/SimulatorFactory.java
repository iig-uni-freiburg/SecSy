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
import de.uni.freiburg.iig.telematik.secsy.logic.generator.TraceLogGeneratorStartComplete;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.time.CaseTimeGenerator;


public class SimulatorFactory {
	
	public static Simulation createTraceSimulator(TraceLogGeneratorType logGeneratorType,
												 LogFormat logFormat,
												 long startTime,
												 int casesPerDay) 
			throws ParameterException, IOException, PerspectiveException, ConfigurationException{
		
		TraceLogGenerator logGenerator = null;
		switch(logGeneratorType){
			case TRACE_GENERATOR: logGenerator = new TraceLogGenerator(logFormat);
			break;
			case TRACE_GENERATOR_SC: logGenerator = new TraceLogGeneratorStartComplete(logFormat);
			break;
		}
		LogEntryGenerator entryGenerator = new LogEntryGenerator();
		CaseTimeGenerator timeGenerator = new CaseTimeGenerator(startTime, casesPerDay);
		return new Simulation(logGenerator, entryGenerator, timeGenerator);
	}
	
	public static Simulation createTraceSimulator(TraceLogGeneratorType logGeneratorType,
												 Context context,
			 									 LogFormat logFormat,
			 									 long startTime,
			 									 int casesPerDay) 
			throws ParameterException, IOException, PerspectiveException, ConfigurationException{

		AttributeValueGenerator valueGenerator = new AttributeValueGenerator();
		CaseDataContainer caseDataContainer = new CaseDataContainer(context, valueGenerator);
		return createTraceSimulator(logGeneratorType, context, caseDataContainer, logFormat, startTime, casesPerDay);
	}
	
	public static Simulation createTraceSimulator(TraceLogGeneratorType logGeneratorType, 
												 Context context,
												 CaseDataContainer caseDataContainer,
												 LogFormat logFormat, 
												 long startTime, 
												 int casesPerDay)
			throws ParameterException, IOException, PerspectiveException,
			ConfigurationException {

		TraceLogGenerator logGenerator = null;
		switch (logGeneratorType) {
		case TRACE_GENERATOR:
			logGenerator = new TraceLogGenerator(logFormat);
			break;
		case TRACE_GENERATOR_SC:
			logGenerator = new TraceLogGeneratorStartComplete(logFormat);
			break;
		}
		DetailedLogEntryGenerator entryGenerator = new DetailedLogEntryGenerator(context, caseDataContainer);
		CaseTimeGenerator timeGenerator = new CaseTimeGenerator(startTime, casesPerDay);
		return new Simulation(logGenerator, entryGenerator, timeGenerator);
	}

	
	public enum TraceLogGeneratorType {
		TRACE_GENERATOR, TRACE_GENERATOR_SC;
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