package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.constraint.NumberOperator;
import de.invation.code.toval.misc.valuegeneration.StochasticValueGenerator;
import de.invation.code.toval.misc.valuegeneration.ValueGenerationException;
import de.invation.code.toval.misc.valuegeneration.ValueGenerator;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.uni.freiburg.iig.telematik.jawl.log.DataAttribute;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.properties.CaseDataContainerProperties;
import de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException;
import de.uni.freiburg.iig.telematik.sepia.petrinet.ifnet.concepts.GuardDataContainer;

/**
 * This class stores values of data elements (attributes) for each execution case.<br>
 * This way it can be ensured that on log creation, the value of the same attribute<br>
 * stays the same during a specific case and no inconsistencies occur between activities
 * using the same attributes.<br>
 * It uses a log context ({@link Context}) to know which attributes are used by which activity<br>
 * and provides methods to gather data attributes for specific process activities.<br>
 * To allow subclasses to adjust the creation of new values for different data attributes,<br>
 * it provides the abstract method {@link #getNewValueFor(String)}.<br>
 * <br>
 * This class implements the interface {@link TraceCompletionListener}<br>
 * to erase information about case attributes which is no longer required.<br>
 * <br>
 * This class implements the interface {@link GuardDataContainer}<br>
 * to provide attribute value information for guard evaluation.<br>
 * The case number used for providing attribute values for guards<br>
 * is determined by the field {@link #actualGuardCase}.
 * 
 * @author Thomas Stocker
 */
public class CaseDataContainer implements TraceCompletionListener, GuardDataContainer{
	
	private static final String toStringFormat = "Data container name: %s\n\n" +
												 "Default value: %s\n%s";
	private static final String defaultValueFormat = "%s (%s)";
	private static final String valueGeneratorFormat = "Attribute \"%s\":\n%s\n";
	
	/**
	 * This map stores for every case a map containing the values for every attribute.
	 */
	protected HashMap<Integer, HashMap<String, Object>> caseAttributeValues = new HashMap<Integer, HashMap<String, Object>>();
	/**
	 * Generator for the generation of new values of data attributes within cases.
	 */
	protected AttributeValueGenerator attributeValueGenerator;
	/**
	 * Context that holds required information about the attributes used on activity execution.
	 */
	protected Context context = null;
	
	/**
	 * Indicates the case number used for providing attribute value information for guard evaluation.<br>
	 * This field is used to support the interface {@link GuardDataContainer}.
	 */
	protected int actualGuardCase = -1;
	
	protected String name = "CaseDataContainer";
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Creates a new case data container using the given context and attribute value generator.
	 * The log context holds required information about the attributes used on activity execution.
	 * The attribute value generator is required to generate new values for data elements used in activity executions.
	 * @param context Log context.
	 * @throws ParameterException if one of the following errors occur:<br>
	 * <ul>
	 * <li>Null pointer: One of the parameters is <code>null</code>.</li>
	 * <li>Incompatibility: The type of generated values for at least one attribute does not fit<br>
	 * to the parameter type of a related context constraint on this attribute.</li>
	 * </ul>
	 */
	public CaseDataContainer(AttributeValueGenerator attributeValueGenerator) throws ParameterException{
		setAttributeValueGenerator(attributeValueGenerator);
	}
	
	public CaseDataContainer(Context context, AttributeValueGenerator attributeValueGenerator) throws ParameterException{
		this(attributeValueGenerator);
		setContext(context);
	}
	
	public void setAttributeValueGenerator(AttributeValueGenerator attValueGenerator) throws ParameterException{
		Validate.notNull(attValueGenerator);
		this.attributeValueGenerator = attValueGenerator;
	}
	
	/**
	 * Sets the case number to be used for providing attribute value information for guard evaluation.
	 * @see #actualGuardCase
	 * @param caseNumber The number of the case where attribute values are of interest.
	 * @throws ParameterException If the given case number is smaller or equal 0.
	 */
	public void setActualGuardCase(int caseNumber) throws ParameterException{
		Validate.bigger(caseNumber, 0);
		this.actualGuardCase = caseNumber;
	}
	
	/**
	 * Returns the log context of the case data container
	 * which is used to gather information about the usage of data attributes
	 * on activity execution.
	 * @return The log context of the case data container.
	 */
	public Context getContext(){
		return context;
	}
	
	public void setContext(Context context) throws ParameterException{
		Validate.notNull(context);
		// Check if the value type of routing constraints is compatible with the type of generated values for attributes.
				for(String activity: context.getActivities()){
					if(context.hasRoutingConstraints(activity)){
						for(AbstractConstraint<?> constraint: context.getRoutingConstraints(activity)){
							Type t1 = constraint.getParameterType();
							Class t2 = attributeValueGenerator.getValueGenerator(constraint.getElement()).getValueClass();
							if(!((Class) t1).isAssignableFrom(t2)){
								throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Type of generated attribute values is in conflict with constraint type.");
							}
						}
					}
				}
		this.context = context;
	}
	
	public void checkValidity() throws ConfigurationException{
		if(context == null){
			throw new ConfigurationException(de.uni.freiburg.iig.telematik.secsy.logic.simulation.ConfigurationException.ErrorCode.NO_CONTEXT);
		}
	}
	
	public <O extends Object> boolean checkConstraint(int caseNumber, AbstractConstraint<O> constraint) throws CompatibilityException, ParameterException{
		context.validateAttribute(constraint.getElement());
		if(!caseAttributeValues.containsKey(caseNumber))
			throw new ParameterException(ErrorCode.INCONSISTENCY, "No case data generated yet for case number " + caseNumber);
		
		//This cast cannot fail.
		//In case of incompatibilities, the constructor would have raised an exception.
		constraint.validate((O) caseAttributeValues.get(caseNumber).get(constraint.getElement()));
		return true;
	}
	
	/**
	 * Returns all attributes (containing names and values) for a specific activity and case.
	 * @param activity The activity for which the attributes are requested.
	 * @param caseNumber The number of the case for which attributes/values are requested.
	 * @return The set of attributes for the given activity together with values within the given case.
	 * @throws ParameterException 
	 * @throws Exception If the attribute value generator throws an exception.
	 */
	public Set<DataAttribute> getAttributesForActivity(String id, int caseNumber) throws ParameterException, ValueGenerationException{
		Validate.notNull(id);
		Validate.bigger(caseNumber, 0);
		if(caseAttributeValues.get(caseNumber) == null)
			generateCaseData(caseNumber);
		return createAttributeSet(context.getAttributesFor(id), caseNumber);
	}
	
	
	public Map<String, Object> getAttributeValuesForActivity(String activity, int caseNumber) throws ParameterException, ValueGenerationException {
		Map<String, Object> result = new HashMap<String, Object>();
		for(DataAttribute attribute: getAttributesForActivity(activity, caseNumber)){
			result.put(attribute.name, attribute.value);
		}
		return result;
	}
	
	private Object getValueForAttribute(String attribute, int caseNumber) throws ParameterException, ValueGenerationException{
		if(caseAttributeValues.get(caseNumber) == null)
			generateCaseData(caseNumber);
		return caseAttributeValues.get(attribute);
	}
	
	/**
	 * Generates new values for all attributes within the context within a specific case (i.e. execution of a process).
	 * The values are stored in {@link #caseAttributeValues}.
	 * @param caseNumber The case number for which attribute values should be generated.
	 * @throws ParameterException If the caseNumber is invalid.
	 * @throws ValueGenerationException 
	 * @throws Exception If the attribute value generator throws an exception.
	 */
	protected void generateCaseData(int caseNumber) throws ParameterException, ValueGenerationException {
		Validate.bigger(caseNumber, 0);
		caseAttributeValues.put(caseNumber, new HashMap<String, Object>());
		for(String attribute: context.getAttributes()){
			caseAttributeValues.get(caseNumber).put(attribute, attributeValueGenerator.getNewValueFor(attribute));
		}
	}
	
	/**
	 * Creates a set containing data attributes.<br>
	 * On the creation of attributes, the value of the corresponding data element is assigned.<br>
	 * The method creates a new set of data usages.<br>
	 * It is assumed that the caller ensures parameter validity.
	 * 
	 * @param attributes The same attributes in form of strings.
	 * @param caseNumber The case number (needed to set the values for attributes)
	 * @return A set of attributes together with values.
	 */
	private Set<DataAttribute> createAttributeSet(Set<String> attributes, int caseNumber){
		Set<DataAttribute> result = new HashSet<DataAttribute>();
		for(String attribute: attributes){
			result.add(new DataAttribute(attribute, caseAttributeValues.get(caseNumber).get(attribute)));
		}
		return result;
	}
	
	/**
	 * Implemented interface method for trace completion notification.<br>
	 * On trace completions, information about case attributes is erased to reduce memory overhead.
	 * Trace completion notifications are e.g. delegated from {@link DetailedLogEntryGenerator}.
	 */
	@Override
	public void traceCompleted(int caseNumber) {
		caseAttributeValues.remove(caseNumber);
	}
	
	public static void main(String[] args) throws Exception{
		Set<String> activities = new HashSet<String>(Arrays.asList("A","B","C"));
		Context c = new Context("Context 01", activities);
		c.setAttributes(new HashSet<String>(Arrays.asList("credit")));
		c.addRoutingConstraint("A", new NumberConstraint("credit", NumberOperator.EQUAL, 20));
		
		AttributeValueGenerator gen = new AttributeValueGenerator();
		StochasticValueGenerator<Integer> g = new StochasticValueGenerator<Integer>(1000);
		g.addProbability(20, 1.0);
		gen.setValueGeneration("credit", g);
		
		CaseDataContainer cont = new CaseDataContainer(c, gen);
		
	}
	
	//------- Implemented methods for interface GuardDataContainer --------------------------------------------

	@Override
	public Set<String> getAttributes() {
		return attributeValueGenerator.getAttributes();
	}

	@Override
	public Object getValueForAttribute(String attribute) throws Exception{
		if(!getAttributes().contains(attribute))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Unknown attribute in case daata container: " + attribute);
		return getValueForAttribute(attribute, actualGuardCase);
	}

	@Override
	public Class getAttributeValueClass(String attribute) throws ParameterException{
		if(!getAttributes().contains(attribute))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Unknown attribute in case daata container: " + attribute);
		return attributeValueGenerator.getAttributeValueClass(attribute);
	}
	
	public AttributeValueGenerator getAttributeValueGenerator(){
		return attributeValueGenerator;
	}
	
	public ValueGenerator<?> getValueGenerator(String attribute) throws ParameterException{
		return attributeValueGenerator.getValueGenerator(attribute);
	}
	
	public void takeOverValues(CaseDataContainer dataContainer){
		try {
			this.setContext(dataContainer.getContext());
		} catch (ParameterException e) {
			// Do nothing, context was not set yet.
		}
		
		this.setName(dataContainer.getName());
		this.attributeValueGenerator = dataContainer.getAttributeValueGenerator();
	}
	
	public CaseDataContainerProperties getProperties() throws ParameterException{
		CaseDataContainerProperties properties = new CaseDataContainerProperties();
		
		properties.setName(getName());
		
//		properties.setContextName(context.getName());
		
		properties.setAttributeValueGenerator(attributeValueGenerator);
		
		return properties;
	}
	
	@Override
	public String toString(){
		String valueGenerators = "\n";
		if(!getAttributeValueGenerator().getAttributes().isEmpty()){
			StringBuilder builder = new StringBuilder();
			
			builder.append('\n');
			for(String attribute: getAttributeValueGenerator().getAttributes()){
				try {
					builder.append(String.format(valueGeneratorFormat, attribute, getAttributeValueGenerator().getValueGenerator(attribute)));
				} catch (ParameterException e) {
					builder.append(String.format(valueGeneratorFormat, attribute, "Cannot extract value generator"));
				}
			}
			valueGenerators = builder.toString();
		}
		
		Object defaultValue = getAttributeValueGenerator().getDefaultValue();
		String defaultValueClass = "Object";
		if(defaultValue == null){
			defaultValue = "null";
		} else {
			defaultValueClass = defaultValue.getClass().getSimpleName();
		}
		
		return String.format(toStringFormat, getName(), 
											 String.format(defaultValueFormat, defaultValue, defaultValueClass),
											 valueGenerators);
	}

}
