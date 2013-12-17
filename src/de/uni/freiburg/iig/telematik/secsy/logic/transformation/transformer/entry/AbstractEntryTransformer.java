package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.entry;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;


public abstract class AbstractEntryTransformer extends AbstractTransformer {
	
	public AbstractEntryTransformer(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
	}

	public AbstractEntryTransformer(double activationProbability) throws ParameterException{
		super(activationProbability);
	}
	
	public EntryTransformerResult transformLogEntry(EntryTransformerEvent event) throws ParameterException{
		Validate.notNull(event);
		if(activationProbability==1.0 || rand.nextDouble()<=activationProbability){
			return applyTransformation(event);
		}
		try {
			return new EntryTransformerResult(event.logEntry, event.caseNumber, false);
		} catch (ParameterException e) {
			// Cannot happen, since EntryTransformerManager enforces non-null values for 
			// log entries and case numbers.
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Subclasses are responsible to check parameter validity.
	 * @param event
	 * @return
	 */
	protected abstract EntryTransformerResult applyTransformation(EntryTransformerEvent event) throws ParameterException;

}
