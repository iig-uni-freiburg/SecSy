package logic.transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.invation.code.toval.misc.CollectionUtils;
import de.invation.code.toval.misc.FormatUtils;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;

import logic.generator.LogEntryGenerator;
import logic.transformation.transformer.exception.MissingRequirementException;
import logic.transformation.transformer.trace.AbstractTraceTransformer;

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
	
	public void registerFilterListener(TransformerListener listener) throws ParameterException{
		transformerListenerSupport.addTransformerListener(listener);
	}
	
	public void removeFilterListener(TransformerListener listener){
		transformerListenerSupport.removeTransformerListener(listener);
	}
	
	public void setSource(LogEntryGenerator source) throws MissingRequirementException, ParameterException {
		Validate.notNull(source);
		try{
			for (AbstractTraceTransformer transformer : traceTransformers) {
				for (EntryField contextType : transformer.requiredContextInformation()) {
					if (!source.providesLogInformation(contextType))
						throw new MissingRequirementException(contextType);
				}
			}
		} catch(ParameterException e){
			// Cannot happen, since filters are required to never return null-values
			// in the method filter.requiredContextInformation().
			e.printStackTrace();
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
	public void addFilter(AbstractTraceTransformer traceTransformer) throws MissingRequirementException, ParameterException{
		Validate.notNull(traceTransformer);
		try{
			if(source!=null)
				for(EntryField contextType: traceTransformer.requiredContextInformation())
					if(!source.providesLogInformation(contextType))
						throw new MissingRequirementException(contextType);
		} catch(ParameterException e){
			// Cannot happen, since filters are required to never return null-values
			// in the method filter.requiredContextInformation().
			e.printStackTrace();
		}
		traceTransformers.add(traceTransformer);
	}
	
	public void applyTransformers(LogTrace logTrace) throws ParameterException{
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
					//Filter not applied
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
		builder.append("Filter Summary:\n\n");
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
