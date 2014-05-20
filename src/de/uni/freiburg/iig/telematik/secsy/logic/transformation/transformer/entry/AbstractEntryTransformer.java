package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.entry;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.EntryTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.AbstractTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;


public abstract class AbstractEntryTransformer extends AbstractTransformer {
	
	private static final long serialVersionUID = 8034453922971088702L;

	public AbstractEntryTransformer(AbstractTransformerProperties properties) throws PropertyException {
		super(properties);
	}

	public AbstractEntryTransformer(double activationProbability){
		super(activationProbability);
	}
	
	public EntryTransformerResult transformLogEntry(EntryTransformerEvent event){
		Validate.notNull(event);
		if(getActivationProbability()==1.0 || rand.nextDouble()<=getActivationProbability()){
			return applyTransformation(event);
		}
		return new EntryTransformerResult(event.logEntry, event.caseNumber, false);
	}
	
	/**
	 * Subclasses are responsible to check parameter validity.
	 * @param event
	 * @return
	 */
	protected abstract EntryTransformerResult applyTransformation(EntryTransformerEvent event);

}
