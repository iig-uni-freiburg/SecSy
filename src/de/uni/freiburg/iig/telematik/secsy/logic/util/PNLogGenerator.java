package de.uni.freiburg.iig.telematik.secsy.logic.util;

import java.util.ArrayList;
import java.util.List;

import de.invation.code.toval.debug.Debug;
import de.invation.code.toval.time.TimeScale;
import de.invation.code.toval.time.TimeValue;
import de.uni.freiburg.iig.telematik.sepia.graphic.AbstractGraphicalPN;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsing;
import de.uni.freiburg.iig.telematik.sepia.parser.PNParsingFormat;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractFlowRelation;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractMarking;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractPlace;
import de.uni.freiburg.iig.telematik.sepia.petrinet.abstr.AbstractTransition;
import de.uni.freiburg.iig.telematik.sewol.format.LogFormatType;
import de.uni.freiburg.iig.telematik.sewol.format.LogPerspective;
import de.uni.freiburg.iig.telematik.sewol.format.MXMLLogFormat;
import de.uni.freiburg.iig.telematik.sewol.format.PlainTraceLogFormat;
import de.uni.freiburg.iig.telematik.sewol.format.XESLogFormat;
import de.uni.freiburg.iig.telematik.sewol.log.LogEntry;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;
import de.uni.freiburg.iig.telematik.sewol.writer.LogWriter;

public class PNLogGenerator {
	
	private static final String doneformat = "done [%s]";

	public static <P extends AbstractPlace<F,S>, 
	   			   T extends AbstractTransition<F,S>, 
	   			   F extends AbstractFlowRelation<P,T,S>, 
	   			   M extends AbstractMarking<S>, 
	   			   S extends Object>
	
	TraceGenerationResult generateLog(AbstractPetriNet<P,T,F,M,S> net, int numTraces, Integer maxEventsPerTrace, boolean useLabelNames, String logPath, String logName, LogFormatType... logFormatTypes) throws Exception{
		Debug.message("Generating Log... ");
		List<LogWriter> logWriters = new ArrayList<LogWriter>();
		for(LogFormatType formatType: logFormatTypes){
			switch(formatType){
			case MXML:
				logWriters.add(new LogWriter(new MXMLLogFormat(net.getName()), logPath, logName));
				break;
			case PLAIN:
				logWriters.add(new LogWriter(new PlainTraceLogFormat(LogPerspective.TRACE_PERSPECTIVE), logPath, logName));
				break;
			case XES:
				logWriters.add(new LogWriter(new XESLogFormat(logName)));
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
