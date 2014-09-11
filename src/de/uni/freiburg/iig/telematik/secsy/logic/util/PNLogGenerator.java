package de.uni.freiburg.iig.telematik.secsy.logic.util;

import java.util.ArrayList;
import java.util.List;

import de.invation.code.toval.debug.Debug;
import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.uni.freiburg.iig.telematik.jawl.format.LogFormatType;
import de.uni.freiburg.iig.telematik.jawl.format.LogPerspective;
import de.uni.freiburg.iig.telematik.jawl.format.MXMLLogFormat;
import de.uni.freiburg.iig.telematik.jawl.format.PlainTraceLogFormat;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.jawl.writer.LogWriter;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.mg.abstr.AbstractMarkingGraphRelation;
import de.uni.freiburg.iig.telematik.sepia.mg.abstr.AbstractMarkingGraphState;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsingFormat;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractFlowRelation;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractMarking;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPlace;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;

public class PNLogGenerator {
	
	private static final String doneformat = "done [%s]";

	public static <P extends AbstractPlace<F,S>, 
	   			   T extends AbstractTransition<F,S>, 
	   			   F extends AbstractFlowRelation<P,T,S>, 
	   			   M extends AbstractMarking<S>, 
	   			   S extends Object,
	   			   X extends AbstractMarkingGraphState<M,S>,
	   			   Y extends AbstractMarkingGraphRelation<M,X,S>>
	
	TraceGenerationResult generateLog(AbstractPetriNet<P,T,F,M,S,X,Y> net, int numTraces, Integer maxEventsPerTrace, boolean useLabelNames, String logPath, String logName, LogFormatType... logFormatTypes) throws Exception{
		Debug.message("Generating Log... ");
		List<LogWriter> logWriters = new ArrayList<LogWriter>();
		for(LogFormatType formatType: logFormatTypes){
			switch(formatType){
			case MXML:
				logWriters.add(new LogWriter(new MXMLLogFormat(), logPath, logName));
				break;
			case PLAIN:
				logWriters.add(new LogWriter(new PlainTraceLogFormat(LogPerspective.TRACE_PERSPECTIVE), logPath, logName));
				break;
			}
		}
		long start = System.currentTimeMillis();
		TraceGenerationResult generationResult = PNTraceGenerator.generateTraces(net, numTraces, maxEventsPerTrace, useLabelNames);
		List<LogTrace<LogEntry>> traces = generationResult.traces;
		for(LogTrace<LogEntry> trace: traces){
			for(LogWriter writer: logWriters){
				writer.writeTrace(trace);
			}
		}
		for(LogWriter writer: logWriters){
			writer.closeFile();
		}
		TimeValue runtime = new TimeValue(System.currentTimeMillis() - start, TimeScale.MILLISECONDS);
		runtime.adjustScale();
		Debug.message(String.format(doneformat, runtime));
		return generationResult;
	}
	
	public static void main(String[] args) throws Exception {
		AbstractGraphicalPN net = PNParsing.parse("/Users/stocker/Desktop/PNPNPN.pnml", PNParsingFormat.PNML);
		PNLogGenerator.generateLog(net.getPetriNet(), 300, 75, true, "/Users/stocker/Desktop/", "PNMLSAMPLE", LogFormatType.PLAIN);
	}

}
