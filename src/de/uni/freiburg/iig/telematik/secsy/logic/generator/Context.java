package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.constraint.NumberConstraint;
import de.invation.code.toval.misc.SetUtils;
import de.invation.code.toval.properties.PropertyException;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.InconsistencyException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.invation.code.toval.validate.ParameterException.ErrorCode;
import de.uni.freiburg.iig.telematik.secsy.logic.generator.properties.ContextProperties;
import de.uni.freiburg.iig.telematik.sepia.petrinet.AbstractTransition;
import de.uni.freiburg.iig.telematik.sepia.util.PNUtils;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.ACModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.acl.ACLModel;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.rbac.RBACModel;

/**
 * This class provides context information for process execution.<br>
 * More specifically:<br>
 * <ul>
 * <li></li>
 * </ul>
 * 
 * To decide which activities can be executed by which subjects, it uses an access control model.
 * 
 * Note: A context must be compatible with the process it is used for,<br>
 * i.e. contain all process activities.
 * 
 * @author Thomas Stocker
 */
public class Context {
	/**
	 * The name of the context.
	 */
	protected String name = ContextProperties.defaultName;
	/**
	 * Names of process activities.
	 */
	protected Set<String> activities = new HashSet<String>();
	/**
	 * Names of subjects executing process activities.
	 */
	protected Set<String> subjects = new HashSet<String>();
	/**
	 * Names of data attributes used during process execution.
	 */
	protected Set<String> attributes = new HashSet<String>();
	/**
	 * Data usage (read, write, ...) for attributes which are used in process activities.
	 */
	protected Map<String, Map<String, Set<DataUsage>>> activityDataUsage = new HashMap<String, Map<String, Set<DataUsage>>>();
	/**
	 * Access control model used to decide which subjects can execute which activities.
	 */
	protected ACModel acModel = null;
	/**
	 * Constraints on attributes, which are used for routing purposes.<br>
	 * Example: Activity "Double Check" is executed when the credit amount exceeds $50.000.<br>
	 * Within the simulation, this property can be enforced by adding a constraint<br>
	 * "creditAmount > 50.000" to the activity "Double Check", which ensures,<br>
	 * that the activity is only executed when the constraint holds.
	 */
	protected Map<String, Set<AbstractConstraint<?>>> routingConstraints = new HashMap<String, Set<AbstractConstraint<?>>>();
	
	protected List<DataUsage> validUsageModes = new ArrayList<DataUsage>(Arrays.asList(DataUsage.values()));
	
	
	//------- Constructors ------------------------------------------------------------------
	
	/**
	 * Creates a new context using the given activity names.
	 * @param activities Names of process activities.
	 * @throws ParameterException 
	 * @throws Exception If activity list is <code>null</code> or empty.
	 */
	public Context(String name, Set<String> activities) throws ParameterException {
		setName(name);
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		this.activities.addAll(activities);
	}
	
	/**
	 * Creates a new context on basis of the given Petri net transitions.<br>
	 * Transitions are converted into a list of activity names.
	 * @param transitions Petri net transitions to be used as basis for activity names.
	 * @throws ParameterException 
	 * @throws Exception If activity list is <code>null</code> or empty.
	 * @see {@link #getNameListFromTransitions(Collection)}
	 */
	public Context(String name, Collection<AbstractTransition<?,?>> transitions) throws ParameterException {
		this(name, PNUtils.getLabelSetFromTransitions(transitions, false));
	}
	
	
	//------- Getters and Setters ------------------------------------------------------------
	
	/**
	 * Returns the name of the context.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Sets the name of the context.
	 * @throws ParameterException 
	 */
	public void setName(String name) throws ParameterException{
		Validate.notNull(name);
		this.name = name;
	}
	
	/**
	 * Returns the names of context activities.
	 * @return An unmodifiable list of activity names.
	 */
	public Set<String> getActivities(){
		return Collections.unmodifiableSet(activities);
	}
	
	public boolean hasActivities(){
		return !activities.isEmpty();
	}
	
	public void addActivities(Collection<String> activities) throws InconsistencyException, ParameterException{
		addActivities(activities, false);
	}
	
	public void addActivities(Collection<String> activities, boolean addToACModel) throws InconsistencyException, ParameterException{
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		
		if(acModel != null){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				Set<String> newActivities = new HashSet<String>(this.activities);
				newActivities.addAll(activities);
				validateACModel(acModel, subjects, newActivities, attributes, validUsageModes);
			} else {
				acModel.addTransactions(activities);
			}
		}
		this.activities.addAll(activities);
	}
	
	public void removeActivities(Collection<String> activities) throws ParameterException{
		removeActivities(activities, false);
	}
	
	public void removeActivities(Collection<String> activities, boolean removeFromACModel) throws ParameterException{
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		
		if(acModel != null && removeFromACModel){
			acModel.removeTransactions(activities);
		}
		this.activities.removeAll(activities);
		this.activityDataUsage.keySet().removeAll(activities);
		this.routingConstraints.keySet().removeAll(activities);
	}
	
	/**
	 * Returns the names of context attributes.
	 * @return An unmodifiable list of attribute names.
	 */
	public Set<String> getAttributes(){
		return Collections.unmodifiableSet(attributes);
	}
	
	public boolean hasAttributes(){
		return !attributes.isEmpty();
	}
	
	/**
	 * Sets the context attributes.<br>
	 * When context attributes are set, data usage is also reset.
	 * @param attributes A list of attribute names.
	 * @throws ParameterException 
	 * @see #clearAttributes()
	 */
	public void setAttributes(Set<String> attributes) throws ParameterException{
		Validate.notNull(attributes);
		Validate.notEmpty(attributes);
		clearAttributes();
		this.attributes.addAll(attributes);
	}
	
	/**
	 * Resets the context attributes.<br>
	 * This method clears the attribute list and also the maps for data usage.
	 */
	public void clearAttributes(){
		attributes.clear();
		activityDataUsage.clear();
	}
	
	public void addAttributes(Collection<String> attributes) throws InconsistencyException, ParameterException{
		addAttributes(attributes, false);
	}
	
	public void addAttributes(Collection<String> attributes, boolean addToACModel) throws InconsistencyException, ParameterException{
		Validate.notNull(attributes);
		Validate.notEmpty(attributes);
		
		if(acModel != null){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				Set<String> newAttributes = new HashSet<String>(this.attributes);
				newAttributes.addAll(attributes);
				validateACModel(acModel, subjects, activities, newAttributes, validUsageModes);
			} else {
				acModel.addObjects(attributes);
			}
		}
		this.attributes.addAll(attributes);
	}
	
	public void removeAttributes(Collection<String> attributes) throws ParameterException{
		removeActivities(attributes, false);
	}
	
	public void removeAttributes(Collection<String> attributes, boolean removeFromACModel) throws ParameterException{
		Validate.notNull(attributes);
		Validate.noNullElements(attributes);
		
		if(acModel != null && removeFromACModel){
			acModel.removeObjects(attributes);
		}
		
		// Remove data usages of removed attributes
		for(String activity: activities){
			for(String attribute: attributes){
				removeDataUsageFor(activity, attribute);
			}
		}
		
		// Remove all routing constraints that relate to removed attributes
		Set<AbstractConstraint<?>> constraintsToRemove = new HashSet<AbstractConstraint<?>>();
		for(String activity: activities){
			if(routingConstraints.containsKey(activity)){
				constraintsToRemove.clear();
				for(AbstractConstraint<?> c: routingConstraints.get(activity)){
					if(attributes.contains(c.getElement())){
						constraintsToRemove.add(c);
					}
				}
				routingConstraints.get(activity).removeAll(constraintsToRemove);
			}
		}
		
		// Remove attributes from attribute list
		this.attributes.removeAll(attributes);
	}
	
	/**
	 * Returns the context subjects.
	 * @return An unmodifiable list of context subjects. 
	 */
	public Set<String> getSubjects(){
		return Collections.unmodifiableSet(subjects);
	}
	
	public boolean hasSubjects(){
		return !subjects.isEmpty();
	}
	
	/**
	 * Sets the context subjects.
	 * @param subjects A list of subject names.
	 * @throws ParameterException 
	 */
	public void setSubjects(Set<String> subjects) throws ParameterException{
		Validate.notNull(subjects);
		Validate.notEmpty(subjects);
		this.subjects.clear();
		this.subjects.addAll(subjects);
	}
	
	public void addSubjects(Collection<String> subjects) throws InconsistencyException, ParameterException{
		addSubjects(subjects, false);
	}
	
	public void addSubjects(Collection<String> subjects, boolean addToACModel) throws InconsistencyException, ParameterException{
		Validate.notNull(subjects);
		Validate.notEmpty(subjects);
		
		if(acModel != null){
			if(!addToACModel){
				// Check if new activities cause an inconsistency
				Set<String> newSubjects = new HashSet<String>(this.subjects);
				newSubjects.addAll(subjects);
				validateACModel(acModel, newSubjects, activities, attributes, validUsageModes);
			} else {
				acModel.addSubjects(subjects);
			}
		}
		this.subjects.addAll(subjects);
	}
	
	public void removeSubjects(Collection<String> subjects) throws ParameterException{
		removeSubjects(subjects, false);
	}
	
	public void removeSubjects(Collection<String> subjects, boolean removeFromACModel) throws ParameterException{
		Validate.notNull(subjects);
		Validate.noNullElements(subjects);
		
		if(acModel != null && removeFromACModel){
			acModel.removeSubjects(subjects);
		}
		
		// Remove subjects from subject set
		this.subjects.removeAll(subjects);
	}
	
	/**
	 * Returns the access control model which is used to determine
	 * authorized subjects for activity execution.
	 * @return The access control model of the context.
	 */
	public ACModel getACModel(){
		return acModel;
	}
	
	/**
	 * Sets the access control model which is used to determine
	 * authorized subjects for activity execution.<br>
	 * This method checks, if the access control model is compatible with the log context,
	 * i.e. is contains all subjects and activities of the log context.
	 * 
	 * @param acModel An access control model.
	 * @throws InconsistencyException, ParameterException 
	 */
	public void setACModel(ACModel acModel) throws InconsistencyException, ParameterException {
		validateACModel(acModel);
		this.acModel = acModel;
	}
	
	public void removeACModel(){
		this.acModel = null;
	}
	
	public boolean isCompatible(ACModel acModel){
		try {
			validateACModel(acModel);
			return true;
		} catch (Exception e) {
			return false;
		} 
	}
	
	public List<DataUsage> getValidUsageModes(){
		return Collections.unmodifiableList(validUsageModes);
	}
	
	@SuppressWarnings("unchecked")
	public void setValidUsageModes(Collection<DataUsage> validUsageModes) throws ParameterException{
		Validate.notNull(validUsageModes);
		Validate.notEmpty(validUsageModes);
		Validate.noNullElements(validUsageModes);
		if(acModel != null){
			if(!SetUtils.containSameElements(new HashSet<DataUsage>(validUsageModes), new HashSet<DataUsage>(acModel.getValidUsageModes())))
				throw new ParameterException(ErrorCode.INCONSISTENCY, "Existing object permissions are in conflict with new set of valid usage modes.");
		}
		if(!activityDataUsage.isEmpty()){
			for(String activity: activities){
				if(activityDataUsage.containsKey(activity)){
					for(String attribute: attributes){
						if(activityDataUsage.get(activity).containsKey(attribute)){
							if(activityDataUsage.get(activity).get(attribute) != null){
								if(!validUsageModes.containsAll(activityDataUsage.get(activity).get(attribute)))
									throw new ParameterException(ErrorCode.INCONSISTENCY, "Existing activity data usages are in conflict with new set of valid usage modes.");
							}
						}
					}
				}
			}
		}
		this.validUsageModes.clear();
		this.validUsageModes.addAll(validUsageModes);
	}
	
	//------- Constraints -----------------------------------------------------------------------------------------------
	
	
	public <C extends AbstractConstraint<?>> boolean addRoutingConstraint(String activity, C constraint) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		Validate.notNull(constraint);
		validateAttribute(constraint.getElement());
		if(!getAttributesFor(activity).contains(constraint.getElement()))
			throw new CompatibilityException("Cannot add constraint on attribute " + constraint.getElement() + " for activity " + activity + ". Activity does not use attribute.");
		
		
		if(!routingConstraints.containsKey(activity))
			routingConstraints.put(activity, new HashSet<AbstractConstraint<?>>());
		return routingConstraints.get(activity).add(constraint);
	}
	
	public boolean hasRoutingConstraints(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		return routingConstraints.containsKey(activity);
	}
	
	public Set<AbstractConstraint<?>> getRoutingConstraints(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		return routingConstraints.get(activity);
	}
	
	public Set<String> getActivitiesWithRoutingConstraints(){
		return routingConstraints.keySet();
	}
	
	public <C extends AbstractConstraint<?>> void removeRoutingConstraint(String activity, C constraint) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		Validate.notNull(constraint);
		validateAttribute(constraint.getElement());
		
		if(!routingConstraints.containsKey(activity))
			return;
		routingConstraints.get(activity).remove(constraint);
	}
	
	public boolean hasRoutingConstraints(){
		return !routingConstraints.isEmpty();
	}
	
	
	//------- Data Usge -------------------------------------------------------------------------------------------------
	
	/**
	 * Sets the data attributes used by the given activity.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attributes Attributes used as input by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @throws NullPointerException If the attribute set is <code>null</code>.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void setDataFor(String activity, Set<String> attributes) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		validateAttributes(attributes);
		if(!attributes.isEmpty()){
			Map<String, Set<DataUsage>> dataUsage = new HashMap<String, Set<DataUsage>>();
			for(String attribute: attributes){
				dataUsage.put(attribute, new HashSet<DataUsage>(validUsageModes));
			}
			activityDataUsage.put(activity, dataUsage);
		}
	}
	
	
	/**
	 * Sets the data attributes used by the given activity together with their usage.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param dataUsage Data usage of input attributes.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @throws NullPointerException If the data usage set is <code>null</code> 
	 * @see {@link DataUsage}
	 * @see #getActivities()
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void setDataUsageFor(String activity, Map<String, Set<DataUsage>> dataUsage) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		validateDataUsage(dataUsage);
		activityDataUsage.put(activity, dataUsage);
	}
	
	public void setDataUsageFor(String activity, String attribute, Set<DataUsage> usageModes) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		validateAttribute(attribute);
		validateUsageModes(usageModes);
		if(!activityDataUsage.containsKey(activity)){
			activityDataUsage.put(activity, new HashMap<String, Set<DataUsage>>());
		}
		activityDataUsage.get(activity).put(attribute, usageModes);
	}
	
	/**
	 * Adds a data attribute for an activity.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attribute Attribute used by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException If the given activity/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageFor(String activity, String attribute) throws CompatibilityException, ParameterException {
		setDataUsageFor(activity, attribute, new HashSet<DataUsage>(validUsageModes));
	}
	
	/**
	 * Adds a data attribute for an activity together with its usage.<br>
	 * The given activity/attributes have to be known by the context, i.e. be contained in the activity/attribute list.<br>
	 * When dataUsage is null, then no usage is added.
	 * 
	 * @param activity Activity for which the attribute usage is set.
	 * @param attribute Attribute used by the given activity.
	 * @param dataUsage Usage of the data attribute by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageFor(String activity, String attribute, DataUsage dataUsage) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		validateAttribute(attribute);
		Validate.notNull(dataUsage);
		if(activityDataUsage.get(activity) == null){
			activityDataUsage.put(activity, new HashMap<String, Set<DataUsage>>());
		}
		if(activityDataUsage.get(activity).get(attribute)==null){
			activityDataUsage.get(activity).put(attribute, new HashSet<DataUsage>());
		}
		activityDataUsage.get(activity).get(attribute).add(dataUsage);
	}
	
	public void removeDataUsageFor(String activity, String attribute, DataUsage dataUsage) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		validateAttribute(attribute);
		Validate.notNull(dataUsage);
		if(!activityDataUsage.containsKey(activity))
			return;
		if(!activityDataUsage.get(activity).containsKey(attribute))
			return;
		activityDataUsage.get(activity).get(attribute).remove(dataUsage);
	}
	
	public void removeDataUsageFor(String activity, String attribute) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		validateAttribute(attribute);
		if(!activityDataUsage.containsKey(activity))
			return;
		activityDataUsage.get(activity).remove(attribute);
	}
	
	
	/**
	 * Adds a data attribute for all given activities together with its usage.<br>
	 * The given activities/attributes have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activities Activities for which the attribute usage is set.
	 * @param attribute Attribute used by the given activities.
	 * @param dataUsage Usage of the data attribute by the given activities.
	 * @throws ParameterException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activities/attributes are not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 * @see #setAttributes(List)
	 */
	public void addDataUsageForAll(Collection<String> activities, String attribute, DataUsage dataUsage) throws ParameterException {
		Validate.notNull(activities);
		Validate.notEmpty(activities);
		for(String activity: activities){
			addDataUsageFor(activity, attribute, dataUsage);
		}
	}
	
	/**
	 * Returns the usage of the given data attribute by the given activity.<br>
	 * If the given attribute is not used as input by the given activity, the returned
	 * set contains no elements.
	 * The given activity/attribute have to be known by the context, i.e. be contained in the activity/attribute list.
	 * 
	 * @param activity Activity for which the usage is requested.
	 * @param attribute Attribute used by the given activity.
	 * @return The usage of the given data attribute by the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity/attribute is not known.
	 * @see {@link #getActivities()}
	 * @see #getAttributes()
	 */
	public Set<DataUsage> getDataUsageFor(String activity, String attribute) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		validateAttribute(attribute);
		if(activityDataUsage.get(activity) == null)
			return new HashSet<DataUsage>(); //No input data elements for this activity
		if(activityDataUsage.get(activity).get(attribute) == null)
			return new HashSet<DataUsage>(); //Attribute not used by the given activity
		return Collections.unmodifiableSet(activityDataUsage.get(activity).get(attribute));
	}
	
	public Set<String> getActivitiesWithDataUsage(){
		return activityDataUsage.keySet();
	}
	
	/**
	 * Returns all attributes of the given activity together with their usage.<br>
	 * If the given attribute has no attributes, the returned set contains no elements.<br>
	 * The given activity has to be known by the context, i.e. be contained in the activity list.
	 * 
	 * @param activity Activity for which the attribute usage is requested.
	 * @return All attributes of the given activity together with their usage.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity is not known.
	 * @see {@link #getActivities()}
	 */
	public Map<String, Set<DataUsage>> getDataUsageFor(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		if(activityDataUsage.get(activity) == null)
			return new HashMap<String, Set<DataUsage>>(); //No input data elements for this activity
		return Collections.unmodifiableMap(activityDataUsage.get(activity));
	}
	
	public boolean hasDataUsage(){
		return !activityDataUsage.isEmpty();
	}
	
	public boolean hasDataUsage(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		return activityDataUsage.containsKey(activity);
	}
	
	/**
	 * Returns all attributes of the given activity.<br>
	 * If the given attribute has no attributes, the returned set contains no elements.
	 * The given activity has to be known by the context, i.e. be contained in the activity list.
	 * 
	 * @param activity Activity for which the attributes are requested.
	 * @return All attributes of the given activity.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws IllegalArgumentException IllegalArgumentException If the given activity is not known.
	 * @see {@link #getActivities()}
	 */
	public Set<String> getAttributesFor(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		if(activityDataUsage.get(activity) == null)
			return new HashSet<String>(); //No input data elements for this activity
		return Collections.unmodifiableSet(activityDataUsage.get(activity).keySet());
	}
	
	/**
	 * Checks if the given subject is authorized to execute the given activity.<br>
	 * A subject is authorized for execution, if it is authorized to execute the activity itself,<br>
	 * plus has permission to access all attributes the activity uses in the same modes.<br>
	 * This method delegates the call to the access control model.
	 * 
	 * @param subject The subject in question.
	 * @param activity The name of a process activity.
	 * @return <code>true</code> if the given subject is authorized to execute the given activity;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws Exception If the activity or subject are not known,<br>
	 * or if the access control model throws an exception.
	 */
	public boolean isAuthorized(String subject, String activity) throws CompatibilityException, ParameterException {
		if(!subjects.contains(subject))
			throw new CompatibilityException("Unknown subject: " + subject);
		validateActivity(activity);
		boolean authorizedForActivity = acModel.isAuthorizedForTransaction(subject, activity);
		if(!authorizedForActivity)
			return false;
		if(hasDataUsage(activity)){
			for(String attribute: activityDataUsage.get(activity).keySet()){
				for(DataUsage usageMode: activityDataUsage.get(activity).get(attribute)){
					if(!acModel.isAuthorizedForObject(subject, attribute, usageMode))
						return false;
				}
			}
		}
		return true;
	}
	
	public Set<String> getAuthorizedSubjects(String activity) throws CompatibilityException, ParameterException{
		validateActivity(activity);
		Set<String> authorizedSubjects = new HashSet<String>();
		for(String subject: getSubjects()){
			if(isAuthorized(subject, activity)){
				authorizedSubjects.add(subject);
			}
		}
		return authorizedSubjects;
	}
	
	/**
	 * Checks if the given activity is executable, i.e. there is at least one subject
	 * which is authorized to execute it.<br>
	 * This method delegates the call to the access control model.
	 * 
	 * @param activity The activity in question.
	 * @return <code>true</code> if the given activity is executable;<br>
	 * <code>false</code> otherwise.
	 * @throws ParameterException 
	 * @throws CompatibilityException 
	 * @throws Exception If the activity is not known,<br>
	 * or if the access control model throws an exception.
	 */
	public boolean isExecutable(String activity) throws CompatibilityException, ParameterException {
		validateActivity(activity);
		return acModel != null && acModel.isExecutable(activity);
	}
	
	/**
	 * Checks if the context is in a valid state.<br>
	 * A context is valid, if every activity is executable,
	 * i.e. there exists at least one subject that is permitted to execute it.
	 * @return <code>true</code> if the context is valid;<br>
	 * <code>false</code> otherwise.
	 */
	public boolean isValid(){
		try {
			for (String activity : activities) {
				if (!isExecutable(activity))
					return false;
			}
		} catch (CompatibilityException e) {
			// Cannot happen, since only activities of the context are used.
			e.printStackTrace();
		} catch (ParameterException e) {
			// Cannot happen, since context does not accept "null" activities.
			e.printStackTrace();
		}
		return true;
	}
	
	
	//------- Helper methods ----------------------------------------------------------------
	
	/**
	 * Checks if the given activity is known, i.e. is contained in the activity list.
	 * @param activity Activity to be checked.
	 * @throws CompatibilityException, ParameterException 
	 * @throws IllegalArgumentException If the given activity is not known.
	 */
	protected void validateActivity(String activity) throws CompatibilityException, ParameterException {
		Validate.notNull(activity);
		if(!activities.contains(activity))
			throw new CompatibilityException("Unknown activity: " + activity);
	}
	
	/**
	 * Checks if the given attribute is known, i.e. is contained in the attribute list.
	 * @param attribute Attribute to be checked.
	 * @throws IllegalArgumentException If the given attribute is not known.
	 */
	protected void validateAttribute(String attribute) throws CompatibilityException, ParameterException {
		Validate.notNull(attribute);
		if(!attributes.contains(attribute))
			throw new CompatibilityException("Unknown attribute: " + attribute);
	}
	
	/**
	 * Checks if the given attributes are known, i.e. they are all contained in the attribute list.
	 * @param attributes Attributes to be checked.
	 * @throws IllegalArgumentException If not all given attribute are known.
	 */
	protected void validateAttributes(Collection<String> attributes) throws CompatibilityException, ParameterException{
		Validate.notNull(attributes);
		if(!this.attributes.containsAll(attributes))
			throw new CompatibilityException("Unknown attributes");
	}
	
	protected void validateDataUsage(Map<String, Set<DataUsage>> dataUsage) throws ParameterException{
		Validate.notNull(dataUsage);
		Validate.noNullElements(dataUsage.keySet());
		Validate.noNullElements(dataUsage.values());
		for(Set<DataUsage> usageModes: dataUsage.values())
			validateUsageModes(usageModes);
	}
	
	public void validateACModel(ACModel acModel) throws InconsistencyException, ParameterException{
		validateACModel(acModel, getSubjects(), getActivities(), getAttributes(), validUsageModes);
	}
	
	@SuppressWarnings("unchecked")
	private void validateACModel(ACModel acModel, 
								 Collection<String> subjects, 
								 Collection<String> activities, 
								 Collection<String> attributes, 
								 List<DataUsage> dataUsageModes) throws ParameterException, InconsistencyException{
		Validate.notNull(acModel);
		if(!acModel.getSubjects().containsAll(subjects))
			throw new InconsistencyException("Incompatible access control model: Missing subjects.");
		if(!acModel.getTransactions().containsAll(activities))
			throw new InconsistencyException("Incompatible access control model: Missing activities.");
		if(!acModel.getObjects().containsAll(attributes))
			throw new InconsistencyException("Incompatible access control model: Missing attributes.");
		if(!SetUtils.containSameElements(new HashSet<DataUsage>(dataUsageModes), new HashSet<DataUsage>(acModel.getValidUsageModes())))
			throw new InconsistencyException("Incompatible access control model: Different set of valid data usage modes.");
	}
	
	protected void validateUsageModes(Collection<DataUsage> usageModes) throws ParameterException{
		Validate.notNull(usageModes);
		Validate.notEmpty(usageModes);
		Validate.noNullElements(usageModes);
		if(!validUsageModes.containsAll(usageModes))
			throw new ParameterException(ErrorCode.INCOMPATIBILITY, "Invalid usage mode. Permitted values: " + validUsageModes);
	}
	
	
	//------- Static methods ----------------------------------------------------------------
	
	/**
	 * Creates a new context using an RBAC access control model.<br>
	 * Users and permissions to execute transactions are randomly assigned to the given roles.<br>
	 * Each person is assigned to exactly one role.
	 * @param transitions A collection of Petri net transitions.
	 * @param originatorCount The number of desired originators.
	 * @param roles The roles to use.
	 * @return A new randomly generated Context.
	 * @throws ParameterException 
	 * @throws Exception 
	 */
	public static Context createRandomContext(Collection<AbstractTransition<?,?>> transitions, int originatorCount, List<String> roles) throws ParameterException {
		return createRandomContext(PNUtils.getLabelSetFromTransitions(transitions, false), originatorCount, roles);
	}
	
	/**
	 * Creates a new context using an RBAC access control model.<br>
	 * Users and permissions to execute transactions are randomly assigned to the given roles.<br>
	 * Each person is assigned to exactly one role.
	 * @param activities The process activities.
	 * @param originatorCount The number of desired originators.
	 * @param roles The roles to use.
	 * @return A new randomly generated Context.
	 * @throws Exception 
	 */
	public static Context createRandomContext(Set<String> activities, int originatorCount, List<String> roles) throws ParameterException {
		Validate.notNull(activities);
		Validate.noNullElements(activities);
		Validate.notNegative(originatorCount);
		Validate.notNull(roles);
		Validate.noNullElements(roles);
		
		Context newContext = new Context("Random Context", activities);
		List<String> cOriginators = createSubjectList(originatorCount);
		newContext.setSubjects(new HashSet<String>(cOriginators));
		//Create a new access control model.
		newContext.setACModel(RBACModel.createRandomModel(cOriginators, activities, roles));
		return newContext;
	}
	
	/**
	 * Creates a list of subject names with the given size.
	 * @param number Number of subjects to create.
	 * @return A list of subject names.
	 * @throws ParameterException 
	 */
	public static List<String> createSubjectList(int number) throws ParameterException{
		return createSubjectList(number, "%s");
	}
	
	/**
	 * Creates a list of subject names in the given format with the given size.
	 * @param number Number of subjects to create.
	 * @param stringFormat The format for subject names.
	 * @return A list of subject names.
	 * @throws ParameterException 
	 */
	public static List<String> createSubjectList(int number, String stringFormat) throws ParameterException{
		Validate.notNegative(number);
		Validate.notNull(stringFormat);
		List<String> result = new ArrayList<String>(number);
		for(int i=1; i<=number; i++){
			result.add(String.format(stringFormat, i));
		}
		return result;
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Context name: ");
		builder.append(getName());
		builder.append('\n');
		builder.append('\n');
		
		if(hasActivities()){
			builder.append("Activities: ");
			builder.append(getActivities());
			builder.append('\n');
		}
		if(hasSubjects()){
			builder.append("Subjects: ");
			builder.append(getSubjects());
			builder.append('\n');
		}
		if(hasAttributes()){
			builder.append("Attributes: ");
			builder.append(getAttributes());
			builder.append('\n');
		}
		
		if(hasDataUsage()){
			builder.append('\n');
			builder.append("Activity data usage:");
			builder.append('\n');
			builder.append('\n');
			for(String activity: activityDataUsage.keySet()){
				builder.append(activity);
				builder.append(": ");
				builder.append(activityDataUsage.get(activity));
				builder.append('\n');
			}
		}
		
		if(hasRoutingConstraints()){
			builder.append('\n');
			builder.append("Routing constraints:");
			builder.append('\n');
			builder.append('\n');
			for(String activity: routingConstraints.keySet()){
				builder.append(activity);
				builder.append(": ");
				builder.append(routingConstraints.get(activity));
				builder.append('\n');
			}
		}
		
		if(getACModel() != null){
			builder.append('\n');
			builder.append("Activity permissions:");
			builder.append('\n');
			builder.append('\n');
			for(String activity: getActivities()){
				builder.append(activity);
				builder.append(": ");
				try {
					builder.append(getACModel().getAuthorizedSubjectsForTransaction(activity));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				} catch (ParameterException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
			

			builder.append('\n');
			builder.append("Attribute permissions:");
			builder.append('\n');
			builder.append('\n');
			for(String attribute: getAttributes()){
				builder.append(attribute);
				builder.append(": ");
				try {
					Map<String, Set<DataUsage>> subjectsAndPermissions = getACModel().getAuthorizedSubjectsAndPermissionsForObject(attribute);
					if(!subjectsAndPermissions.isEmpty()){
						builder.append('[');
						for(String subject: subjectsAndPermissions.keySet()){
							builder.append(subject);
							builder.append(subjectsAndPermissions.get(subject));
							builder.append(' ');
						}
						builder.append(']');
					}
				} catch (CompatibilityException e) {
					e.printStackTrace();
				} catch (ParameterException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
			
			builder.append('\n');
			builder.append("Execution authorization:");
			builder.append('\n');
			builder.append('\n');
			for(String activity: getActivities()){
				builder.append(activity);
				builder.append(": ");
				try {
					builder.append(getAuthorizedSubjects(activity));
				} catch (CompatibilityException e) {
					e.printStackTrace();
				} catch (ParameterException e) {
					e.printStackTrace();
				}
				builder.append('\n');
			}
		}
		
		return builder.toString();
	}
	
	public ContextProperties getProperties() throws ParameterException, PropertyException{
		if(!isValid())
			throw new ParameterException(ErrorCode.INCONSISTENCY, "Cannot extract properties in invalid state!");
		
		ContextProperties result = new ContextProperties();
		
		result.setName(getName());
		
		result.addActivities(getActivities());
		
		result.addSubjects(getSubjects());
		
		Set<String> attributes = getAttributes();
		if(attributes != null && !attributes.isEmpty())
			result.addAttributes(getAttributes());
		
		result.setACModelName(getACModel().getName());
		
		for(String activity: getActivities()){
			Set<AbstractConstraint<?>> routingConstraints = getRoutingConstraints(activity);
			if(routingConstraints != null && !routingConstraints.isEmpty()){
				for(AbstractConstraint<?> routingConstraint: routingConstraints){
					result.addRoutingConstraint(activity, routingConstraint);
				}
			}
			
			Map<String, Set<DataUsage>> dataUsage = getDataUsageFor(activity);
			if(dataUsage != null && !dataUsage.isEmpty()){
				result.setDataUsage(activity, dataUsage);
			}
		}
		
		return result;
	}
	
	public void takeOverValues(Context context) throws ParameterException{
		Validate.notNull(context);
		
		name = ContextProperties.defaultName;
		String name = context.getName();
		if(name != null){
			setName(name);
		}
		
		activities = new HashSet<String>();
		Set<String> otherActivities = context.getActivities();
		if(otherActivities != null){
			activities.clear();
			activities.addAll(otherActivities);
		}
		
		subjects = new HashSet<String>();
		Set<String> otherSubjects = context.getSubjects();
		if(otherSubjects != null){
			subjects.clear();
			subjects.addAll(otherSubjects);
		}
		
		attributes = new HashSet<String>();
		Set<String> otherAttributes = context.getAttributes();
		if(otherAttributes != null){
			attributes.clear();
			attributes.addAll(otherAttributes);
		}
		
		acModel = null;
		ACModel otherACModel = context.getACModel();
		if(otherACModel != null){
			setACModel(otherACModel);
		}
		
		setValidUsageModes(context.getValidUsageModes());
		
		activityDataUsage.clear();
		for(String activity: context.getActivitiesWithDataUsage()){
			Map<String, Set<DataUsage>> dataUsage = context.getDataUsageFor(activity);
			for(String attribute: dataUsage.keySet()){
				setDataUsageFor(activity, attribute, new HashSet<DataUsage>(dataUsage.get(attribute)));
			}
		}
		
		routingConstraints.clear();
		for(String activity: context.getActivitiesWithRoutingConstraints()){
			Set<AbstractConstraint<?>> otherRoutingConstraints = context.getRoutingConstraints(activity);
			for(AbstractConstraint<?> routingConstraint: otherRoutingConstraints){
				addRoutingConstraint(activity, routingConstraint.clone());
			}
		}
	}
	
	@Override
	public Context clone() {
			
		Context result = null;
		
		try{
			result = new Context(name, activities);
			
			//Set Subjects
			if(!subjects.isEmpty())
				result.setSubjects(subjects);
			
			//Set Attributes
			if(!attributes.isEmpty())
				result.setAttributes(attributes);
			
			//Set AC Model
			if(acModel != null)
				result.setACModel(acModel);
			
			//Set valid usage modes
			result.setValidUsageModes(validUsageModes);
			
			//Add data usage
			for(String activity: activityDataUsage.keySet()){
				Map<String, Set<DataUsage>> dataUsage = activityDataUsage.get(activity);
				for(String attribute: dataUsage.keySet()){
					result.setDataUsageFor(activity, attribute, new HashSet<DataUsage>(dataUsage.get(attribute)));
				}
			}
			
			//Add routing constraints
			for(String activity: routingConstraints.keySet()){
				for(AbstractConstraint<?> routingConstraint: routingConstraints.get(activity)){
					result.addRoutingConstraint(activity, routingConstraint.clone());
				}
			}
		
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}

		return result;
	}
	
	public static void main(String[] args) throws ParameterException, PropertyException, IOException {
		Map<String, Set<DataUsage>> usage1 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes1 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage1.put("attribute1", modes1);
		
		Map<String, Set<DataUsage>> usage2 = new HashMap<String, Set<DataUsage>>();
		Set<DataUsage> modes2 = new HashSet<DataUsage>(Arrays.asList(DataUsage.READ, DataUsage.WRITE));
		usage2.put("attribute2", modes2);
		
		Set<String> activities = new HashSet<String>(Arrays.asList("act1", "act2"));
		Set<String> attributes = new HashSet<String>(Arrays.asList("attribute1", "attribute2"));
		Set<String> subjects = new HashSet<String>(Arrays.asList("s1", "s2"));
		Context c = new Context("c1", activities);
		c.addAttributes(attributes);
		c.addSubjects(subjects);
		c.setDataUsageFor("act1", usage1);
		c.setDataUsageFor("act2", usage2);
		c.addRoutingConstraint("act1", NumberConstraint.parse("attribute1 < 200"));
		
		ACLModel acModel = new ACLModel(subjects);
		acModel.setName("acmodel1");
		acModel.addTransactions(activities);
		acModel.addObjects(attributes);
		acModel.setTransactionPermission("s1", activities);
		c.setACModel(acModel);
		
		c.getProperties().store("GERD");
	}

}
