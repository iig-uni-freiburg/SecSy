package logic.simulation;

import java.io.IOException;

import logformat.LogFormat;
import logic.generator.AttributeValueGenerator;
import logic.generator.CaseDataContainer;
import logic.generator.Context;
import logic.generator.DetailedLogEntryGenerator;
import logic.generator.LogEntryGenerator;
import logic.generator.TraceLogGenerator;
import logic.generator.TraceLogGeneratorStartComplete;
import logic.generator.time.CaseTimeGenerator;
import validate.ParameterException;
import writer.PerspectiveException;

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
