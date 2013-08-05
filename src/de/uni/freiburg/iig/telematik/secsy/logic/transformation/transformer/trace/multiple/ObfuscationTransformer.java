package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.multiple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.misc.SetUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.LogEntryGenerator;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.TransformerType;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.ObfuscationTransformerProperties;


public class ObfuscationTransformer extends AbstractMultipleTraceTransformer {
	
	private final String CUSTOM_SUCCESS_FORMAT = "entry %s, field %s: %s -> %s";
	LogEntryGenerator entryGenerator = null;
	Set<EntryField> excludedFields = new HashSet<EntryField>();
	
	public ObfuscationTransformer(ObfuscationTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		excludedFields = properties.getExcludedFields();
	}

	public ObfuscationTransformer(double activationProbability, int maxAppliance) throws ParameterException {
		super(TransformerType.OBFUSCATION, activationProbability, maxAppliance);
		excludedFields.addAll(Arrays.asList(ObfuscationTransformerProperties.defaultExcludedFields));
	}
	
	public ObfuscationTransformer(double activationProbability, int maxAppliance, EntryField... excludedFields) throws ParameterException {
		this(activationProbability, maxAppliance, new HashSet<EntryField>(Arrays.asList(excludedFields)));
	}
	
	public ObfuscationTransformer(double activationProbability, int maxAppliance, Set<EntryField> excludedFields) throws ParameterException {
		this(activationProbability, maxAppliance);
		setExcludedFields(excludedFields);
	}
	
	public Set<EntryField> getExcludedFields(){
		return Collections.unmodifiableSet(excludedFields);
	}
	
	public void setExcludedFields(Set<EntryField> excludedFields) throws ParameterException{
		Validate.notNull(excludedFields);
		Validate.noNullElements(excludedFields);
		this.excludedFields.clear();
		this.excludedFields.addAll(excludedFields);
	}

	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException {
		Validate.notNull(event);
		entryGenerator = (LogEntryGenerator) event.sender;
		return super.applyTransformation(event);
	}

	@Override
	protected boolean applyEntryTransformation(LogTrace trace, LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		super.applyEntryTransformation(trace, entry, transformerResult);
		//Find all fields that can be obfuscated
		List<EntryField> possibleFields = new ArrayList<EntryField>();
		possibleFields.remove(EntryField.ORIGINATOR_CANDIDATES);
		for(EntryField field: EntryField.values()){
			if(!entry.isFieldLocked(field) && entryGenerator.providesLogInformation(field))
				possibleFields.add(field);
		}
		possibleFields.removeAll(excludedFields);
		if(possibleFields.isEmpty())
			return false;
		
		//Try to apply an obfuscation
		Obfuscation obfuscation;
		String activityName = entry.getActivity();
		Collections.shuffle(possibleFields);
		for(EntryField field: possibleFields){
			obfuscation = applyObfuscation(field, entry);
			if(obfuscation.isSuccess()){
				addMessageToResult(getSuccessMessage(activityName, field, obfuscation.getOldValue(), obfuscation.getNewValue()), transformerResult);
				entry.lockField(field, "Transformer-Enforcement: Obfuscation");
				return true;
			}
		}
		return false;
	}
	
	protected Obfuscation applyObfuscation(EntryField field, LogEntry entry) throws ParameterException{
		Validate.notNull(field);
		Validate.notNull(entry);
		Obfuscation result = new Obfuscation(field, entry.getFieldValue(field));
		if(appliancePossible(field, entry) && setNewValueFor(field, entry))
			result.setNewValue(entry.getFieldValue(field));
		return result;
		
	}
	
	protected boolean appliancePossible(EntryField field, LogEntry entry) throws ParameterException{
		Validate.notNull(field);
		Validate.notNull(entry);
		switch (field) {
		case ACTIVITY:
			return entry.getActivity() != null;
		case ORIGINATOR:
			return entry.getOriginator() != null;
		case EVENTTYPE:
			return entry.getEventType() != null;
		case DATA:
			return !entry.getDataAttributes().isEmpty();
		default:
			return false;
		}
	}
	
	protected boolean setNewValueFor(EntryField field, LogEntry entry) throws ParameterException{
		Validate.notNull(field);
		Validate.notNull(entry);
		try{
			switch (field) {
			case ACTIVITY:
				entry.setActivity(null);
				return true;
			case ORIGINATOR:
				entry.removeAllOriginatorCandidates();
				return true;
			case EVENTTYPE:
				entry.setEventType(null);
				return true;
			case DATA:
				entry.removeDataAttribute(SetUtils.getRandomElement(entry.getDataAttributes()));
				return true;
			default:
				return false;
			}
		} catch (LockingException e) {
			return false;
		}
	}
	
	private String getSuccessMessage(String activity, EntryField field, Object oldValue, Object newValue){
		return getNoticeMessage(String.format(CUSTOM_SUCCESS_FORMAT, activity, field, oldValue, newValue));
	}
	
	@Override
	public List<EntryField> requiredContextInformation() {
		return Arrays.asList();
	}
	
	protected class Obfuscation{
		private EntryField field;
		private Object oldValue;
		private Object newValue;
		private boolean success;
		
		public Obfuscation(EntryField field, Object oldValue, Object newValue) throws ParameterException {
			Validate.notNull(field);
			this.field = field;
			this.oldValue = oldValue;
			setNewValue(newValue);
		}
		
		public Obfuscation(EntryField field, Object oldValue) throws ParameterException {
			Validate.notNull(field);
			this.field = field;
			this.oldValue = oldValue;
			this.newValue = oldValue;
			success = false;
		}
		
		public EntryField getField() {
			return field;
		}

		public Object getOldValue() {
			return oldValue;
		}

		public Object getNewValue() {
			return newValue;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setNewValue(Object newValue) {
			this.newValue = newValue;
			success = !oldValue.equals(newValue);
			if(oldValue instanceof Collection && newValue instanceof Collection){
				Collection<?> oldColl = (Collection<?>) oldValue;
				Collection<?> newColl = (Collection<?>) newValue;
				success = !oldColl.containsAll(newColl) || !newColl.containsAll(oldColl);
			}
		}
		
	}

	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((ObfuscationTransformerProperties) properties).setExcludedFields(excludedFields);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		ObfuscationTransformerProperties properties = new ObfuscationTransformerProperties();
		fillProperties(properties);
		return properties;
	}
	

}
