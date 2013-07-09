package de.uni.freiburg.iig.telematik.secsy.logic.generator.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.constraint.StringConstraint;
import de.invation.code.toval.misc.ArrayUtils;
import de.invation.code.toval.misc.StringUtils;
import de.invation.code.toval.properties.AbstractProperties;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.Context;

public class ContextProperties extends AbstractProperties {
	
	public static final String defaultName = "NewContext";
	
	private final String CONSTRAINT_FORMAT = ContextProperty.CONSTRAINT + "_%s";
	private final String ACTIVITY_CONSTRAINTS_FORMAT = ContextProperty.ACTIVITY_CONSTRAINTS + "_%s";
	private final String DATA_USAGE_FORMAT = ContextProperty.DATA_USAGE + "_%s";
	private final String DATA_USAGE_VALUE_FORMAT = "\"%s\" %s";
	private final String ACTIVITY_DATA_USAGES_FORMAT = ContextProperty.ACTIVITY_DATA_USAGES + "_%s";
	
	//------- Property setting -------------------------------------------------------------
	
	private void setProperty(ContextProperty contextProperty, Object value){
		props.setProperty(contextProperty.toString(), value.toString());
	}
	
	private String getProperty(ContextProperty contextProperty){
		return props.getProperty(contextProperty.toString());
	}
	
	//-- Context name
	
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		Validate.notEmpty(name);
		setProperty(ContextProperty.CONTEXT_NAME, name);
	}
	
	public String getName() throws PropertyException {
		String propertyValue = getProperty(ContextProperty.CONTEXT_NAME);
		if(propertyValue == null)
			throw new PropertyException(ContextProperty.CONTEXT_NAME, propertyValue);
		return propertyValue;
	}
	
	//-- Activities
	
	public void addActivity(String activity) throws ParameterException{
		addActivities(Arrays.asList(activity));
	}
	
	public void addActivities(Collection<String> activities) throws ParameterException{
		validateStringCollection(activities);
		Set<String> currentValues = getActivities();
		currentValues.addAll(activities);
		setProperty(ContextProperty.ACTIVITIES, ArrayUtils.toString(encapsulateValues(currentValues)));
	}
	
	public Set<String> getActivities(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ACTIVITIES);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(activityTokens.hasMoreTokens()){
			String nextToken = activityTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	public boolean existActivities(){
		return !getActivities().isEmpty();
	}
	
	public void removeActivity(String activity) throws ParameterException{
		removeActivities(Arrays.asList(activity));
	}
	
	public void removeActivities(Collection<String> activities) throws ParameterException{
		validateStringCollection(activities);
		Set<String> currentActivities = getActivities();
		currentActivities.removeAll(activities);
		setProperty(ContextProperty.ACTIVITIES, ArrayUtils.toString(encapsulateValues(currentActivities)));
	}
	
	//-- Subjects
	
	public void addSubject(String subject) throws ParameterException{
		addSubjects(Arrays.asList(subject));
	}
	
	public void addSubjects(Collection<String> subjects) throws ParameterException{
		validateStringCollection(subjects);
		Set<String> currentValues = getSubjects();
		currentValues.addAll(subjects);
		setProperty(ContextProperty.SUBJECTS, ArrayUtils.toString(encapsulateValues(currentValues)));
	}
	
	public Set<String> getSubjects(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.SUBJECTS);
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	public boolean existSubjects(){
		return !getSubjects().isEmpty();
	}
	
	public void removeSubject(String subject) throws ParameterException{
		removeSubjects(Arrays.asList(subject));
	}
	
	public void removeSubjects(Collection<String> subjects) throws ParameterException{
		validateStringCollection(subjects);
		Set<String> currentSubjects = getSubjects();
		currentSubjects.removeAll(subjects);
		setProperty(ContextProperty.SUBJECTS, ArrayUtils.toString(encapsulateValues(currentSubjects)));
	}
	
	//-- Attributes
	
	public void addAttribute(String attribute) throws ParameterException{
		addAttributes(Arrays.asList(attribute));
	}
	
	public void addAttributes(Collection<String> attributes) throws ParameterException{
		validateStringCollection(attributes);
		Set<String> currentValues = getAttributes();
		currentValues.addAll(attributes);
		setProperty(ContextProperty.ATTRIBUTES, ArrayUtils.toString(encapsulateValues(currentValues)));
	}
	
	public Set<String> getAttributes(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ATTRIBUTES);
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken.substring(1, nextToken.length()-1));
		}
		return result;
	}
	
	public boolean existAttributes(){
		return !getAttributes().isEmpty();
	}
	
	public void removeAttribute(String attribute) throws ParameterException{
		removeAttributes(Arrays.asList(attribute));
	}
	
	public void removeAttributes(Collection<String> attributes) throws ParameterException{
		validateStringCollection(attributes);
		Set<String> currentAttributes = getAttributes();
		currentAttributes.removeAll(attributes);
		setProperty(ContextProperty.ATTRIBUTES, ArrayUtils.toString(encapsulateValues(currentAttributes)));
	}
	
	//-- Routing Constraints
	
	/**
	 * Adds a routing constraint for an activity.
	 * @param activity The name of the activity for which the constraint is added.
	 * @param constraint The routing constraint to add.
	 * @throws ParameterException if the given parameters are invalid.
	 * @throws PropertyException if the given constraint cannot be added as a property.
	 * @see #addConstraint(AbstractConstraint)
	 * @see #addActivityWithConstraints(String)
	 */
	public void addRoutingConstraint(String activity, AbstractConstraint<?> constraint) throws ParameterException, PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(constraint);
		
		//1. Add constraint itself
		//   This also adds the constraint to the list of constraints
		String propertyNameForNewConstraint = addConstraint(constraint);
		
		//2. Add constraint name to the list of constraints for this activity
		Set<String> currentConstraintNames = getConstraintNames(activity);
		currentConstraintNames.add(propertyNameForNewConstraint);
		if(currentConstraintNames.size() == 1){
			//Add the activity to the list of activities with routing constraints
			addActivityWithConstraints(activity);
		}
		props.setProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity), ArrayUtils.toString(currentConstraintNames.toArray()));
	}
	
	/**
	 * Adds an activity to the list of activities with routing constraints.
	 * @param activity The name of the activity to add.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void addActivityWithConstraints(String activity) throws ParameterException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithConstraints();
		currentActivities.add(activity);
		setProperty(ContextProperty.ACTIVITIES_WITH_CONSTRAINTS, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Removes an activity from the list of activities with routing constraints.
	 * @param activity The name of the activity to remove.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void removeActivityWithConstraints(String activity) throws ParameterException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithConstraints();
		currentActivities.remove(activity);
		setProperty(ContextProperty.ACTIVITIES_WITH_CONSTRAINTS, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Returns the names of all activities with routing constraints.
	 * @return A set of all activities with routing constraints.
	 */
	public Set<String> getActivitiesWithConstraints(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ACTIVITIES_WITH_CONSTRAINTS);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	public boolean hasConstraints(String activity) throws ParameterException{
		Validate.notNull(activity);
		String propertyValue = props.getProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		return propertyValue != null;
	}
	
	/**
	 * Adds the given routing constraint to the context properties.<br>
	 * This requires to generate a new name for the constraint (e.g. CONSTRAINT_1) <br>
	 * and storing a string-representation of this constraint under this name.<br>
	 * Additionally, the new constraint name is stored in the property field which is summing up all constraint names (CONSTRAINTS).
	 * @param constraint The routing constraint to add.
	 * @return The newly generated name for the constraint under which it is accessible.
	 * @throws ParameterException if the given routing constraint is <code>null</code>. 
	 * @throws PropertyException if the constraint property name cannot be generated or the constraint cannot be stored.
	 * @see #getNextConstraintIndex()
	 * @see #addConstraintName(String)
	 */
	private String addConstraint(AbstractConstraint<?> constraint) throws PropertyException, ParameterException{
		Validate.notNull(constraint);
		String constraintName = String.format(CONSTRAINT_FORMAT, getNextConstraintIndex());
		props.setProperty(constraintName, constraint.toString());
		addConstraintNameToList(constraintName);
		return constraintName;
	}
	
	
	/**
	 * Returns the lowest unused index for constraint names.<br>
	 * Constraint names are enumerated (CONSTRAINT_1, CONSTRAINT_2, ...).<br>
	 * When new constraints are added, the lowest unused index is used as property name.
	 * @return The lowest free index to be used for constraint naming.
	 * @throws PropertyException if the extraction of used indexes fails.
	 * @see {@link #getConstraintNameIndexes()}
	 */
	private int getNextConstraintIndex() throws PropertyException{
		Set<Integer> usedIndexes = getConstraintNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	/**
	 * Returns all used indexes for constraint property names.<br>
	 * Constraint names are enumerated (CONSTRAINT_1, CONSTRAINT_2, ...).<br>
	 * When new constraints are added, the lowest unused index is used within the new property name.
	 * @return The set of indexes in use.
	 * @throws PropertyException if existing constraint names are invalid (e.g. due to external file manipulation).
	 */
	private Set<Integer> getConstraintNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> constraintNames = getConstraintNameList();
		if(constraintNames.isEmpty())
			return result;
		for(String constraintName: constraintNames){
			int separatorIndex = constraintName.lastIndexOf("_");
			if(separatorIndex == -1 || (constraintName.length() == separatorIndex + 1))
				throw new PropertyException(ContextProperty.CONSTRAINT, constraintName, "Corrupted property file (invalid constraint name)");
			Integer index = null;
			try {
				index = Integer.parseInt(constraintName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(ContextProperty.CONSTRAINT, constraintName, "Corrupted property file (invalid constraint name)");
			}
			result.add(index);
		}
		return result;
	}
	
	/**
	 * Adds a new constraint property name to the list of constraint properties (CONSTRAINTS-field).
	 * @param constraintName The name of the constraint-property to add (e.g. CONSTRAINT_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void addConstraintNameToList(String constraintName) throws ParameterException{
		validateStringValue(constraintName);
		Set<String> currentValues = getConstraintNameList();
		currentValues.add(constraintName);
		setProperty(ContextProperty.ALL_CONSTRAINTS, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Removes the constraint property with the given name from the list of constraint properties (CONSTRAINTS-field).
	 * @param constraintName The name of the constraint-property to remove (e.g. CONSTRAINT_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void removeConstraintNameFromList(String constraintName) throws ParameterException{
		validateStringValue(constraintName);
		Set<String> currentValues = getConstraintNameList();
		currentValues.remove(constraintName);
		setProperty(ContextProperty.ALL_CONSTRAINTS, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Returns all property names for routing constraints, <br>
	 * i.e. the value of the context property CONSTRAINTS.
	 * @return A set of all used property names for routing constraints.
	 */
	private Set<String> getConstraintNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ALL_CONSTRAINTS);
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	/**
	 * Returns all routing constraints of the given activity in form of constraint-objects.
	 * 
	 * @param activity The name of the activity whose routing constraints are requested.
	 * @return A possibly empty set of routing constraints.
	 * @throws ParameterException if the activity name is <code>null</code> or empty.
	 * @throws PropertyException if corresponding constraint-properties cannot be extracted.
	 * @see #getConstraintNames(String)
	 * @see #getConstraint(String)
	 */
	public Set<AbstractConstraint<?>> getRoutingConstraints(String activity) throws ParameterException, PropertyException{
		Set<String> constraintNames = getConstraintNames(activity);
		Set<AbstractConstraint<?>> result = new HashSet<AbstractConstraint<?>>();
		for(String constraintName: constraintNames){
			result.add(getConstraint(constraintName));
		}
		return result;
	}
	
	/**
	 * Returns the property-names of all routing constraints related to the given activity.<br>
	 * These names are required to extract constraint property-values.
	 * @param activity The name of the activity
	 * @return A set of constraint property-names related to the given activity.
	 * @throws ParameterException if the given activity name is <code>null</code or invalid.
	 */
	private Set<String> getConstraintNames(String activity) throws ParameterException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	/**
	 * Extracts the constraint with the given name in form of a constraint-object.<br>
	 * @param constraintName The property-name of the constraint (CONSTRAINT_X)
	 * @return The constraint-representation of the constraint-property
	 * @throws PropertyException if there is no constraint with the given name or the value cannot be converted into a number- or string-constraint.
	 */
	private AbstractConstraint<?> getConstraint(String constraintName) throws PropertyException{
		String constraintString = props.getProperty(constraintName);
		if(constraintString == null)
			throw new PropertyException(ContextProperty.CONSTRAINT, constraintName, "Unparseable constraint");
		AbstractConstraint<?> result = null;
		try{
			result = NumberConstraint.parse(constraintString);
		}catch(Exception e){
			try{
				result = StringConstraint.parse(constraintString);
			}catch(Exception e1){
				throw new PropertyException(ContextProperty.CONSTRAINT, constraintName, "Unparseable constraint");
			}
		}
		return result;
	}
	
	public void removeRoutingConstraint(String activity, AbstractConstraint<?> constraint) throws ParameterException, PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(constraint);
		//Find name of the given constraint
		Set<String> propertyNamesForActivityConstraints = getConstraintNames(activity);
		if(propertyNamesForActivityConstraints.isEmpty()){
			//This activity has no constraints.
			return;
		}
		String propertyNameForConstraintToRemove = null;
		for(String propertyNameForActivityConstraint: propertyNamesForActivityConstraints){
			AbstractConstraint<?> activityConstraint = getConstraint(propertyNameForActivityConstraint);
			if(activityConstraint.equals(constraint)){
				propertyNameForConstraintToRemove = propertyNameForActivityConstraint;
				break;
			}
		}
		if(propertyNameForConstraintToRemove == null){
			//There is no stored reference to the given constraint to remove
			//-> Removal not necessary
			return;
		}
		
		//1. Remove the constraint itself
		props.remove(propertyNameForConstraintToRemove);
		
		//2. Remove the constraint from the list of constraints
		removeConstraintNameFromList(propertyNameForConstraintToRemove);
		
		//3. Remove the constraint from the activity constraint list.
		Set<String> currentConstraintNames = getConstraintNames(activity);
		currentConstraintNames.remove(propertyNameForConstraintToRemove);
		if(currentConstraintNames.isEmpty()){
			removeActivityWithConstraints(activity);
			props.remove(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity));
		} else {
			props.setProperty(String.format(ACTIVITY_CONSTRAINTS_FORMAT, activity), ArrayUtils.toString(currentConstraintNames.toArray()));
		}
	}
	
	
	//-- AC Model
	
	public void setACModelName(String acModelName) throws ParameterException{
		validateStringValue(acModelName);
		setProperty(ContextProperty.AC_MODEL_NAME, acModelName);
	}
	
	public String getACModelName() throws PropertyException, ParameterException {
		String propertyValue = getProperty(ContextProperty.AC_MODEL_NAME);
		if(propertyValue == null)
			throw new PropertyException(ContextProperty.AC_MODEL_NAME, propertyValue);
		
		validateStringValue(propertyValue);
		
		return propertyValue;
	}
	
	// Data Usage
	
	public void setDataUsage(String activity, Map<String, Set<DataUsage>> dataUsage) throws ParameterException, PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Validate.notNull(dataUsage);
		Validate.noNullElements(dataUsage.keySet());
		Validate.noNullElements(dataUsage.values());
		
		//1. Add data usages
		//   This also adds the data usages to the list of data usages
		List<String> propertyNamesForDataUsages = new ArrayList<String>();
		for(String attribute: dataUsage.keySet()){
			propertyNamesForDataUsages.add(addDataUsage(attribute, dataUsage.get(attribute)));
		}
		
		//2. Add data usage names to the list of data usages for this activity
		addActivityWithDataUsage(activity);
		props.setProperty(String.format(ACTIVITY_DATA_USAGES_FORMAT, activity), ArrayUtils.toString(propertyNamesForDataUsages.toArray()));
	}
	
	public Map<String, Set<DataUsage>> getDataUsageFor(String activity) throws ParameterException, PropertyException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		
		Set<String> dataUsageNames = getDataUsageNames(activity);
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		for(String dataUsageName: dataUsageNames){
			Map<String, Set<DataUsage>> dataUsage = getDataUsage(dataUsageName);
			String attribute = dataUsage.keySet().iterator().next();
			result.put(attribute, dataUsage.get(attribute));
		}
		return result;
	}
	
	/**
	 * Adds the given data usage to the context properties.<br>
	 * This requires to generate a new name for the data usage (e.g. DATA_USAGE_1) <br>
	 * and storing a string-representation of this data usage under this name.<br>
	 * Additionally, the new data usage name is stored in the property field which is summing up all data usage names (ACTIVITY_DATA_USAGES).
	 * @return The newly generated name for the data usage under which it is accessible.
	 * @throws ParameterException if the given data usage parameters are invalid. 
	 * @throws PropertyException if the data usage property name cannot be generated or the data usage cannot be stored.
	 * @see #getNextDataUsageIndex()
	 * @see #addDataUsageNameToList(String)
	 */
	private String addDataUsage(String attribute, Set<DataUsage> usages) throws PropertyException, ParameterException{
		Validate.notNull(attribute);
		Validate.notEmpty(attribute);
		Validate.notNull(usages);
		Validate.noNullElements(usages);
		String dataUsageName = String.format(DATA_USAGE_FORMAT, getNextDataUsageIndex());
		props.setProperty(dataUsageName, String.format(DATA_USAGE_VALUE_FORMAT, attribute, ArrayUtils.toString(usages.toArray())));
		addDataUsageNameToList(dataUsageName);
		return dataUsageName;
	}
	
	/**
	 * Extracts the data usage with the given name in form of a map containing the attribute as key and the data usages as value.<br>
	 * @param dataUsageName The property-name of the data usage (DATA_USAGE_X)
	 * @return The map-representation of the data usage-property
	 * @throws PropertyException if there is no constraint with the given name or the value cannot be converted into a number- or string-constraint.
	 */
	private Map<String, Set<DataUsage>> getDataUsage(String dataUsageName) throws PropertyException{
		String dataUsageString = props.getProperty(dataUsageName);
		if(dataUsageString == null)
			throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "No data usage with name \""+dataUsageName+"\"");
		Map<String, Set<DataUsage>> result = new HashMap<String, Set<DataUsage>>();
		int delimiterIndex = dataUsageString.indexOf(" ");
		if(delimiterIndex == -1)
			throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
		String attributeString = null;
		String dataUsagesString = null;
		try {
			attributeString = dataUsageString.substring(0, delimiterIndex);
			dataUsagesString = dataUsageString.substring(delimiterIndex+1);
			
			attributeString = attributeString.substring(1, attributeString.length()-1);
		} catch(Exception e){
			throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
		}
		
		Set<DataUsage> usageModes = new HashSet<DataUsage>();
		StringTokenizer usageModeTokens = StringUtils.splitArrayString(dataUsagesString, " ");
		while(usageModeTokens.hasMoreTokens()){
			try {
				usageModes.add(DataUsage.parse(usageModeTokens.nextToken()));
			} catch (ParameterException e) {
				throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "Invalid property value for data usage with name \""+dataUsageName+"\"");
			}
		}
		result.put(attributeString, usageModes);
		
		return result;
	}
	
	/**
	 * Adds an activity to the list of activities with data usage.
	 * @param activity The name of the activity to add.
	 * @throws ParameterException if the activity name is invalid.
	 */
	private void addActivityWithDataUsage(String activity) throws ParameterException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> currentActivities = getActivitiesWithDataUsage();
		currentActivities.add(activity);
		setProperty(ContextProperty.ACTIVITIES_WITH_DATA_USAGE, ArrayUtils.toString(currentActivities.toArray()));
	}
	
	/**
	 * Returns the names of all activities with data usage.
	 * @return A set of all activities with data usage.
	 */
	public Set<String> getActivitiesWithDataUsage(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ACTIVITIES_WITH_DATA_USAGE);
		if(propertyValue == null)
			return result;
		StringTokenizer activityTokens = StringUtils.splitArrayString(propertyValue, " ");
		while(activityTokens.hasMoreTokens()){
			result.add(activityTokens.nextToken());
		}
		return result;
	}
	
	/**
	 * Adds a new data usage property name to the list of data usage properties (DATA_USAGES-field).
	 * @param dataUsageName The name of the data usage-property to add (e.g. DATA_USAGE_5).
	 * @throws ParameterException if the given property name is invalid.
	 */
	private void addDataUsageNameToList(String dataUsageName) throws ParameterException{
		validateStringValue(dataUsageName);
		Set<String> currentValues = getDataUsageNameList();
		currentValues.add(dataUsageName);
		setProperty(ContextProperty.ALL_DATA_USAGES, ArrayUtils.toString(currentValues.toArray()));
	}
	
	/**
	 * Returns the lowest unused index for data usage names.<br>
	 * Data usage names are enumerated (DATA_USAGE_1, DATA_USAGE_2, ...).<br>
	 * When new data usages are added, the lowest unused index is used as property name.
	 * @return The lowest free index to be used for data usage naming.
	 * @throws PropertyException if the extraction of used indexes fails.
	 * @see {@link #getDataUsageNameIndexes()}
	 */
	private int getNextDataUsageIndex() throws PropertyException{
		Set<Integer> usedIndexes = getDataUsageNameIndexes();
		int nextIndex = 1;
		while(usedIndexes.contains(nextIndex)){
			nextIndex++;
		}
		return nextIndex;
	}
	
	/**
	 * Returns all used indexes for data usage property names.<br>
	 * Constraint names are enumerated (DATA_USAGE_1, DATA_USAGE_2, ...).<br>
	 * When new data usages are added, the lowest unused index is used within the new property name.
	 * @return The set of indexes in use.
	 * @throws PropertyException if existing constraint names are invalid (e.g. due to external file manipulation).
	 */
	private Set<Integer> getDataUsageNameIndexes() throws PropertyException{
		Set<Integer> result = new HashSet<Integer>();
		Set<String> dataUsageNames = getDataUsageNameList();
		if(dataUsageNames.isEmpty())
			return result;
		for(String dataUsageName: dataUsageNames){
			int separatorIndex = dataUsageName.lastIndexOf("_");
			if(separatorIndex == -1 || (dataUsageName.length() == separatorIndex + 1))
				throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "Corrupted property file (invalid data usage name)");
			Integer index = null;
			try {
				index = Integer.parseInt(dataUsageName.substring(separatorIndex+1));
			} catch(Exception e){
				throw new PropertyException(ContextProperty.DATA_USAGE, dataUsageName, "Corrupted property file (invalid data usage name)");
			}
			result.add(index);
		}
		return result;
	}
	
	/**
	 * Returns the property-names of all data usages related to the given activity.<br>
	 * These names are required to extract data usage property-values.
	 * @param activity The name of the activity
	 * @return A set of data usage property-names related to the given activity.
	 * @throws ParameterException if the given activity name is <code>null</code or invalid.
	 */
	private Set<String> getDataUsageNames(String activity) throws ParameterException{
		Validate.notNull(activity);
		Validate.notEmpty(activity);
		Set<String> result = new HashSet<String>();
		String propertyValue = props.getProperty(String.format(ACTIVITY_DATA_USAGES_FORMAT, activity));
		if(propertyValue == null)
			return result;
		StringTokenizer subjectTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(subjectTokens.hasMoreTokens()){
			String nextToken = subjectTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	/**
	 * Returns all property names for data usages, <br>
	 * i.e. the value of the context property ACTIVITY_DATA_USAGES.
	 * @return A set of all used property names for data usages.
	 */
	private Set<String> getDataUsageNameList(){
		Set<String> result = new HashSet<String>();
		String propertyValue = getProperty(ContextProperty.ALL_DATA_USAGES);
		if(propertyValue == null)
			return result;
		StringTokenizer attributeTokens = StringUtils.splitArrayString(propertyValue, String.valueOf(ArrayUtils.VALUE_SEPARATION));
		while(attributeTokens.hasMoreTokens()){
			String nextToken = attributeTokens.nextToken();
			result.add(nextToken);
		}
		return result;
	}
	
	// Startup/Test
	
	public static void main(String[] args) throws Exception{
		ContextProperties p = new ContextProperties();
		NumberConstraint n1 = NumberConstraint.parse("alf < 50.0");
		NumberConstraint n2 = NumberConstraint.parse("alf == 100");
		NumberConstraint n3 = NumberConstraint.parse("alf == 100");
		p.addRoutingConstraint("act1", n1);
		p.addRoutingConstraint("act1", n2);
		p.addRoutingConstraint("act2", n3);
		p.store("TELEFON");
		System.out.println("act1: " + p.getRoutingConstraints("act1"));
		System.out.println("act2: " + p.getRoutingConstraints("act2"));
//		System.out.println("remove all constraints for activity act1");
//		p.removeRoutingConstraint("act1", n1);
//		p.removeRoutingConstraint("act1", n2);
//		System.out.println("act1" + p.getRoutingConstraints("act1"));
//		System.out.println("act2" + p.getRoutingConstraints("act2"));
		
		
		Map<String, Set<DataUsage>> usage = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage.put("attribute1", modes);
		p.setDataUsage("act1", usage);
		p.store("TELEFON");
		
		System.out.println(p.getDataUsageFor("act1"));
		
		Set<String> activities = new HashSet<String>(Arrays.asList("act1", "act2"));
		Context c = new Context("c1", activities);
//		c.getD
	}

}
