package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;

/**
 * abstract class for transformers that apply to whole log traces instead of single log entries.
 * This is particularly helpful in cases where properties must be ensured/violated that apply to a set of entries.<br>
 * <br>
 * The class ensures that the transformer appliances respect the activation probability.
 * @author Thomas Stocker
 */
@SuppressWarnings("serial")
public abstract class AbstractTraceTransformer extends AbstractTransformer {
	
	public AbstractTraceTransformer(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}

	/**
	 * Creates a new TraceTransformer according to the given parameters.
	 * @param activationProbability Probability for applying the transformer.<br>
	 * If the probability is 1, the transformer is always applied.
	 * @throws ParameterException 
	 */
	public AbstractTraceTransformer(Double activationProbability) throws ParameterException{
		super(activationProbability);
	}
	
	/**
	 * Creates a new TraceTransformer with default properties.
	 */
	public AbstractTraceTransformer(){
		super();
	}
	
	/**
	 * Applies the transformer to a log trace.<br>
	 * It checks if the activation probability allows the transformer to be applied
	 * (either it is 1 or a newly generated random number is smaller or equal than the activation probability).
	 * In case of an appliance it calls a subroutine for the transformation itself.
	 * @param event Contains the LogTrace as such together with information on the caller of the transformer routine (e.g. a LogEntryGenerator).
	 * @return Information on the success of the transformer appliance together with transformer messages.
	 * @throws ParameterException 
	 * @see AbstractTraceTransformer#applyTransformation(TraceTransformerEvent);
	 */
	public TraceTransformerResult transformLogTrace(TraceTransformerEvent event) throws ParameterException{
		Validate.notNull(event);
		if(activationProbability==1.0 || rand.nextDouble()<=activationProbability){
			return applyTransformation(event);
		}
		try {
			return new TraceTransformerResult(event.logTrace, false);
		} catch (ParameterException e) {
			// Cannot happen, since TraceTransformerEvent enforces non-null values for log traces.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Transforms a trace according to the aim of the trace transformer.
	 * @param event Contains the LogTrace as such together with information on the caller of the transformer routine (e.g. a LogEntryGenerator).
	 * @return Information on the success of the transformer appliance together with transformer messages.
	 */
	protected abstract TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException;
	
	/**
	 * When a transformer is mandatory, it is applied on all traces.<br>
	 * This means, that the activation probability parameter is neglected.
	 * @return <code>true</code> if the transformer is mandatory;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean isMandatory(){
		return false;
	}

}
