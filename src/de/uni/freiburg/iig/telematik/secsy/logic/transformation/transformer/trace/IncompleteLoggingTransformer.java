package de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.jawl.log.EntryField;
import de.uni.freiburg.iig.telematik.jawl.log.LogEntry;
import de.uni.freiburg.iig.telematik.jawl.log.LogTrace;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.TraceTransformerResult;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.PropertyAwareTransformer;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.AbstractTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.IncompleteLoggingTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.properties.SkipActivitiesTransformerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.transformation.transformer.trace.abstr.AbstractMultipleTraceTransformer;


public class IncompleteLoggingTransformer extends AbstractMultipleTraceTransformer implements PropertyAwareTransformer{

	private static final long serialVersionUID = 2457776689250793415L;

	private final String CUSTOM_SUCCESS_FORMAT = "entry \"%s\" skipped";
	
	public static final String hint = "<html><p>An incomplete logging transformer removes single events" +
									  "from a process trace. In contrast to the skip activities" +
									  "transformer, timestamps of succeeding events are NOT adjusted." +
									  "transformer parameterization allows to specify a set of" +
									  "activities that may be skipped. The number of appliances" +
									  "per trace is randomly chosen with an adjustable upper bound.</p></html>";


	private Set<String> skipActivities = new HashSet<String>();
	
	public IncompleteLoggingTransformer(IncompleteLoggingTransformerProperties properties) throws ParameterException, PropertyException {
		super(properties);
		skipActivities = properties.getSkipActivities();
	}

	public IncompleteLoggingTransformer(Double activationProbability, Integer maxAppliances) throws ParameterException {
		super(activationProbability, maxAppliances);
	}
	
	public IncompleteLoggingTransformer() {
		super();
	}
	
	/**
	 * Sets the transformer-specific properties and requires the following values:<br>
	 * <ul>
	 * <li><code>Set&ltString&gt</code>: Skip Activities.<br>
	 * Set of activities for which skipping is allowed.<br></li>
	 * </ul>
	 * @see #setSkipActivities(Set)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setProperties(Object[] properties) throws Exception {
		Validate.notNull(properties);
		Validate.notEmpty(properties);
		if(properties.length != 1)
			throw new ParameterException("Wrong number of parameters. Expected 1, but got " + properties.length);
		Validate.noNullElements(properties);
		Validate.type(properties[0], Set.class);
		Set<String> skipActivities = null;
		try {
			skipActivities = (Set<String>) properties[0];
		} catch(Exception e){
			throw new ParameterException("Wrong parameter type: " + e.getMessage());
		}
		setSkipActivities(skipActivities);
	}
	
	public Set<String> getSkipActivities(){
		return Collections.unmodifiableSet(skipActivities);
	}
	
	public void setSkipActivities(Set<String> skipActivities) throws ParameterException{
		SkipActivitiesTransformerProperties.validateSkipActivities(skipActivities);
		this.skipActivities.clear();
		this.skipActivities.addAll(skipActivities);
	}
	
	@Override
	protected boolean applyEntryTransformation(LogTrace trace, LogEntry entry, TraceTransformerResult transformerResult) throws ParameterException {
		if(skipAllowed(entry.getActivity())){
			if(trace.removeAllEntries(trace.getEntriesForGroup(entry.getGroup())))
				addMessageToResult(getCustomSuccessMessage(entry.getActivity()), transformerResult);
			return true;
		}
		return false;
	}
	
	protected boolean skipAllowed(String activity) throws ParameterException{
		Validate.notNull(activity);
		return skipActivities.contains(activity);
	}
	
	
	protected String getCustomSuccessMessage(String activity) throws ParameterException{
		Validate.notNull(activity);
		return getNoticeMessage(String.format(CUSTOM_SUCCESS_FORMAT, activity));
	}
	
	@Override
	public List<EntryField> requiredEntryFields() {
		return Arrays.asList();
	}
	
	@Override
	protected void fillProperties(AbstractTransformerProperties properties) throws ParameterException, PropertyException {
		super.fillProperties(properties);
		((IncompleteLoggingTransformerProperties) properties).setSkipActivities(skipActivities);
	}

	@Override
	public AbstractTransformerProperties getProperties() throws ParameterException, PropertyException {
		IncompleteLoggingTransformerProperties properties = new IncompleteLoggingTransformerProperties();
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