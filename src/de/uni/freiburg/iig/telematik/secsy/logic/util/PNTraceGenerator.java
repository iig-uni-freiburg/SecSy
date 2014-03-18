package de.uni.freiburg.iig.telematik.secsy.logic.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.sepia.exception.PNException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractFlowRelation;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractMarking;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPetriNet;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractPlace;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;
import de.uni.freiburg.iig.telematik.sepia.traversal.PNTraverser;
import de.uni.freiburg.iig.telematik.sepia.traversal.RandomPNTraverser;

public class PNTraceGenerator {

	public static <P extends AbstractPlace<F,S>, 
	   T extends AbstractTransition<F,S>, 
	   F extends AbstractFlowRelation<P,T,S>, 
	   M extends AbstractMarking<S>, 
	   S extends Object> 

	   TraceGenerationResult generateTraces(AbstractPetriNet<P,T,F,M,S> net, int numTraces, Integer maxEventsPerTrace, boolean useLabelNames) 
                   throws ParameterException{

		Validate.notNull(net);
		ArrayList<LogTrace<LogEntry>> traces = new ArrayList<LogTrace<LogEntry>>(numTraces);
		PNTraverser<T> traverser = new RandomPNTraverser<T>(net);
		Set<List<String>> activitySequences = new HashSet<List<String>>();
		int distinctActivitySequences = 0;
		for (int i = 0; i < numTraces; i++) {
			LogTrace<LogEntry> newTrace = new LogTrace<LogEntry>(i+1);
			List<String> newActivitySequence = new ArrayList<String>();
			net.reset();
			int c = 0;
			while (net.hasEnabledTransitions() && (maxEventsPerTrace == null || c++ < maxEventsPerTrace)) {
				T nextTransition = traverser.chooseNextTransition(net.getEnabledTransitions());
				String descriptor = useLabelNames ? nextTransition.getLabel() : nextTransition.getName();
				if (!nextTransition.isSilent()) {
					newTrace.addEntry(new LogEntry(descriptor));
					newActivitySequence.add(descriptor);
				}
				try {
					net.fire(nextTransition.getName());
				} catch (PNException e) {
					e.printStackTrace();
				}
			}
			traces.add(newTrace);
			if(activitySequences.add(newActivitySequence)){
				distinctActivitySequences++;
			}
		}
		return new TraceGenerationResult(traces, distinctActivitySequences);
	}

}
