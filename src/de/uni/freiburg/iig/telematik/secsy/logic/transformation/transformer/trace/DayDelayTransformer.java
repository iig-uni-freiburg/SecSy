package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.Arrays;
import java.util.List;

import de.invation.code.toval.misc.RandomUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LockingException;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.log.SimulationLogEntry;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerEvent;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.PropertyAwareTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.DayDelayTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractMultipleTraceTransformer;


public class DayDelayTransformer extends AbstractMultipleTraceTransformer implements PropertyAwareTransformer{
	
	private static final long serialVersionUID = -7163494962328013091L;

	private final String CUSTOM_SUCCESS_FORMAT = "entry %s: added delay of %s days";
	
	public static final String hint = "<p>A day delay transformer adds an extra delay of several days" +
									  "between events of a process trace. transformer parameterization" +
									  "allows to specify min and max values for added days." +
									  "The number of appliances per trace is randomly chosen" +
									  "with an adjustable upper bound.</p>";
	
	private int minDays;
	private int maxDays;

	public DayDelayTransformer(DayDelayTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		setDayBounds(properties.getMinDays(), properties.getMaxDays());
	}
	
	public DayDelayTransformer(Double activationProbability, Integer maxAppliances) throws ParameterException {
		super(activationProbability, maxAppliances);
	}
	
	public DayDelayTransformer() {
		super();
	}

	/**
	 * Sets the transformer-specific properties and requires the following values:<br>
	 * <ul>
	 * <li><code>Integer</code>: Min Days.<br>
	 * Specifies the minimum number of days which are added to an entry's timestamp .<br></li>
	 * <li><code>Integer</code>: Max Days.<br>
	 * Specifies the maximum number of days which are added to an entry's timestamp .<br></li>
	 * </ul>
	 * @see #setDayBounds(int, int)
	 */
	@Override
	public void setProperties(Object[] properties) throws Exception {
		Validate.notNull(properties);
		Validate.notEmpty(properties);
		if(properties.length != 2)
			throw new ParameterException("Wrong number of parameters. Expected 2, but got " + properties.length);
		Validate.noNullElements(properties);
		Validate.type(properties[0], Integer.class);
		Validate.type(properties[1], Integer.class);
		setDayBounds((Integer) properties[0], (Integer) properties[1]);
	}

	public void setDayBounds(int minDays, int maxDays) throws ParameterException{
		DayDelayTransformerProperties.validateDayBounds(minDays, maxDays);
		this.minDays = minDays;
		this.maxDays = maxDays;
	}
	
	public Integer getMinDays() {
		return minDays;
	}

	public Integer getMaxDays() {
		return maxDays;
	}
	
	@Override
	protected TraceTransformerResult applyTransformation(TraceTransformerEvent event) throws ParameterException {
		TraceTransformerResult result = super.applyTransformation(event);
		if(result.isSuccess()){
			for(LogEntry transformedEntry: transformedEntries){
				transformedEntry.lockField(EntryField.TIME, "Transformer-Enforcement: DayDelay");
			}
		}
		return result;
	}

	@Override
	protected boolean applyEntryTransformation(LogTrace<SimulationLogEntry> trace, SimulationLogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		
		// Check, if timestamps can be altered for the entry itself and all its successors within the trace
		if(entry.isFieldLocked(EntryField.TIME)){
			addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field"), transformerResult);
			return false;
		}
		for(SimulationLogEntry affectedEntry: transformerResult.getLogTrace().getSucceedingEntries(entry)){
			if(affectedEntry.isFieldLocked(EntryField.TIME)){
				addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due to locked time-field in sucessing entry ("+affectedEntry.getActivity()+")"), transformerResult);
				return false;
			}
		}
		
		try {
			int extraDays = RandomUtils.randomIntBetween(minDays, maxDays+1);
			long delayInMilliseconds = 86400000 * extraDays;
			if(entry.addTime(delayInMilliseconds)){
			
				// -> Adjust the start times of all following entries.
				// Since locking properties are checked before in applyEntryTransformation, there should not occur any errors
				for(LogEntry succeedingEntry: trace.getSucceedingEntries(entry)){
					succeedingEntry.addTime(delayInMilliseconds);
				}
				addMessageToResult(getSuccessMessage(entry.getActivity(), extraDays), transformerResult);
				
				return true;
			} else {
				// Should not happen, locking property is checked before
				addMessageToResult(super.getErrorMessage("entry " + entry.getActivity() + ": Cannot add delay due t olocked time-field"), transformerResult);
				return false;
			}
		} catch(LockingException e){
			//should not happen, since we checked field locking properties before.
			e.printStackTrace();
			return false;
		}
	}
	
	private String getSuccessMessage(String activityName, int extraDays){
		return getSuccessMessage(String.format(CUSTOM_SUCCESS_FORMAT, activityName, extraDays));
	}

	@Override
	public List<EntryField> requiredEntryFields() {
		return Arrays.asList(EntryField.TIME);
	}
	
	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((DayDelayTransformerProperties) properties).setDayBounds(getMinDays(), getMaxDays());
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		DayDelayTransformerProperties properties = new DayDelayTransformerProperties();
		fillProperties(properties);
		return properties;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public boolean requiresTimeGenerator() {
		return false;
	}

	@Override
	public boolean requiresContext() {
		return false;
	}

}
