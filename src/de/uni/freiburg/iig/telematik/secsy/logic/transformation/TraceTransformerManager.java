package de.uni.freiburg.iig.telematik.secsy.logic.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.invation.code.toval.misc.CollectionUtils;
import de.invation.code.toval.misc.FormatUtils;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.exception.MissingRequirementException;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractTraceTransformer;
import de.uni.freiburg.iig.telematik.sewol.log.EntryField;
import de.uni.freiburg.iig.telematik.sewol.log.LogTrace;


public class TraceTransformerManager {
	
	private static final String transformerSummaryFormat = "------- Transformer: %s\n" +
													  "Triggered in %s traces (%s%%)\n" +
													  "  successful: %s:\n%s\n" +
													  "unsuccessful: %s:\n%s\n";
	
	protected List<AbstractTraceTransformer> traceTransformers = new ArrayList<AbstractTraceTransformer>();
	protected Map<AbstractTraceTransformer, List<Integer>> succesfulAppliances = new HashMap<AbstractTraceTransformer, List<Integer>>();
	protected Map<AbstractTraceTransformer, List<Integer>> unsuccesfulAppliances = new HashMap<AbstractTraceTransformer, List<Integer>>();
	
	protected LogEntryGenerator source;
	private Integer traces = 0;
	
	private TransformerListenerSupport transformerListenerSupport = new TransformerListenerSupport();
	
	public void registerTransformerListener(TransformerListener listener){
		transformerListenerSupport.addTransformerListener(listener);
	}
	
	public void removeTransformerListener(TransformerListener listener){
		transformerListenerSupport.removeTransformerListener(listener);
	}
	
	public void setSource(LogEntryGenerator source) throws MissingRequirementException {
		Validate.notNull(source);
		for (AbstractTraceTransformer transformer : traceTransformers) {
			for (EntryField contextType : transformer.requiredEntryFields()) {
				if (!source.providesLogInformation(contextType)) {
					throw new MissingRequirementException(contextType);
				}
			}
		}
		this.source = source;
	}
	
	public List<AbstractTraceTransformer> getTraceTransformers(){
		return Collections.unmodifiableList(traceTransformers);
	}
	
	public boolean isEmpty(){
		return traceTransformers.isEmpty();
	}
	
	/**
	 * Adds the given transformers to the set of managed transformers.
	 * @param traceTransformer Set of transformers.
	 * @throws MissingRequirementException 
	 * @throws ParameterException 
	 */
	public void addTransformer(AbstractTraceTransformer traceTransformer) throws MissingRequirementException{
		Validate.notNull(traceTransformer);
		if (source != null) {
			for (EntryField contextType : traceTransformer.requiredEntryFields()) {
				if (!source.providesLogInformation(contextType)) {
					throw new MissingRequirementException(contextType);
				}
			}
		}
		traceTransformers.add(traceTransformer);
	}
	
	public void applyTransformers(LogTrace<SimulationLogEntry> logTrace){
		if(!traceTransformers.isEmpty()){
			TraceTransformerEvent event = new TraceTransformerEvent(logTrace, source);
			for(AbstractTraceTransformer traceTransformer: traceTransformers){
				AbstractTransformerResult transformerResult = traceTransformer.transformLogTrace(event);
				if(transformerResult.wasTransformerApplied()){
					if(transformerResult.containsMessages()){
						transformerListenerSupport.fireTransformerMessage(transformerResult.getTransformerMessages()+"\n\n");
					}
				
					if(transformerResult.isSuccess()){
						incSuccessfulAppliances(traceTransformer, logTrace.getCaseNumber());
						transformerListenerSupport.fireTransformerSuccess(traceTransformer);
					} else {
						incUnsuccessfulAppliances(traceTransformer, logTrace.getCaseNumber());
						transformerListenerSupport.fireTransformerFailure(traceTransformer);
					}
				} else {
					//Transformer not applied
				}
			}
		}
		traces++;
	}
	
	private void incSuccessfulAppliances(AbstractTraceTransformer transformer, Integer caseID){
		if(!succesfulAppliances.containsKey(transformer)){
			succesfulAppliances.put(transformer, new ArrayList<Integer>());
		}
		succesfulAppliances.get(transformer).add(caseID);
	}
	
	private void incUnsuccessfulAppliances(AbstractTraceTransformer transformer, Integer caseID){
		if(!unsuccesfulAppliances.containsKey(transformer)){
			unsuccesfulAppliances.put(transformer, new ArrayList<Integer>());
		}
		unsuccesfulAppliances.get(transformer).add(caseID);
	}
	
	private Integer getSuccessfulAppliances(AbstractTraceTransformer transformer){
		if(!succesfulAppliances.containsKey(transformer)){
			return 0;
		}
		return  succesfulAppliances.get(transformer).size();
	}
	
	private Integer getUnsuccessfulAppliances(AbstractTraceTransformer transformer){
		if(!unsuccesfulAppliances.containsKey(transformer)){
			return 0;
		}
		return  unsuccesfulAppliances.get(transformer).size();
	}
	
	private Integer getAppliances(AbstractTraceTransformer transformer){
		return getSuccessfulAppliances(transformer) + getUnsuccessfulAppliances(transformer);
	}
	
	public String getTransformerSummary(){
		StringBuilder builder = new StringBuilder();
		builder.append("Transformer Summary:\n\n");
		for(AbstractTraceTransformer transformer: getTraceTransformers()){
			builder.append(getTransformerSummary(transformer));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	private String getTransformerSummary(AbstractTraceTransformer transformer){
		return String.format(transformerSummaryFormat, transformer.getName(),
												  getAppliances(transformer), FormatUtils.format((getAppliances(transformer)/traces.doubleValue())*100.0, 2),
												  getSuccessfulAppliances(transformer), succesfulAppliances.containsKey(transformer) ? CollectionUtils.toString(succesfulAppliances.get(transformer)) : "[]",
												  getUnsuccessfulAppliances(transformer), unsuccesfulAppliances.containsKey(transformer) ? CollectionUtils.toString(unsuccesfulAppliances.get(transformer)) : "[]");
	}
	
	
	public void reset(){
		traces = 0;
		succesfulAppliances.clear();
		unsuccesfulAppliances.clear();
	}
	
	public void clear(){
		traceTransformers.clear();
	}

}
