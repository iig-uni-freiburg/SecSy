package de.uni.freiburg.iig.telematik.secsy.logic.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.invation.code.toval.constraint.AbstractConstraint;
import de.invation.code.toval.types.DataUsage;
import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.uni.freiburg.iig.telematik.sewol.accesscontrol.AbstractACModel;
import de.uni.freiburg.iig.telematik.sewol.context.ProcessConstraintContext;


public class SynthesisContext extends ProcessConstraintContext {
	/**
	 * Data usage (read, write, ...) for attributes which are used in process activities.
	 */
	protected Map<String, Map<String, Set<DataUsage>>> activityDataUsage = new HashMap<String, Map<String, Set<DataUsage>>>();
	/**
	 * Access control model used to decide which subjects can execute which activities.
	 */
	protected AbstractACModel acModel = null;
	/**
	 * Constraints on attributes, which are used for routing purposes.<br>
	 * Example: Activity "Double Check" is executed when the credit amount exceeds $50.000.<br>
	 */
	protected Map<String, Set<AbstractConstraint<?>>> routingConstraints = new HashMap<String, Set<AbstractConstraint<?>>>();
	
	protected List<DataUsage> validUsageModes = new ArrayList<DataUsage>(Arrays.asList(DataUsage.values()));
	
	
	//------- Constructors ---------------------------------------------------------------------------------------------
	
	/**
	 * Creates a new context using the given activity names.
	 * @param activities Names of process activities.
	 * @throws ParameterException 
	 * @throws Exception If activity list is <code>null</code> or empty.
	 */
	public SynthesisContext(String name, Set<String> activities){
		super(name);
		setActivities(activities);
	}

	
	//------- Validity --------------------------------------------------------------------------------------------------
	
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
		}
		return true;
	}
	

}
